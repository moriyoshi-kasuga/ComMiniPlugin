package github.moriyoshi.comminiplugin.biggame.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.BukkitRandomUtil;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import github.moriyoshi.comminiplugin.system.AbstractBigGame;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.IGameListener;
import github.moriyoshi.comminiplugin.system.type.ISpectatorGame;
import github.moriyoshi.comminiplugin.system.type.IWinnerTypeBigGame;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class SSBigGame extends AbstractBigGame implements IWinnerTypeBigGame, ISpectatorGame {

  public enum Mode {
    FFA,
    TEAM
  }

  public final HashMap<UUID, Pair<Integer, ChatColor>> players = new HashMap<>();
  private final int MAX_RADIUS_RANGE = 600;
  private final int MIN_BORDER_RANGE = 30;
  private final int MAX_SECOND = 60 * 7;
  private final int AFTER_PVP_SECOND = 60 * 5;
  public final int AIR_LIMIT = 60 * 3;
  private final Vector VOID_BLOCK_RADIUS = new Vector(3, 3, 3);
  private final double speedRate = 1.25;
  private long previousTime;
  private double previousBorderSize;
  @Getter private boolean canPvP;
  private BossBar bossBar;
  private BukkitRunnable run;

  private boolean isFinalArea;

  private BukkitRandomUtil random;

  private final ArrayDeque<CompletableFuture<Block>> teleportBlocks = new ArrayDeque<>();

  @Getter private Mode mode;

  public SSBigGame(
      Material icon,
      String id,
      String name,
      String description,
      Player player,
      PrefixUtil prefixUtil,
      Function<IdentifierKey, IGameListener<?>> listener)
      throws GameInitializeFailedException {
    super(icon, id, name, description, player, prefixUtil, listener);
    world = player.getWorld();
    lobby = world.getHighestBlockAt(player.getLocation()).getLocation().add(new Vector(0, 50, 0));
    random = new BukkitRandomUtil(lobby, (MAX_RADIUS_RANGE / 2) - 10).setMaxTry(500);
    world.getWorldBorder().setCenter(lobby);
    world.getWorldBorder().setSize(MAX_RADIUS_RANGE);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    world.setClearWeatherDuration(0);
    world.setTime(1000);
    world
        .getNearbyEntities(
            world.getWorldBorder().getCenter(),
            MAX_RADIUS_RANGE + 50,
            320,
            MAX_RADIUS_RANGE + 50,
            entity -> entity instanceof Monster || entity instanceof Item)
        .forEach(Entity::remove);
    setBarrier();

    canPvP = false;
    lobby = null;
    bossBar = null;
    run = null;
    isFinalArea = false;
    previousBorderSize = MAX_RADIUS_RANGE;
    previousTime = MAX_SECOND;
    mode = Mode.FFA;
  }

  @Override
  public void predicateInnerInitialize(@NotNull Player player)
      throws GameInitializeFailedException {
    if (!player.getWorld().equals(Bukkit.getWorld("world"))) {
      throw new GameInitializeFailedException("<red>オーバーワールド内でしかこのゲームはプレイできません");
    }
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    prefix.broadCast("<red>Modeが<u>" + mode.name() + "</u>に変わりました");
    prefix.broadCast("<gray>ゲームをしたい人はもう一度参加ボタンを押してください");
    runPlayers(GameSystem::initializePlayer);
    players.clear();
  }

  @Override
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
    GameSystem.initializePlayer(player);
    val isPlayer = players.remove(uuid).getFirst() != -1;
    prefix.broadCast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
    val temp = teleportBlocks.removeLast();
    if (temp != null && !temp.isDone()) {
      temp.cancel(true);
    }
  }

  public final void joinPlayer(final Player player, final boolean isPlayer, final ChatColor color) {
    val prev = players.get(player.getUniqueId());
    if (prev != null && (prev.getFirst() != -1) == isPlayer && prev.getSecond() == color) {
      prefix.send(player, "<red>抜けるには抜けるボタンを押してください");
      return;
    }

    teleportBlocks.addLast(random.randomTopBlock());

    val item =
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .build();
    player.getInventory().removeItem(item);
    players.put(player.getUniqueId(), Pair.of(isPlayer ? AIR_LIMIT : -1, color));
    player.teleport(getLobby());
    player.getInventory().addItem(item);
    hidePlayer(player);
    if (isPlayer) {
      if (color == null) {
        prefix.broadCast(player.getName() + "が<blue>参加します");
      } else {
        prefix.broadCast(
            player.getName()
                + "が<#"
                + BukkitUtil.chatColorToHex(color)
                + ">"
                + color.name()
                + "<gray>に<blue>参加します");
      }
    } else {
      prefix.broadCast(player.getName() + "が<gray>観戦します");
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
  public boolean predicateStartGame(final Audience player) {
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

  @Override
  public void innerFinishGame() {
    teleportBlocks.clear();
    removeBarrier();
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
  public void innerAddSpec(final Player player) {
    player.teleport(getLobby());
    setSpec(player);
  }

  public void setSpec(final Player player) {
    players.put(player.getUniqueId(), Pair.of(-1, null));
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    players.forEach(
        (uuid, pair) -> {
          val target = Bukkit.getPlayer(uuid);
          val color = pair.getSecond();
          try {
            if (color == null) {
              if (pair.getFirst() == -1) {
                PluginLib.getGlowingEntities().unsetGlowing(target, player);
              } else {
                PluginLib.getGlowingEntities().setGlowing(target, player, ChatColor.WHITE);
              }
            } else {
              PluginLib.getGlowingEntities().setGlowing(target, player, color);
            }
          } catch (ReflectiveOperationException e) {
          }
        });
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

  @Override
  public MenuHolder<ComMiniPlugin> createHelpMenu() {
    return new SSHelpMenu();
  }

  @Override
  public boolean predicateSpec(Player player) {
    return true;
  }

  private final void setBarrier() {

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
  }

  private void start() {
    removeBarrier();

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
              world
                  .getNearbyEntities(
                      world.getWorldBorder().getCenter(),
                      MIN_BORDER_RANGE + 50,
                      320,
                      MIN_BORDER_RANGE + 50,
                      (entity) -> entity instanceof Monster)
                  .forEach(Entity::remove);
            }
          }
        };
    run.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
    world.setClearWeatherDuration(MAX_SECOND * 20);
    world.setTime(1000);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
    val last = world.getHighestBlockAt(lobby, HeightMap.MOTION_BLOCKING).getLocation();
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
            p.teleport(getLobby());
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
          val future = teleportBlocks.removeFirst();
          if (future == null) {
            p.teleport(last);
            return;
          }
          if (future.isDone()) {
            BukkitUtil.teleportOnTheBlock(future.join(), p);
          } else {
            future.cancel(true);
          }
        });
    teleportBlocks.clear();
    if (mode == Mode.TEAM) {
      players.entrySet().stream()
          .filter(entry -> entry.getValue().getSecond() != null)
          .collect(Collectors.groupingBy(entry -> entry.getValue().getSecond()))
          .forEach(
              (color, entries) -> {
                val size = entries.size();
                for (int i = 0; i < size; i++) {
                  val current = Bukkit.getPlayer(entries.get(i).getKey());
                  current.playerListName(
                      BukkitUtil.mm(
                          "<#" + BukkitUtil.chatColorToHex(color) + ">" + current.getName()));
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

  private final void removeBarrier() {
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
  }
}
