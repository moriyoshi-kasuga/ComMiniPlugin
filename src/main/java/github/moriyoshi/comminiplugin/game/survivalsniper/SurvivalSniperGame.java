package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.GamePlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SurvivalSniperGame extends AbstractGame {

  private static final String HELD_WORLD = "world";
  private static final int MAX_RADIUS_RANGE = 400;
  private static final int MAX_MINUTES = 60 * 20;
  private static final SurvivalSniperGame INSTANCE = new SurvivalSniperGame();

  public static SurvivalSniperGame getInstance() {
    return INSTANCE;
  }

  // true は生きている、falseは観戦者(死んで観戦者で機能を統一)
  public final HashMap<UUID, Pair<Boolean, Integer>> players = new HashMap<>();

  public final static int AIR_LIMIT = 120;
  private BukkitRunnable run;

  public final void joinPlayer(Player player, boolean isPlayer) {
    var uuid = player.getUniqueId();
    if (players.containsKey(uuid)) {
      if (players.get(uuid).getLeft() == isPlayer) {
        players.remove(uuid);
        prefix.send(player, "<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
        return;
      }
    }
    players.put(uuid, Pair.of(isPlayer, isPlayer ? AIR_LIMIT : -1));
    prefix.send(player, (isPlayer ? "<blue>参加" : "<gray>観戦") + "します");
  }

  protected SurvivalSniperGame() {
    super(
        "survivalsniper",
        "<blue>サバイバルスナイパー",
        "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
        Material.SPYGLASS,
        new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
        new SurvivalSniperListener());
  }

  @Override
  public MenuHolder<ComMiniPlugin> adminMenu() {
    return new SurvivalSniperAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> gameMenu(Player player) {
    return new SurvivalSniperMenu();
  }

  @Override
  public boolean initializeGame(Player player) {
    if (!player.getWorld().getName().equalsIgnoreCase(HELD_WORLD)) {
      prefix.send(player, "<red>オーバーワールドでのみ実行可能です");
      return false;
    }
    lobby = player.getLocation();
    lobby.getWorld().getWorldBorder().setCenter(lobby);
    lobby.getWorld().getWorldBorder().setSize((MAX_RADIUS_RANGE + 10) * 2);
    return true;
  }

  @Override
  public boolean startGame(Player player) {
    if (2 > players.values().stream().filter(b -> b.getLeft()).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }

    run = new BukkitRunnable() {
      @Override
      public void run() {
        players.forEach((t, u) -> {
          if (!u.getLeft()) {
            return;
          }
          Player p = Bukkit.getPlayer(t);
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
      }
    };

    run.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
    var world = lobby.getWorld();
    world.getWorldBorder().setSize(50, MAX_MINUTES);
    world.setTime(1000);
    var random = new Random();
    var loc = lobby.clone();
    var bx = loc.getBlockX();
    var bz = loc.getBlockZ();
    runPlayers(p -> {
      var uuid = p.getUniqueId();
      Util.title(p, "<blue>サバイバルスナイパー", "<red>スタート");
      if (!players.get(uuid).getLeft()) {
        p.setGameMode(GameMode.SPECTATOR);
        teleportLobby(p);
        return;
      }
      var inv = p.getInventory();
      inv.clear();
      inv.addItem(new Sniper().getItem(), new Jump().getItem());
      p.setGameMode(GameMode.SURVIVAL);
      GamePlayer.getPlayer(uuid).setHunger(true);
      int x = random.nextInt(-MAX_RADIUS_RANGE, MAX_RADIUS_RANGE);
      int z = random.nextInt(-MAX_RADIUS_RANGE, MAX_RADIUS_RANGE);
      var block = loc.getWorld().getHighestBlockAt(bx + x, bz + z,
          HeightMap.MOTION_BLOCKING_NO_LEAVES);
      p.teleport(block.getLocation().add(x > 0 ? 0.5 : -0.5, 1.0, z > 0 ? 0.5 : -0.5));
    });
    return true;
  }

  public void endGame(UUID winner) {
    var name = Bukkit.getPlayer(winner).getName();
    runPlayers(p -> {
      prefix.send(p, "<red><u>" + name + "</u>が勝ちました");
    });
    GameSystem.finalizeGame();
  }

  @Override
  public void finishGame() {
    run.cancel();
    players.clear();
    lobby = null;
    Bukkit.getWorld(HELD_WORLD).getWorldBorder().reset();
  }

  @Override
  public boolean isGamePlayer(Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  public boolean addSpec(Player player) {
    players.put(player.getUniqueId(), Pair.of(false, -1));
    return true;
  }
}
