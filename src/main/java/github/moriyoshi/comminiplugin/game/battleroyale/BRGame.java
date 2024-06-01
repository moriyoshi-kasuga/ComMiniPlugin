package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.game.battleroyale.BRField.SIGNAL;
import github.moriyoshi.comminiplugin.game.battleroyale.items.WingItem;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.gametype.WinnerTypeGame;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import github.moriyoshi.comminiplugin.util.tuple.Sequence;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.bossbar.BossBar;

public class BRGame extends AbstractGame implements WinnerTypeGame {

  public final HashMap<UUID, Boolean> players = new HashMap<>();

  private final int BORDERE_INTERVAL = 60;
  private final int START_DROP = 15;
  private final int BORDERE_CONTRACTION_TIME = 50;
  private final int BORDERE_CONTRACTION_SIZE = 70;
  private final int BORDERE_BEFORE_MOVE_TIME = 10;

  private final List<Sequence<Integer, Integer, Material, BlockData>> lobbyBlows = new ArrayList<>();

  private BossBar bossBar;

  @Getter
  private boolean isCanPvP;

  @Getter
  private BRField field;

  public void setField(final BRField field) {
    if (this.field != null) {
      this.field.stop();
    }
    this.field = field;
  }

  public BRGame() {
    super(
        "battleroyale",
        "<yellow>バトルロワイヤル",
        "<yellow>殺せ!殺せ!勝ち上がれ!",
        Material.GOLDEN_SWORD,
        new PrefixUtil("<gray>[<yellow>BattleRoyale<gray>]"),
        new BRListener());
    this.world = ComMiniWorld.GAME_WORLD;
  }

  public final void joinPlayer(final Player player, final boolean isPlayer) {
    val uuid = player.getUniqueId();
    if (players.containsKey(uuid)) {
      if (players.get(uuid) == isPlayer) {
        players.remove(uuid);
        prefix.cast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
        return;
      }
    }
    players.put(uuid, isPlayer);
    prefix.cast(player.getName() + "が" + (isPlayer ? "<blue>参加" : "<gray>観戦") + "します");
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
  public boolean addSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, false);
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    teleportLobby(player);
    return true;
  }

  @Override
  public boolean initializeGame(final Player player) {
    return true;
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  protected boolean innerStartGame(final Player player) {
    if (2 > players.values().stream().filter(v -> v).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }
    if (field == null) {
      prefix.send(player, "<red>フィールドを選択してください");
      return false;
    }
    field.initialize();

    bossBar = BossBar.bossBar(Util.mm("<red>投下まで<u>20</u>秒"), 1f,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_10);

    runPlayers(p -> {
      val uuid = p.getUniqueId();
      val inv = p.getInventory();
      inv.clear();
      if (players.get(uuid)) {
        val gamePlayer = ComMiniPlayer.getPlayer(uuid);
        gamePlayer.setHideNameTag(true);
        gamePlayer.getGamePlayerData(BRPlayer.class).getHotbar().setItems(inv);
        p.setSaturation(6);
        p.setGameMode(GameMode.SURVIVAL);
      } else {
        p.setGameMode(GameMode.SPECTATOR);
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
      }
      p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 0, true, false));
      p.showBossBar(bossBar);
      teleportLobby(p);
    });

    field.getTreasure().setTreasures();

    new BukkitRunnable() {

      private int time = START_DROP + 1;

      @Override
      public void run() {
        if (!GameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (--time == 0) {
          runPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1));

          val loc = getLobby().clone().subtract(0, 1, 0);
          for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
              val temp = loc.clone().add(x, 0, z).getBlock();
              lobbyBlows.add(Sequence.of(x, z, temp.getType(), temp.getBlockData()));
              temp.setType(Material.AIR);
            }
          }
          runPlayers(WingItem::setWing);

          startContractionBorder();

          new BukkitRunnable() {

            @Override
            public void run() {
              if (GameSystem.isIn()) {
                lobbyBlows.forEach(s -> {
                  val b = loc.clone().add(s.getFirst(), 0, s.getSecond()).getBlock();
                  b.setType(s.getThird());
                  b.setBlockData(s.getFourth());
                });
                lobbyBlows.clear();
              }
            }

          }.runTaskLater(ComMiniPlugin.getPlugin(), 20 * 10);

          new BukkitRunnable() {

            @Override
            public void run() {
              if (GameSystem.isIn()) {
                isCanPvP = true;
              }
            }

          }.runTaskLater(ComMiniPlugin.getPlugin(), 20 * 3);
          this.cancel();
          return;
        }
        if (3 >= time) {
          runPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 1));
        }
        bossBar.name(Util.mm("<red>投下まで<u>" + time + "</u>秒")).progress((float) time / (float) START_DROP);
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);

    hidePlayer();
    return true;
  }

  public void startContractionBorder() {
    field.startContraction(field.getLobby(), BORDERE_CONTRACTION_SIZE, BORDERE_CONTRACTION_TIME, signal -> {
      switch (signal) {
        case SIGNAL.MIN min -> {
          new BukkitRunnable() {

            private int temp = BORDERE_BEFORE_MOVE_TIME + 1;

            @Override
            public void run() {
              if (!GameSystem.isIn()) {
                this.cancel();
                return;
              }
              if (0 >= --temp) {
                bossBar.name(Util.mm("<red>動きまくります!")).progress(0f);
                field.startMove(20, 15, 10);
                this.cancel();
                return;
              }
              bossBar.name(Util.mm("<yellow>ボーダー最小サイズ: 動くまで<u>" + temp + "</u>秒"))
                  .progress((float) temp / (float) BORDERE_BEFORE_MOVE_TIME);
            }

          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
        }
        case SIGNAL.END end -> {
          new BukkitRunnable() {

            private int temp = BORDERE_INTERVAL + 1;

            @Override
            public void run() {
              if (!GameSystem.isIn()) {
                this.cancel();
                return;
              }
              if (0 >= --temp) {
                startContractionBorder();
                this.cancel();
                return;
              }
              bossBar.name(Util.mm("<aqua>ボーダー停止中: 起動まで<u>" + temp + "</u>秒"))
                  .progress((float) temp / (float) BORDERE_INTERVAL);
            }

          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
        }
        case SIGNAL.NONE none -> {
          bossBar.name(Util.mm("<red>ボーダー収縮残り: <u>" + none.restTime() + "</u>秒"))
              .progress((float) none.restTime() / (float) BORDERE_CONTRACTION_TIME);
        }
      }
    });

  }

  @Override
  protected void innerFinishGame() {
    if (field != null) {
      val loc = field.getLobby();
      lobbyBlows.forEach(s -> {
        val b = loc.clone().add(s.getFirst(), 0, s.getSecond()).getBlock();
        b.setType(s.getThird());
        b.setBlockData(s.getFourth());
      });
      field.stop();
    }
    if (bossBar != null) {
      runPlayers(p -> p.hideBossBar(bossBar));
    }
    lobbyBlows.clear();
    isCanPvP = false;
    players.clear();
    showPlayer();
  }

  @Override
  protected void fieldInitialize(final boolean isCreatingInstance) {
    this.field = null;
    this.bossBar = null;
  }

  @Override
  public Location getLobby() {
    return field.getLobby();
  }

  @Override
  public World getWorld() {
    return field.getLobby().getWorld();
  }

}
