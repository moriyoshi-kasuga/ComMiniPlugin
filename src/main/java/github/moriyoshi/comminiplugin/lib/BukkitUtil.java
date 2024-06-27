package github.moriyoshi.comminiplugin.lib;

import github.moriyoshi.comminiplugin.system.GameListener;
import io.papermc.paper.entity.TeleportFlag;
import java.awt.Color;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** よく使うメゾットがある */
@SuppressWarnings("deprecation")
public final class BukkitUtil {

  private BukkitUtil() {}

  public static void log(final Object message) {
    Bukkit.getConsoleSender().sendMessage(BukkitUtil.mm(message));
  }

  /**
   * Object を String に変換してから{@link Component}に変換します
   *
   * @param str 変換対象
   * @return 変換物
   */
  public static Component mm(final Object str) {
    return str instanceof Component
        ? ((Component) str)
        : MiniMessage.miniMessage()
            .deserialize(String.valueOf(str))
            .decoration(TextDecoration.ITALIC, false);
  }

  /**
   * コマンドをコンソールで実行
   *
   * @param command command string (no /)
   */
  public static void consoleCommand(final String command) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
  }

  /**
   * send to sender
   *
   * @param sender sender
   * @param str send text
   */
  public static void send(final Audience sender, final Object str) {
    if (sender != null) {
      sender.sendMessage(BukkitUtil.mm(str));
    }
  }

  /**
   * send to sender
   *
   * @param sender sender
   * @param str send text
   */
  public static void send(final Audience sender, final Object... objects) {
    if (sender != null) {
      sender.sendMessage(
          mm(String.join(",", Arrays.stream(objects).map(String::valueOf).toList())));
    }
  }

  /**
   * execute command
   *
   * @param execute player
   * @param command command
   */
  public static void executeCommand(final Player execute, final String command) {
    if (execute.isOp()) {
      execute.performCommand(command);
    } else {
      try {
        execute.setOp(true);
        execute.performCommand(command);
      } finally {
        execute.setOp(false);
      }
    }
  }

  /**
   * &c to component
   *
   * @param str string
   * @return component
   */
  public static Component legacy(final String str) {
    return LegacyComponentSerializer.legacyAmpersand().deserializeOrNull(str);
  }

  /**
   * vanilla color convert
   *
   * @param str string
   * @return convert
   */
  public static String cc(final String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }

  /**
   * listの内容をComponentに変換
   *
   * @param list 変換したいlist
   * @return List<Component>
   */
  public static List<Component> ListMM(@NotNull final List<?> list) {
    return list.stream().map(BukkitUtil::mm).collect(Collectors.toList());
  }

  /**
   * {@link Component} をStringに変換
   *
   * @param str 変換したいコンポーネント
   * @return 変換されたString
   */
  public static String serialize(final Component str) {
    return MiniMessage.miniMessage().serializeOrNull(str);
  }

  /**
   * component <red> to &c
   *
   * @param str component
   * @return string
   */
  public static String serializeLegacy(final Component str) {
    return LegacyComponentSerializer.legacyAmpersand().serializeOrNull(str);
  }

  public static String chatColorToHex(final ChatColor color) {
    return colorToHex(color.asBungee().getColor());
  }

  public static String colorToHex(final Color color) {
    return Integer.toHexString(color.getRGB()).substring(2);
  }

  /**
   * show title
   *
   * @param player player
   * @param title title string
   * @param subtitle subtitle string
   */
  public static void title(final Player player, final Object title, final Object subtitle) {
    if (player != null && player.isOnline()) {
      player.showTitle(
          Title.title(
              BukkitUtil.mm(Objects.requireNonNullElse(title, "")),
              BukkitUtil.mm(Objects.requireNonNullElse(subtitle, ""))));
    }
  }

  /**
   * show time of seconds title
   *
   * @param player player
   * @param title tile
   * @param subtitle subtitle
   * @param seconds seconds
   */
  public static void title(
      final Player player, final Object title, final Object subtitle, final int seconds) {
    if (player != null && player.isOnline()) {
      player.showTitle(
          Title.title(
              BukkitUtil.mm(Objects.requireNonNullElse(title, "")),
              BukkitUtil.mm(Objects.requireNonNullElse(subtitle, "")),
              Title.Times.times(Duration.ZERO, Duration.ofSeconds(seconds), Duration.ZERO)));
    }
  }

  /**
   * BroadCast
   *
   * @param str message
   */
  public static void broadCast(final Object str) {
    Bukkit.broadcast(BukkitUtil.mm(str));
  }

  public static void broadCast(final Object... objects) {
    Bukkit.broadcast(
        BukkitUtil.mm(
            String.join(",", java.util.Arrays.stream(objects).map(String::valueOf).toList())));
  }

  /**
   * send message to all op players
   *
   * @param str message
   */
  public static void important(final Object str) {
    val message = mm(str);
    Bukkit.getOnlinePlayers()
        .forEach(
            (player) -> {
              if (player.isOp()) {
                player.sendMessage(message);
              }
            });
  }

  public static final BlockFace[] axis = {
    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
  };

  private static final Map<UUID, Entity> fallingBlocks = new HashMap<>();

  public static List<Player> getSquarePlayers(final BoundingBox square) {

    return Bukkit.getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector()))
        .toList();
  }

  public static void disableMove(final Player player, final int tick) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, tick, 138, true, false));
  }

  public static BlockFace convertYawToBlockFace(final float yaw) {
    return axis[Math.round(yaw / 90f) & 0x3];
  }

  public static float convertBlockFaceToYaw(final BlockFace face) {
    val first = face.name().split("_")[0];
    return switch (first) {
      case "NORTH" -> 180;
      case "WEST" -> 90;
      case "EAST" -> -90;
      default -> 0;
    };
  }

  public static void clear() {
    fallingBlocks
        .values()
        .forEach(
            falling -> {
              if (!falling.isDead()) {
                falling.remove();
              }
            });
    fallingBlocks.clear();
  }

  public static void removeFalling(final UUID uuid) {
    val temp = fallingBlocks.get(uuid);
    if (temp != null) {
      temp.remove();
    }
  }

  public static boolean isFalling(final UUID uuid) {
    return fallingBlocks.containsKey(uuid);
  }

  public static void setVelocity(
      final Player player, final Vector velocity, final JumpState state) {
    val uuid = player.getUniqueId();
    val loc = player.getLocation();
    val falling =
        loc.getWorld()
            .spawn(
                loc,
                Snowball.class,
                entity -> {
                  entity.setInvisible(true);
                  entity.setInvulnerable(true);
                  entity.setSilent(true);
                  entity.setGravity(true);
                  entity.setVelocity(velocity);
                  entity.setVisibleByDefault(false);
                });
    GameListener.addProjectileHitListener(
        falling.getUniqueId(),
        (projectile, event) -> {
          event.setCancelled(true);
        });

    val temp = fallingBlocks.put(player.getUniqueId(), falling);
    if (temp != null) {
      temp.remove();
    }
    switch (state) {
      case FREE ->
          new BukkitRunnable() {

            private int rest = 3;

            @Override
            public void run() {
              if (falling.isDead() || 0 >= --rest) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(PluginLib.getPlugin(), 0, 1);
      case DOWN ->
          new BukkitRunnable() {

            @Override
            public void run() {
              if (falling.isDead() || 0 >= falling.getVelocity().getY()) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(PluginLib.getPlugin(), 0, 1);
      case FIXED ->
          new BukkitRunnable() {

            @Override
            public void run() {
              if (falling.isDead()) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(PluginLib.getPlugin(), 0, 1);
    }
  }

  public static Boolean teleportOnTheBlock(final Block block, final Entity entity) {
    return entity.teleport(
        block.getLocation().add(0.5, 1, 0.5),
        TeleportCause.PLUGIN,
        TeleportFlag.Relative.YAW,
        TeleportFlag.Relative.PITCH);
  }
}
