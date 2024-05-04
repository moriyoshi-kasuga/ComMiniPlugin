package github.moriyoshi.comminiplugin.util;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;

/**
 * よく使うメゾットがある
 */

public class Util {

  private Util() {
  }

  public static void log(Object message) {
    Bukkit.getConsoleSender().sendMessage(Util.mm(message));
  }

  /**
   * Object を String に変換してから{@link Component}に変換します
   *
   * @param str 変換対象
   * @return 変換物
   */
  public static Component mm(Object str) {
    return str instanceof Component ? ((Component) str)
        : MiniMessage.miniMessage().deserialize(String.valueOf(str))
            .decoration(TextDecoration.ITALIC, false);
  }

  /**
   * コマンドをコンソールで実行
   *
   * @param command command string (no /)
   */

  public static void consoleCommand(String command) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
  }

  /**
   * send to uuid's player
   *
   * @param uuid player's uuid
   * @param str  send text
   */

  public static void send(UUID uuid, Object str) {
    send(Bukkit.getPlayer(uuid), str);
  }

  /**
   * send to sender
   *
   * @param sender sender
   * @param str    send text
   */
  public static void send(CommandSender sender, Object str) {
    if (sender != null) {
      sender.sendMessage(Util.mm(str));
    }
  }

  /**
   * execute command
   *
   * @param execute player
   * @param command command
   */
  public static void executeCommand(Player execute, String command) {
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

  public static Component legacy(String str) {
    return LegacyComponentSerializer.legacyAmpersand().deserializeOrNull(str);
  }

  /**
   * vanilla color convert
   *
   * @param str string
   * @return convert
   */

  public static String cc(String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }

  /**
   * listの内容をComponentに変換
   *
   * @param list 変換したいlist
   * @return List<Component>
   */

  public static List<Component> ListMM(@NotNull List<?> list) {
    return list.stream().map(Util::mm).collect(Collectors.toList());
  }

  /**
   * {@link Component} をStringに変換
   *
   * @param str 変換したいコンポーネント
   * @return 変換されたString
   */

  public static String serialize(Component str) {
    return MiniMessage.miniMessage().serializeOrNull(str);
  }

  /**
   * component <red> to &c
   *
   * @param str component
   * @return string
   */
  public static String serializeLegacy(Component str) {
    return LegacyComponentSerializer.legacyAmpersand().serializeOrNull(str);
  }

  /**
   * show title
   *
   * @param uuid     player uuid
   * @param title    title string
   * @param subtitle subtitle string
   */

  public static void title(UUID uuid, Object title, Object subtitle) {
    title(Bukkit.getPlayer(uuid), title, subtitle);
  }

  /**
   * show title
   *
   * @param player   player
   * @param title    title string
   * @param subtitle subtitle string
   */

  public static void title(Player player, Object title, Object subtitle) {
    if (player != null && player.isOnline()) {
      player.showTitle(Title.title(Util.mm(Objects.requireNonNullElse(title, "")),
          Util.mm(Objects.requireNonNullElse(subtitle, ""))));
    }
  }

  /**
   * show time of seconds title
   *
   * @param player   player
   * @param title    tile
   * @param subtitle subtitle
   * @param seconds  seconds
   */
  public static void title(Player player, Object title, Object subtitle, int seconds) {
    if (player != null && player.isOnline()) {
      player.showTitle(Title.title(Util.mm(Objects.requireNonNullElse(title, "")),
          Util.mm(Objects.requireNonNullElse(subtitle, "")),
          Title.Times.times(Duration.ZERO, Duration.ofSeconds(seconds), Duration.ZERO)));
    }
  }

  /**
   * BroadCast
   *
   * @param str message
   */

  public static void cast(Object str) {
    Bukkit.broadcast(Util.mm(str));
  }

  /**
   * send message to all op players
   *
   * @param str message
   */
  public static void important(Object str) {
    Bukkit.getOnlinePlayers().forEach((player) -> {
      if (player.isOp()) {
        player.sendMessage(mm(str));
      }
    });
  }

}
