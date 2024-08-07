package github.moriyoshi.comminiplugin.biggame.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.biggame.battleroyale.BRField.SIGNAL;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.WingItem;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.tuple.Sequence;
import github.moriyoshi.comminiplugin.system.AbstractBigGame;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.IGameListener;
import github.moriyoshi.comminiplugin.system.type.ISpectatorGame;
import github.moriyoshi.comminiplugin.system.type.IWinnerTypeBigGame;
import github.moriyoshi.comminiplugin.util.KeyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BRBigGame extends AbstractBigGame implements IWinnerTypeBigGame, ISpectatorGame {

  public final HashMap<UUID, Boolean> players = new HashMap<>();

  private final int START_DROP = 15;

  private final List<Sequence<Integer, Integer, Material, BlockData>> lobbyBlocks =
      new ArrayList<>();

  private final AttributeModifier resistance =
      new AttributeModifier(KeyUtil.createUUIDKey(), 20, Operation.ADD_NUMBER);

  private BossBar bossBar;

  @Getter private boolean isCanPvP;

  @Getter private BRField field;

  public BRBigGame(
      Material icon,
      String id,
      String name,
      String description,
      Player player,
      PrefixUtil prefixUtil,
      Function<IdentifierKey, IGameListener<?>> listener)
      throws GameInitializeFailedException {
    super(icon, id, name, description, player, prefixUtil, listener);
    this.world = ComMiniWorld.GAME_WORLD;
    this.field = null;
    this.bossBar = null;
  }

  @Override
  public String getId() {
    return "battleroyale";
  }

  @Override
  public String getName() {
    return "<yellow>バトルロワイヤル";
  }

  @Override
  public String getDescription() {
    return "<yellow>殺せ!殺せ!勝ち上がれ!";
  }

  @Override
  public Material getIcon() {
    return Material.GOLDEN_SWORD;
  }

  public void setField(final BRField field) {
    if (this.field != null) {
      this.field.stop();
    }
    this.field = field;
  }

  public final void joinPlayer(final Player player, final boolean isPlayer) {
    val uuid = player.getUniqueId();
    if (players.containsKey(uuid)) {
      if (players.get(uuid) == isPlayer) {
        players.remove(uuid);
        prefix.broadCast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
        return;
      }
    }
    players.put(uuid, isPlayer);
    prefix.broadCast(player.getName() + "が" + (isPlayer ? "<blue>参加" : "<gray>観戦") + "します");
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new BRAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(final Player player) {
    return new BRMenu();
  }

  @Override
  public boolean predicateSpec(Player player) {
    return true;
  }

  @Override
  public void innerAddSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, false);
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    player.teleport(getLobby());
  }

  public boolean isPlayingPlayer(Player player) {
    return players.containsKey(player.getUniqueId()) && players.get(player.getUniqueId());
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  public boolean predicateStartGame(final Audience player) {
    if (2 > players.values().stream().filter(v -> v).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }
    if (field == null) {
      prefix.send(player, "<red>フィールドを選択してください");
      return false;
    }
    return true;
  }

  @Override
  protected void innerStartGame() {
    field.initialize();

    bossBar =
        BossBar.bossBar(
            BukkitUtil.mm("<red>投下まで<u>20</u>秒"),
            1f,
            BossBar.Color.RED,
            BossBar.Overlay.NOTCHED_10);

    val barrier =
        new ItemBuilder(Material.BARRIER)
            .name("<red>Barrier")
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .build();
    runPlayers(
        p -> {
          val uuid = p.getUniqueId();
          val inv = p.getInventory();
          inv.clear();
          if (players.get(uuid)) {
            val gamePlayer = ComMiniPlayer.getPlayer(uuid);
            gamePlayer
                .setHideNameTag(true)
                .setCanFoodRegain(false)
                .getGamePlayerData(BRPlayer.class)
                .getHotbarSlot()
                .setItems(inv);
            for (int i = 9; i < 27; i++) {
              inv.setItem(i, barrier);
            }
            p.setGameMode(GameMode.SURVIVAL);
            p.getAttribute(Attribute.GENERIC_ARMOR).addTransientModifier(resistance);
          } else {
            p.setGameMode(GameMode.SPECTATOR);
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
          }
          p.showBossBar(bossBar);
          p.teleport(getLobby());
        });

    field.getTreasure().setTreasures();

    new BukkitRunnable() {

      private int time = START_DROP + 1;

      @Override
      public void run() {
        if (!BigGameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (--time == 0) {
          runPlayers(
              p -> {
                WingItem.setWing(p);
                p.playSound(
                    p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1);
              });

          val loc = getLobby().clone().subtract(0, 1, 0);
          for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
              val temp = loc.clone().add(x, 0, z).getBlock();
              lobbyBlocks.add(Sequence.of(x, z, temp.getType(), temp.getBlockData()));
              temp.setType(Material.AIR);
            }
          }

          new BukkitRunnable() {

            private int temp = field.getBorder_first_before_move_time() + 1;

            @Override
            public void run() {
              if (!BigGameSystem.isIn()) {
                this.cancel();
                return;
              }
              if (0 >= --temp) {
                startContractionBorder();
                this.cancel();
                return;
              }
              bossBar
                  .name(BukkitUtil.mm("<aqua>ボーダー停止中: 起動まで<u>" + temp + "</u>秒"))
                  .progress((float) temp / field.getBorder_first_before_move_time());
            }
          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);

          new BukkitRunnable() {

            @Override
            public void run() {
              if (BigGameSystem.isIn()) {
                lobbyBlocks.forEach(
                    s -> {
                      val b = loc.clone().add(s.getFirst(), 0, s.getSecond()).getBlock();
                      b.setType(s.getThird());
                      b.setBlockData(s.getFourth());
                    });
                lobbyBlocks.clear();
              }
            }
          }.runTaskLater(ComMiniPlugin.getPlugin(), 20 * 10);

          new BukkitRunnable() {

            @Override
            public void run() {
              if (BigGameSystem.isIn()) {
                isCanPvP = true;
              }
            }
          }.runTaskLater(ComMiniPlugin.getPlugin(), 20 * 3);
          this.cancel();
          return;
        }
        if (3 >= time) {
          runPlayers(
              p ->
                  p.playSound(
                      p.getLocation(),
                      Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                      SoundCategory.MASTER,
                      1,
                      1));
        }
        bossBar
            .name(BukkitUtil.mm("<red>投下まで<u>" + time + "</u>秒"))
            .progress((float) time / (float) START_DROP);
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
  }

  public void startContractionBorder() {
    field.startContraction(
        field.getBorder_contraction_size(),
        field.getBorder_contraction_time(),
        signal -> {
          switch (signal) {
            case SIGNAL.MIN ignored -> {
              new BukkitRunnable() {

                private int temp = field.getBorder_before_move_time() + 1;

                @Override
                public void run() {
                  if (!BigGameSystem.isIn()) {
                    this.cancel();
                    return;
                  }
                  if (0 >= --temp) {
                    bossBar.name(BukkitUtil.mm("<red>動きまくります!")).progress(0f);
                    field.startMove(15, 10, 10);
                    this.cancel();
                    return;
                  }
                  bossBar
                      .name(BukkitUtil.mm("<yellow>ボーダー最小サイズ: 動くまで<u>" + temp + "</u>秒"))
                      .progress((float) temp / (float) field.getBorder_before_move_time());
                }
              }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
            }
            case SIGNAL.END ignored -> {
              new BukkitRunnable() {

                private int temp = field.getBorder_interval() + 1;

                @Override
                public void run() {
                  if (!BigGameSystem.isIn()) {
                    this.cancel();
                    return;
                  }
                  if (0 >= --temp) {
                    startContractionBorder();
                    this.cancel();
                    return;
                  }
                  bossBar
                      .name(BukkitUtil.mm("<aqua>ボーダー停止中: 起動まで<u>" + temp + "</u>秒"))
                      .progress((float) temp / (float) field.getBorder_interval());
                }
              }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
            }
            case SIGNAL.NONE none -> {
              bossBar
                  .name(BukkitUtil.mm("<red>ボーダー収縮残り: <u>" + none.restTime() + "</u>秒"))
                  .progress((float) none.restTime() / (float) field.getBorder_contraction_time());
            }
          }
        });
  }

  @Override
  protected void innerFinishGame() {
    if (field != null) {
      val loc = field.getLobby();
      lobbyBlocks.forEach(
          s -> {
            val b = loc.clone().add(s.getFirst(), 0, s.getSecond()).getBlock();
            b.setType(s.getThird());
            b.setBlockData(s.getFourth());
          });
      field.stop();
    }
    if (bossBar != null) {
      runPlayers(p -> p.hideBossBar(bossBar));
    }
    runPlayers(p -> p.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(resistance));
    lobbyBlocks.clear();
    isCanPvP = false;
    players.clear();
  }

  @Override
  public Location getLobby() {
    return field.getLobby();
  }

  @Override
  public World getWorld() {
    return field.getLobby().getWorld();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createHelpMenu() {
    return new BRHelpMenu();
  }

  @Override
  public void leavePlayer(Player p) {
    val loc = p.getLocation();
    val world = p.getWorld();
    val inv = p.getInventory();
    inv.forEach(
        i -> {
          if (i == null || i.isEmpty()) {
            return;
          }
          world.dropItemNaturally(loc, i);
          val custom = CustomItem.getCustomItem(i);
          if (custom != null && custom instanceof CooldownItem cooldown) {
            cooldown.removeCooldown();
          }
        });
    inv.clear();
    val alives = players.entrySet().stream().filter(e -> e.getValue()).toList();
    if (alives.size() != 1) {
      p.teleport(getLobby());
      return;
    }
    endGame(Bukkit.getPlayer(alives.getFirst().getKey()).getName());
  }
}
