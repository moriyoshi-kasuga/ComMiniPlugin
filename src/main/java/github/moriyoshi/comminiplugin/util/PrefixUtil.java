package github.moriyoshi.comminiplugin.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** {@link #prefix} を先頭につけメッセージを送信するUtil */
public class PrefixUtil {

  private final Component prefix;
  private final Component loggerPrefix;

  public PrefixUtil(final Object prefix) {
    this(prefix, prefix);
  }

  public PrefixUtil(final Object prefix, final Object loggerPrefix) {
    this.prefix = Util.mm(prefix);
    this.loggerPrefix = Util.mm(loggerPrefix);
  }

  /**
   * consoleにmessageを送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logInfo(final Object message) {
    log(getLoggerPrefix().append(Util.mm(message)));
  }

  /**
   * consoleに[prefix + "DEBUG" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logWarn(final Object message) {
    log(loggerPrefix.append(getWarn()).append(Util.mm(message)));
  }

  /**
   * consoleに[prefix + "TRACE" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logTrace(final Object message) {
    log(loggerPrefix.append(getTrace()).append(Util.mm(message)));
  }

  /**
   * consoleに[prefix + "DEBUG" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logDebug(final Object message) {
    log(loggerPrefix.append(getDebug()).append(Util.mm(message)));
  }

  /**
   * consoleに[prefix + "ERROR" + message] を送信します
   *
   * @param message 送信するメッセージ
   */
  public final void logError(final Object message) {
    log(loggerPrefix.append(getError()).append(Util.mm(message)));
  }

  public final void send(final CommandSender sender, final Object str) {
    sender.sendMessage(Util.mm(getPrefix()).append(Util.mm(str)));
  }

  public final void cast(final Object str) {
    Bukkit.broadcast(getPrefix().append(Util.mm(str)));
  }

  public final void important(final Object str) {
    Util.important(Util.mm(getPrefix()).append(Util.mm(str)));
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
    Bukkit.getConsoleSender().sendMessage(Util.mm(message));
  }

  /**
   * 引数のbooleanに寄って送信するかを選択します
   *
   * @param message 送信するメッセージ
   * @param loggable メッセージを送信するか
   */
  protected final void log(final Object message, final boolean loggable) {
    if (loggable) {
      log(message);
    }
  }

  protected Component getWarn() {
    return Util.mm(" <reset><yellow>[WARN]<reset> ");
  }

  protected Component getTrace() {
    return Util.mm(" <reset><white>[TRACE]<reset> ");
  }

  protected Component getDebug() {
    return Util.mm(" <reset><gray>[DEBUG]<reset> ");
  }

  protected Component getError() {
    return Util.mm(" <reset><red>[ERROR]<reset> ");
  }
}
