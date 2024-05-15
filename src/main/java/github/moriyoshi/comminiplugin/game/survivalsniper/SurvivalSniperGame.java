package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.game.AbstractGame;
import github.moriyoshi.comminiplugin.system.game.GamePlayer;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;

public class SurvivalSniperGame extends AbstractGame {

  private final int MAX_RADIUS_RANGE = 300;
  private final int MIN_BORDER_RANGE = 50;
  private final int MAX_SECOND = 60 * 10;
  private final int AFTER_PVP_SECOND = 60 * 5;
  private final int AIR_LIMIT = 60 * 3;
  private final Vector VOID_BLOCK_RADIUS = new Vector(3, 3, 3);

  // true は生きている、falseは観戦者(死んで観戦者で機能を統一)
  public final HashMap<UUID, Pair<Boolean, Integer>> players = new HashMap<>();

  @Getter
  private boolean canPvP = false;
  private BossBar bossBar = null;
  private BukkitRunnable run = null;
  private boolean isFinalArea = false;

  public SurvivalSniperGame() {
    super(
        "survivalsniper",
        "<blue>サバイバルスナイパー",
        "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
        Material.SPYGLASS,
        new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
        new SurvivalSniperListener());
  }

  public final void joinPlayer(Player player, boolean isPlayer) {
    if (isStarted()) {
      return;
    }
    var uuid = player.getUniqueId();
    player.getInventory().removeItem(new ItemStack(Material.SPYGLASS));
    if (players.containsKey(uuid)) {
      if (players.get(uuid).getLeft() == isPlayer) {
        players.remove(uuid);
        var text = player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ";
        GameSystem.initializePlayer(player);
        player.teleport(ComMiniWorld.LOBBY);
        if (isPlayer) {
          prefix.cast(text);
        } else {
          prefix.send(player, text);
        }
        return;
      }
    }
    players.put(uuid, Pair.of(isPlayer, isPlayer ? AIR_LIMIT : -1));
    player.teleport(lobby);
    player.getInventory().addItem(new ItemStack(Material.SPYGLASS));
    var text = player.getName() + "が" + (isPlayer ? "<blue>参加" : "<gray>観戦") + "します";
    if (isPlayer) {
      prefix.cast(text);
    } else {
      prefix.send(player, text);
    }
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new SurvivalSniperAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(Player player) {
    return new SurvivalSniperMenu();
  }

  @Override
  public boolean initializeGame(Player player) {
    if (!player.getWorld().getName().equalsIgnoreCase("world")) {
      prefix.send(player, "<red>オーバーワールドでのみ実行可能です");
      return false;
    }
    var temp = player.getLocation().clone();
    world = temp.getWorld();
    lobby = world.getHighestBlockAt(temp).getLocation().add(new Vector(0, 50, 0));
    world.getWorldBorder().setCenter(lobby);
    world.getWorldBorder().setSize(MAX_RADIUS_RANGE * 2);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    world.setClearWeatherDuration(0);
    world.setTime(1000);
    var vec = lobby.toVector();
    var min = vec.clone().add(VOID_BLOCK_RADIUS);
    var max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    Util.consoleCommand(
        String.format("execute in %s run fill %s %s %s %s %s %s minecraft:barrier outline",
            "overworld",
            min.getBlockX(), min.getBlockY(),
            min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ()));
    Util.consoleCommand("chunky worldborder");
    Util.consoleCommand("chunky start");
    return true;
  }

  @Override
  public boolean innerStartGame(Player player) {
    if (2 > players.values().stream().filter(Pair::getLeft).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }
    var vec = lobby.toVector();
    var min = vec.clone().add(VOID_BLOCK_RADIUS);
    var max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    Util.consoleCommand(
        String.format(
            "execute in %s run fill %s %s %s %s %s %s minecraft:air replace minecraft:barrier",
            "overworld", min.getBlockX(), min.getBlockY(), min.getBlockZ(),
            max.getBlockX(), max.getBlockY(), max.getBlockZ()));

    bossBar = BossBar.bossBar(Util.mm("<red>PvP解禁まで<u>" + AFTER_PVP_SECOND + "</u>秒"), 1f,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_10);

    run = new BukkitRunnable() {
      private int second = AFTER_PVP_SECOND;

      @Override
      public void run() {
        if (second != -1) {
          if (second > 0) {
            bossBar.name(Util.mm("<red>PvP解禁まで<u>" + second + "</u>秒"))
                .progress((float) second / (float) AFTER_PVP_SECOND);
          } else {
            canPvP = true;
            runPlayers(p -> {
              p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
              p.hideBossBar(bossBar);
              prefix.send(p, "<red>PvP解禁!!!");
            });
            if (players.size() == 2) {
              setBorder();
            }
          }
          second--;
        }
        players.forEach((t, u) -> {
          if (!u.getLeft()) {
            return;
          }
          Player p = Bukkit.getPlayer(t);
          if (p == null) {
            return;
          }
          int num = u.getRight();
          p.sendActionBar(Util.mm("酸素: " + num + " /" + AIR_LIMIT));
          boolean inCave = 7 > p.getLocation().getBlock().getLightFromSky();
          if (!inCave && num == AIR_LIMIT) {
            return;
          }
          if (num > 0) {
            players.put(t, Pair.of(true, inCave ? --num : ++num));
            return;
          }
          p.setHealth(0);
        });
        if (!isFinalArea && world.getWorldBorder().getSize() == MIN_BORDER_RANGE) {
          isFinalArea = true;
          runPlayers(p -> prefix.send(p, "<red>最終安置になりました！(これからはモブがわきません)"));
          world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
          world.getNearbyLivingEntities(world.getWorldBorder().getCenter(), MIN_BORDER_RANGE, 320, MIN_BORDER_RANGE)
              .forEach(entity -> {
                if (entity instanceof Player || entity instanceof Item) {
                  return;
                }
                if (entity instanceof Monster) {
                  entity.remove();
                }
              });
        }
      }
    };

    run.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
    world.getWorldBorder().setSize(MIN_BORDER_RANGE, MAX_SECOND);
    world.setClearWeatherDuration(MAX_SECOND * 20);
    world.setTime(1000);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    world.getNearbyLivingEntities(world.getWorldBorder().getCenter(), MIN_BORDER_RANGE, 320, MIN_BORDER_RANGE)
        .forEach(entity -> {
          if (entity instanceof Player || entity instanceof Item) {
            return;
          }
          if (entity instanceof Monster) {
            entity.remove();
          }
        });
    var loc = lobby.clone();
    runPlayers(p -> {
      var uuid = p.getUniqueId();
      p.showBossBar(bossBar);
      Util.title(p, "<blue>サバイバルスナイパー", "<red>スタート");
      var inv = p.getInventory();
      inv.clear();
      if (!players.get(uuid).getLeft()) {
        p.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
        teleportLobby(p);
        return;
      }
      var gamePlayer = GamePlayer.getPlayer(uuid);
      gamePlayer.setHunger(true);
      gamePlayer.setHideNameTag(true);
      var i = 0;
      for (var item : gamePlayer.getSurvivapsniperSlot().toItemStacks()) {
        inv.setItem(i, item);
        i++;
      }
      p.setSaturation(6);
      p.setGameMode(GameMode.SURVIVAL);
      if (!BukkitUtil.randomTeleport(p, loc, MAX_RADIUS_RANGE - 10)) {
        p.teleport(world.getHighestBlockAt(loc, HeightMap.MOTION_BLOCKING).getLocation());
      }
    });
    hidePlayer();
    return true;
  }

  private int previousTime = MAX_SECOND;
  private double previousWidth = MAX_RADIUS_RANGE * 2;

  public void endGame(UUID winner) {
    var name = Bukkit.getPlayer(winner).getName();
    runPlayers(p -> prefix.send(p, "<red><u>" + name + "</u>が勝ちました"));
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.finalizeGame();
      }

    }.runTaskLater(ComMiniPlugin.getPlugin(), 100);
  }

  @Override
  public void innerFinishGame() {
    var vec = lobby.toVector();
    var min = vec.clone().add(VOID_BLOCK_RADIUS);
    var max = vec.clone().subtract(VOID_BLOCK_RADIUS);
    Util.consoleCommand(
        String.format(
            "execute in %s run fill %s %s %s %s %s %s minecraft:air replace minecraft:barrier",
            "overworld",
            min.getBlockX(),
            min.getBlockY(),
            min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ()));

    if (run != null) {
      run.cancel();
      run = null;
    }
    if (bossBar != null) {
      runPlayers(p -> p.hideBossBar(bossBar));
      bossBar = null;
    }
    canPvP = false;
    lobby = null;
    world.getWorldBorder().reset();
    isFinalArea = false;
    previousWidth = MAX_RADIUS_RANGE * 2;
    previousTime = MAX_SECOND;
    showPlayer();
    players.clear();
  }

  @Override
  public boolean isGamePlayer(Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  public boolean addSpec(Player player) {
    var uuid = player.getUniqueId();
    players.put(uuid, Pair.of(false, -1));
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
    teleportLobby(player);
    return true;
  }

  public void setBorder() {
    if (isFinalArea) {
      return;
    }
    double size = world.getWorldBorder().getSize();
    double speed = previousWidth / previousTime;
    double afterTime = previousTime - ((previousWidth - size) / speed) * 0.8;
    previousTime = (int) afterTime;
    previousWidth = size;
    world.getWorldBorder().setSize(size, (long) afterTime);
    runPlayers(p -> prefix.send(p, "<red>DANGER! ボーダーの速度が上がりました"));
  }

}
