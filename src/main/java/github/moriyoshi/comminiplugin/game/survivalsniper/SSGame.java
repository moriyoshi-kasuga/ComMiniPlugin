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
import github.moriyoshi.comminiplugin.util.tuple.Pair;
import java.util.HashMap;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

// TODO: チーム戦実装する
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
  public final HashMap<UUID, Pair<Boolean, Integer>> players = new HashMap<>();

  @Getter private boolean canPvP;
  private BossBar bossBar;
  private BukkitRunnable run;
  private boolean isFinalArea;

  public SSGame() {
    super(
        "survivalsniper",
        "<blue>サバイバルスナイパー",
        "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
        Material.SPYGLASS,
        new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
        new SSListener());
  }

  public final void joinPlayer(final Player player, final boolean isPlayer) {
    val uuid = player.getUniqueId();
    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    if (players.containsKey(uuid)) {
      if (players.get(uuid).getFirst() == isPlayer) {
        players.remove(uuid);
        GameSystem.initializePlayer(player);
        prefix.cast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
        return;
      }
    }
    players.put(uuid, Pair.of(isPlayer, isPlayer ? AIR_LIMIT : -1));
    player.teleport(lobby);
    player.getInventory().addItem(item);
    prefix.cast(player.getName() + "が" + (isPlayer ? "<blue>参加" : "<gray>観戦") + "します");
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new SSAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(final Player player) {
    return new SSMenu();
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
    if (2 > players.values().stream().filter(Pair::getFirst).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }
    // TODO: タイトルで5秒カウントする
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
          private int second = AFTER_PVP_SECOND;

          @Override
          public void run() {
            if (second != -1) {
              // TODO: ここで alert を出す
              if (second > 0) {
                bossBar
                    .name(Util.mm("<red>PvP解禁まで<u>" + second + "</u>秒"))
                    .progress((float) second / (float) AFTER_PVP_SECOND);
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
              second--;
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
                    players.put(t, Pair.of(true, inCave ? --num : ++num));
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
    hidePlayer();
    return true;
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
    showPlayer();
    players.clear();
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  public boolean addSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, Pair.of(false, -1));
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
            MIN_BORDER_RANGE,
            320,
            MIN_BORDER_RANGE,
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
  }
}
