package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.gametype.WinnerTypeGame;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import github.moriyoshi.comminiplugin.util.tuple.Triple;
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

  private final int MAX_RADIUS_RANGE = 600;
  private final int MIN_BORDER_RANGE = 50;
  private final int MAX_SECOND = 60 * 10;
  private final int AFTER_PVP_SECOND = 60 * 5;
  private final int AIR_LIMIT = 60 * 3;
  private final Vector VOID_BLOCK_RADIUS = new Vector(3, 3, 3);

  private final double speedRate = 1.25;
  private long previousTime;
  private double previousBorderSize;

  // true は生きている、falseは観戦者(死んで観戦者で機能を統一)
  public final HashMap<UUID, Triple<Boolean, Integer, ChatColor>> players = new HashMap<>();

  @Getter private boolean canPvP;
  private BossBar bossBar;
  private BukkitRunnable run;
  private boolean isFinalArea;

  @Getter private Mode mode;

  public void setMode(Mode mode) {
    this.mode = mode;
    prefix.cast("<red>Modeが<u>" + mode.name() + "</u>に変わりました");
    prefix.cast("<gray>ゲームをしたい人はもう一度参加ボタンを押してください");
    runPlayers(
        player -> {
          GameSystem.initializePlayer(player);
        });
    players.clear();
  }

  public SSGame() {
    super(
        "survivalsniper",
        "<blue>サバイバルスナイパー",
        "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
        Material.SPYGLASS,
        new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
        new SSListener());
  }

  public final void leavePlayer(final Player player) {
    val uuid = player.getUniqueId();
    if (!players.containsKey(uuid)) {
      prefix.send(player, "<red>あなたはゲームに参加していません");
      return;
    }
    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    GameSystem.initializePlayer(player);
    val isPlayer = players.remove(uuid).getFirst();
    prefix.cast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
  }

  public final void joinPlayer(final Player player, final boolean isPlayer, final ChatColor color) {
    val prev = players.get(player.getUniqueId());
    if (prev != null && prev.getFirst() == isPlayer && prev.getThird() == color) {
      prefix.send(player, "<red>抜けるには抜けるボタンを押してください");
      return;
    }
    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    players.put(player.getUniqueId(), Triple.of(isPlayer, isPlayer ? AIR_LIMIT : -1, color));
    player.teleport(lobby);
    player.getInventory().addItem(item);
    if (isPlayer) {
      if (color == null) {
        prefix.cast(player.getName() + "が<blue>参加します");
      } else {
        prefix.cast(
            Util.mm(player.getName() + "が<white>")
                .append(Util.colorToComponent(color, color.name()))
                .append(Util.mm("<gray>に<blue>参加します")));
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
    Util.consoleCommand(
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
  public boolean innerStartGame(final Player player) {
    if (mode == Mode.FFA) {
      if (2 > players.values().stream().filter(Triple::getFirst).toList().size()) {
        prefix.send(player, "<red>二人以上でしかプレイできません");
        return false;
      }
    } else {
      if (2
          > players.values().stream()
              .filter(value -> value.getThird() != null)
              .collect(Collectors.groupingBy(Triple::getThird))
              .size()) {
        prefix.send(player, "<red>二チーム以上でしかプレイできません");
        return false;
      }
    }
    new BukkitRunnable() {

      private int rest = 11;

      @Override
      public void run() {
        if (1 > --rest) {
          runPlayers(
              p -> {
                p.playSound(
                    p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1);
                Util.title(p, "<red>スタート!", null);
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
        runPlayers(
            p -> {
              Util.title(p, "<red><u>" + rest + "</u>秒後に始まります", null);
            });
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
    return true;
  }

  private void start() {
    val vec = lobby.toVector();
    val min = vec.clone().add(VOID_BLOCK_RADIUS);
    val max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    Util.consoleCommand(
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
            Util.mm("<red>PvP解禁まで<u>" + AFTER_PVP_SECOND + "</u>秒"),
            1f,
            BossBar.Color.RED,
            BossBar.Overlay.NOTCHED_10);

    run =
        new BukkitRunnable() {
          private int second = AFTER_PVP_SECOND + 1;

          @Override
          public void run() {
            if (second != -1) {
              if (--second > 0) {
                bossBar
                    .name(Util.mm("<red>PvP解禁まで<u>" + second + "</u>秒"))
                    .progress((float) second / (float) AFTER_PVP_SECOND);
                if (second % 60 == 0) {
                  val message = Util.mm("<red>PvP解禁まで<u>" + second / 60 + "</u>分");
                  runPlayers(
                      p -> {
                        prefix.send(p, message);
                      });
                }
                if (10 >= second) {
                  runPlayers(
                      p -> {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                      });
                }
              } else {
                canPvP = true;
                runPlayers(
                    p -> {
                      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
                      p.hideBossBar(bossBar);
                      prefix.send(p, "<red>PvP解禁!!!");
                    });
                if (players.size() == 2) {
                  speedUpBorder();
                }
              }
            }
            players.forEach(
                (t, u) -> {
                  if (!u.getFirst()) {
                    return;
                  }
                  final Player p = Bukkit.getPlayer(t);
                  if (p == null) {
                    return;
                  }
                  int num = u.getSecond();
                  p.sendActionBar(Util.mm("酸素: " + num + " /" + AIR_LIMIT));
                  final boolean inCave = 7 > p.getLocation().getBlock().getLightFromSky();
                  if (!inCave && num == AIR_LIMIT) {
                    return;
                  }
                  if (num > 0) {
                    players.put(t, Triple.of(true, inCave ? --num : ++num, u.getThird()));
                    return;
                  }
                  p.setHealth(0);
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
    world.getWorldBorder().setSize(MIN_BORDER_RANGE, MAX_SECOND);
    world.setClearWeatherDuration(MAX_SECOND * 20);
    world.setTime(1000);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    clearMonster();
    val loc = lobby.clone();
    runPlayers(
        p -> {
          val uuid = p.getUniqueId();
          p.showBossBar(bossBar);
          Util.title(p, "<blue>サバイバルスナイパー", "<red>スタート");
          val inv = p.getInventory();
          inv.clear();
          if (!players.get(uuid).getFirst()) {
            p.setGameMode(GameMode.SPECTATOR);
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
            teleportLobby(p);
            return;
          }
          val gamePlayer = ComMiniPlayer.getPlayer(uuid);
          gamePlayer.setHunger(true);
          gamePlayer.setHideNameTag(true);
          gamePlayer.getGamePlayerData(SSPlayer.class).getHotbar().setItems(inv);
          p.setSaturation(6);
          p.setGameMode(GameMode.SURVIVAL);
          if (!BukkitUtil.randomTeleport(p, loc, (MAX_RADIUS_RANGE / 2) - 10)) {
            p.teleport(world.getHighestBlockAt(loc, HeightMap.MOTION_BLOCKING).getLocation());
          }
        });
    if (mode == Mode.TEAM) {
      players.entrySet().stream()
          .filter(entry -> entry.getValue().getThird() != null)
          .collect(Collectors.groupingBy(entry -> entry.getValue().getThird()))
          .forEach(
              (color, entries) -> {
                val size = entries.size();
                for (int i = 0; i < size; i++) {
                  val current = Bukkit.getPlayer(entries.get(i).getKey());
                  current.playerListName(Util.colorToComponent(color, current.getName()));
                  for (int j = 0; j < size; j++) {
                    if (i != j) {
                      try {
                        ComMiniPlugin.getGlowingEntities()
                            .setGlowing(Bukkit.getPlayer(entries.get(j).getKey()), current, color);
                      } catch (ReflectiveOperationException e) {
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
    Util.consoleCommand(
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
      players.entrySet().stream()
          .filter(entry -> entry.getValue().getThird() != null)
          .collect(Collectors.groupingBy(entry -> entry.getValue().getThird()))
          .forEach(
              (color, entries) -> {
                val size = entries.size();
                for (int i = 0; i < size; i++) {
                  Player current = Bukkit.getPlayer(entries.get(i).getKey());
                  current.playerListName(Util.mm(current.getName()));
                  for (int j = 0; j < size; j++) {
                    if (i != j) {
                      try {
                        // TODO: ここ network protocol error が起こる
                        ComMiniPlugin.getGlowingEntities()
                            .unsetGlowing(Bukkit.getPlayer(entries.get(j).getKey()), current);
                      } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                      }
                    }
                  }
                }
              });
    }
    players.clear();
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  public boolean addSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, Triple.of(false, -1, null));
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    teleportLobby(player);
    return true;
  }

  public void speedUpBorder() {
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

  public enum Mode {
    FFA,
    TEAM
  }
}
