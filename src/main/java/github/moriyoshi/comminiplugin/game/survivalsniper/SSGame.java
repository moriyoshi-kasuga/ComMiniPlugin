package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.MainGameSystem;
import github.moriyoshi.comminiplugin.system.game.AbstractGame;
import github.moriyoshi.comminiplugin.system.game.WinnerTypeGame;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("deprecation")
public class SSGame extends AbstractGame implements WinnerTypeGame {

  public final HashMap<UUID, Pair<Integer, ChatColor>> players = new HashMap<>();
  private final int MAX_RADIUS_RANGE = 600;
  private final int MIN_BORDER_RANGE = 30;
  private final int MAX_SECOND = 60 * 7;
  private final int AFTER_PVP_SECOND = 60 * 5;
  private final int AIR_LIMIT = 60 * 3;
  private final Vector VOID_BLOCK_RADIUS = new Vector(3, 3, 3);
  private final double speedRate = 1.25;
  private long previousTime;
  private double previousBorderSize;
  @Getter private boolean canPvP;
  private BossBar bossBar;
  private BukkitRunnable run;
  private boolean isFinalArea;

  @Getter private Mode mode;

  public SSGame() {
    super(
        "survivalsniper",
        "<blue>サバイバルスナイパー",
        "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
        Material.SPYGLASS,
        new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
        new SSListener());
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    prefix.cast("<red>Modeが<u>" + mode.name() + "</u>に変わりました");
    prefix.cast("<gray>ゲームをしたい人はもう一度参加ボタンを押してください");
    runPlayers(MainGameSystem::initializePlayer);
    players.clear();
  }

  public final void leavePlayer(final Player player) {
    val uuid = player.getUniqueId();
    if (!players.containsKey(uuid)) {
      prefix.send(player, "<red>あなたはゲームに参加していません");
      return;
    }
    showPlayer(player);
    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    MainGameSystem.initializePlayer(player);
    val isPlayer = players.remove(uuid).getFirst() != -1;
    prefix.cast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
  }

  public final void joinPlayer(final Player player, final boolean isPlayer, final ChatColor color) {
    val prev = players.get(player.getUniqueId());
    if (prev != null && (prev.getFirst() != -1) == isPlayer && prev.getSecond() == color) {
      prefix.send(player, "<red>抜けるには抜けるボタンを押してください");
      return;
    }
    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    players.put(player.getUniqueId(), Pair.of(isPlayer ? AIR_LIMIT : -1, color));
    player.teleport(lobby);
    player.getInventory().addItem(item);
    hidePlayer(player);
    if (isPlayer) {
      if (color == null) {
        prefix.cast(player.getName() + "が<blue>参加します");
      } else {
        prefix.cast(
            BukkitUtil.mm(player.getName() + "が<white>")
                .append(BukkitUtil.colorToComponent(color, color.name()))
                .append(BukkitUtil.mm("<gray>に<blue>参加します")));
      }
    } else {
      prefix.cast(player.getName() + "が<gray>観戦します");
    }
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new SSAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(final Player player) {
    if (mode == Mode.FFA) {
      return new SSFFAMenu();
    }
    return new SSTeamMenu();
  }

  @Override
  public boolean initializeGame(final Player player) {
    if (!player.getWorld().getName().equalsIgnoreCase("world")) {
      prefix.send(player, "<red>オーバーワールドでのみ実行可能です");
      return false;
    }
    val temp = player.getLocation().clone();
    world = temp.getWorld();
    lobby = world.getHighestBlockAt(temp).getLocation().add(new Vector(0, 50, 0));
    world.getWorldBorder().setCenter(lobby);
    world.getWorldBorder().setSize(MAX_RADIUS_RANGE);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    world.setClearWeatherDuration(0);
    world.setTime(1000);
    clearMonster();
    val vec = lobby.toVector();
    val min = vec.clone().add(VOID_BLOCK_RADIUS);
    val max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    BukkitUtil.consoleCommand(
        String.format(
            "execute in %s run fill %s %s %s %s %s %s minecraft:barrier outline",
            "overworld",
            min.getBlockX(),
            min.getBlockY(),
            min.getBlockZ(),
            max.getBlockX(),
            max.getBlockY(),
            max.getBlockZ()));

    return true;
  }

  @Override
  public boolean predicateGame(final Player player) {
    if (mode == Mode.FFA) {
      if (2 > players.values().stream().filter(pair -> pair.getFirst() != -1).toList().size()) {
        prefix.send(player, "<red>二人以上でしかプレイできません");
        return false;
      }
    } else {
      if (2
          > players.values().stream()
              .filter(value -> value.getSecond() != null)
              .collect(Collectors.groupingBy(Pair::getSecond))
              .size()) {
        prefix.send(player, "<red>二チーム以上でしかプレイできません");
        return false;
      }
    }
    return true;
  }

  @Override
  public void innerStartGame() {
    new BukkitRunnable() {

      private int rest = 11;

      @Override
      public void run() {
        if (1 > --rest) {
          runPlayers(
              p -> {
                p.playSound(
                    p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1);
                BukkitUtil.title(p, "<red>スタート!", null);
              });
          start();
          this.cancel();
          return;
        }
        if (3 >= rest) {
          runPlayers(
              p ->
                  p.playSound(
                      p.getLocation(),
                      Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                      SoundCategory.MASTER,
                      1,
                      1));
        }
        runPlayers(p -> BukkitUtil.title(p, "<red><u>" + rest + "</u>秒後に始まります", null));
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
  }

  private void start() {
    val vec = lobby.toVector();
    val min = vec.clone().add(VOID_BLOCK_RADIUS);
    val max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    BukkitUtil.consoleCommand(
        String.format(
            "execute in %s run fill %s %s %s %s %s %s minecraft:air replace minecraft:barrier",
            "overworld",
            min.getBlockX(),
            min.getBlockY(),
            min.getBlockZ(),
            max.getBlockX(),
            max.getBlockY(),
            max.getBlockZ()));

    bossBar =
        BossBar.bossBar(
            BukkitUtil.mm("<red>PvP解禁まで<u>" + AFTER_PVP_SECOND + "</u>秒"),
            1f,
            BossBar.Color.RED,
            BossBar.Overlay.NOTCHED_10);

    run =
        new BukkitRunnable() {
          private int second = AFTER_PVP_SECOND + 1;

          @Override
          public void run() {
            if (second > 0) {
              if (--second > 0) {
                bossBar
                    .name(BukkitUtil.mm("<red>PvP解禁まで<u>" + second + "</u>秒"))
                    .progress((float) second / (float) AFTER_PVP_SECOND);
                if (second % 60 == 0) {
                  val message = BukkitUtil.mm("<red>PvP解禁まで<u>" + second / 60 + "</u>分");
                  runPlayers(p -> prefix.send(p, message));
                }
                if (10 >= second) {
                  runPlayers(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1));
                }
              } else {
                canPvP = true;
                runPlayers(
                    p -> {
                      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
                      p.hideBossBar(bossBar);
                      world.getWorldBorder().setSize(MIN_BORDER_RANGE, MAX_SECOND);
                      prefix.send(p, "<red>PvP解禁!!!<gray>ボーダーが縮まります");
                    });
                if (players.size() == 2) {
                  speedUpBorder();
                }
              }
            }
            players.forEach(
                (t, u) -> {
                  int num = u.getFirst();
                  if (num == -1) {
                    return;
                  }
                  final Player p = Bukkit.getPlayer(t);
                  if (p == null) {
                    return;
                  }
                  p.sendActionBar(BukkitUtil.mm("酸素: " + num + " /" + AIR_LIMIT));
                  final boolean inCave = 7 > p.getLocation().getBlock().getLightFromSky();
                  if (!inCave && num == AIR_LIMIT) {
                    return;
                  }
                  val recent = Math.max(inCave ? --num : ++num, 0);
                  players.put(t, Pair.of(recent, u.getSecond()));
                  if (recent == 0) {
                    p.damage(1);
                  }
                });
            if (!isFinalArea && world.getWorldBorder().getSize() == MIN_BORDER_RANGE) {
              isFinalArea = true;
              runPlayers(p -> prefix.send(p, "<red>最終安置になりました！(これからはモブがわきません)"));
              world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
              clearMonster();
            }
          }
        };
    run.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
    world.setClearWeatherDuration(MAX_SECOND * 20);
    world.setTime(1000);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    clearMonster();
    val loc = lobby.clone();
    runPlayers(
        p -> {
          val uuid = p.getUniqueId();
          p.showBossBar(bossBar);
          BukkitUtil.title(p, "<blue>サバイバルスナイパー", "<red>スタート");
          val inv = p.getInventory();
          inv.clear();
          if (players.get(uuid).getFirst() == -1) {
            p.setGameMode(GameMode.SPECTATOR);
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
            teleportLobby(p);
            return;
          }
          ComMiniPlayer.getPlayer(uuid)
              .setHunger(true)
              .setHideNameTag(true)
              .getGamePlayerData(SSPlayer.class)
              .getInventorySlot()
              .setItems(inv);
          p.setSaturation(6);
          p.setGameMode(GameMode.SURVIVAL);
          if (!BukkitUtil.randomTeleport(p, loc, (MAX_RADIUS_RANGE / 2) - 10)) {
            p.teleport(world.getHighestBlockAt(loc, HeightMap.MOTION_BLOCKING).getLocation());
          }
        });
    if (mode == Mode.TEAM) {
      players.entrySet().stream()
          .filter(entry -> entry.getValue().getSecond() != null)
          .collect(Collectors.groupingBy(entry -> entry.getValue().getSecond()))
          .forEach(
              (color, entries) -> {
                val size = entries.size();
                for (int i = 0; i < size; i++) {
                  val current = Bukkit.getPlayer(entries.get(i).getKey());
                  current.playerListName(BukkitUtil.colorToComponent(color, current.getName()));
                  for (int j = 0; j < size; j++) {
                    if (i != j) {
                      try {
                        PluginLib.getGlowingEntities()
                            .setGlowing(Bukkit.getPlayer(entries.get(j).getKey()), current, color);
                      } catch (ReflectiveOperationException ignored) {
                      }
                    }
                  }
                }
              });
    }
  }

  @Override
  public void innerFinishGame() {
    val vec = lobby.toVector();
    val min = vec.clone().add(VOID_BLOCK_RADIUS);
    val max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    BukkitUtil.consoleCommand(
        String.format(
            "execute in %s run fill %s %s %s %s %s %s minecraft:air replace minecraft:barrier",
            "overworld",
            min.getBlockX(),
            min.getBlockY(),
            min.getBlockZ(),
            max.getBlockX(),
            max.getBlockY(),
            max.getBlockZ()));
    world.getWorldBorder().reset();
    if (bossBar != null) {
      runPlayers(p -> p.hideBossBar(bossBar));
    }
    if (run != null) {
      run.cancel();
    }
    if (mode == Mode.TEAM) {
      val keys = players.keySet().stream().map(Bukkit::getPlayer).toList();
      for (val current : keys) {
        for (val another : keys) {
          if (!current.equals(another)) {
            try {
              PluginLib.getGlowingEntities().unsetGlowing(another, current);
            } catch (ReflectiveOperationException ignored) {
            }
          }
        }
      }
    }
    players.clear();
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  protected boolean predicateSpec(Player player) {
    return true;
  }

  @Override
  public void innerAddSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, Pair.of(-1, null));
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    teleportLobby(player);
  }

  public void speedUpBorder() {
    if (!isCanPvP()) {
      return;
    }
    if (isFinalArea) {
      return;
    }
    val size = world.getWorldBorder().getSize();
    val speed = previousBorderSize / previousTime;
    val afterTime = (previousTime - ((previousBorderSize - size) / speed)) * (1.0 / speedRate);
    previousTime = (int) afterTime;
    previousBorderSize = size;
    world.getWorldBorder().setSize(MIN_BORDER_RANGE, (long) afterTime);
    runPlayers(p -> prefix.send(p, "<red>DANGER! ボーダーの速度が上がりました"));
  }

  public void clearMonster() {
    world
        .getNearbyLivingEntities(
            world.getWorldBorder().getCenter(),
            MIN_BORDER_RANGE + 50,
            320,
            MIN_BORDER_RANGE + 50,
            (entity) -> entity instanceof Monster)
        .forEach(LivingEntity::remove);
  }

  @Override
  protected void fieldInitialize(final boolean isCreatingInstance) {
    canPvP = false;
    lobby = null;
    world = null;
    bossBar = null;
    run = null;
    isFinalArea = false;
    previousBorderSize = MAX_RADIUS_RANGE;
    previousTime = MAX_SECOND;
    mode = Mode.FFA;
  }

  @Override
  public MenuHolder<ComMiniPlugin> createHelpMenu() {
    return new SSHelpMenu();
  }

  public enum Mode {
    FFA,
    TEAM
  }
}
