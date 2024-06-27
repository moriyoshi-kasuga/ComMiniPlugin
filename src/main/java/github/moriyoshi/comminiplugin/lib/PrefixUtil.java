package github.moriyoshi.comminiplugin.lib;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

/** {@link #prefix} を先頭につけメッセージを送信するUtil */
public class PrefixUtil {

  private final Component prefix;
  private final Component loggerPrefix;

  public PrefixUtil(final Object prefix) {
    this(prefix, prefix);
  }

  public PrefixUtil(final Object prefix, final Object loggerPrefix) {
    this.prefix = BukkitUtil.mm(prefix);
    this.loggerPrefix = BukkitUtil.mm(loggerPrefix);
  }

  /**
   * consoleにmessageを送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logInfo(final Object message) {
    log(getLoggerPrefix().append(BukkitUtil.mm(message)));
  }

  /**
   * consoleに[prefix + "DEBUG" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logWarn(final Object message) {
    log(loggerPrefix.append(getWarn()).append(BukkitUtil.mm(message)));
  }

  /**
   * consoleに[prefix + "TRACE" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logTrace(final Object message) {
    log(loggerPrefix.append(getTrace()).append(BukkitUtil.mm(message)));
  }

  /**
   * consoleに[prefix + "DEBUG" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logDebug(final Object message) {
    log(loggerPrefix.append(getDebug()).append(BukkitUtil.mm(message)));
  }

  /**
   * consoleに[prefix + "ERROR" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logError(final Object message) {
    log(loggerPrefix.append(getError()).append(BukkitUtil.mm(message)));
  }

  public final void send(final Audience sender, final Object str) {
    sender.sendMessage(BukkitUtil.mm(getPrefix()).append(BukkitUtil.mm(str)));
  }

  public final void broadCast(final Object str) {
    Bukkit.broadcast(getPrefix().append(BukkitUtil.mm(str)));
  }

  public final void important(final Object str) {
    BukkitUtil.important(BukkitUtil.mm(getPrefix()).append(BukkitUtil.mm(str)));
  }

  public final Component getPrefix() {
    return prefix;
  }

  public final Component getLoggerPrefix() {
    return loggerPrefix;
  }

  /**
   * consoleにmessageを送信します
   *
   * @param message 送信するメッセージ
   */
  protected final void log(final Object message) {
    Bukkit.getConsoleSender().sendMessage(BukkitUtil.mm(message));
  }

  protected Component getWarn() {
    return BukkitUtil.mm(" <reset><yellow>[WARN]<reset> ");
  }

  protected Component getTrace() {
    return BukkitUtil.mm(" <reset><white>[TRACE]<reset> ");
  }

  protected Component getDebug() {
    return BukkitUtil.mm(" <reset><gray>[DEBUG]<reset> ");
  }

  protected Component getError() {
    return BukkitUtil.mm(" <reset><red>[ERROR]<reset> ");
  }
}
