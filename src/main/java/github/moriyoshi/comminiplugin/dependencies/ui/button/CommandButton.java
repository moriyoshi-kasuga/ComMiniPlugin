package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * クリックされるとコマンドを実行するボタンです。
 *
 * @param <MH> the menu holder type
 */
public class CommandButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  /**
   * -- GETTER -- Get the command.
   */
  @Getter
  private Command command;
  /**
   * -- GETTER -- Get the arguments.
   */
  @Getter
  private String[] arguments;
  /**
   * -- SETTER -- 結果ハンドラーを設定する。
   */
  @Setter
  private CommandResultHandler<MH> resultHandler;

  /**
   * 定数でないコマンドや引数を使用したいコマンドボタンのためのプロテクテッドコンストラクタです。
   * このスーパーコンストラクタを使用するサブクラスは、{@link #getArguments()}または{@link
   * #getCommand}またはそのオーバーロードをオーバーライドする必要があります。
   *
   * @param icon the icon
   */
  protected CommandButton(ItemStack icon) {
    super(icon);
  }

  /**
   * CommandButtonを作成します。
   *
   * @param icon      the icon of the button
   * @param command   実行されるコマンド
   * @param arguments コマンドの実行に使用される引数
   */
  public CommandButton(ItemStack icon, Command command, String... arguments) {
    super(icon);
    setCommand(command);
    setArguments(arguments);
  }

  /**
   * CommandButtonを作成します。
   *
   * @param icon          the icon of the button
   * @param command       実行されるコマンド
   * @param arguments     コマンドの実行に使用される引数
   * @param resultHandler コマンドが実行された後に実行されるハンドラ
   */
  public CommandButton(ItemStack icon, Command command, CommandResultHandler<MH> resultHandler,
      String... arguments) {
    this(icon, command, arguments);
    setResultHandler(resultHandler);
  }

  /**
   * getCommand()} で取得したコマンドを、{@link #getArguments()} で指定した引数で実行します。
   * もし{@link CommandResultHandler}があれば、その{@link CommandResultHandler#afterCommand(HumanEntity,
   * Command, String[], boolean, MenuHolder, InventoryClickEvent)}も実行します。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    Command command = getCommand(menuHolder, event);
    String[] arguments = getArguments(menuHolder, event);

    HumanEntity player = event.getWhoClicked();
    boolean success = command.execute(player, command.getLabel(), arguments);

    getResultHandler().ifPresent(
        resultHandler -> resultHandler.afterCommand(player, command, arguments, success, menuHolder,
            event
        ));
  }

  /**
   * MenuHolderとInventoryClickEventから使用するコマンドを算出する。
   * このメソッドは{@link #onClick(MenuHolder, InventoryClickEvent)}によって呼び出されます。
   * デフォルトの実装では、{@link #getCommand()}に委譲されます。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   * @return the command to be executed
   */
  protected Command getCommand(MH menuHolder, InventoryClickEvent event) {
    return getCommand();
  }

  /**
   * MenuHolderとInventoryClickEventから使用する引数を計算する。
   * このメソッドは{@link #onClick(MenuHolder, InventoryClickEvent)}によって呼び出されます。
   * デフォルトの実装では、{@link #getArguments()}に委ねられます。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   * @return コマンドを実行するための引数
   */
  protected String[] getArguments(MH menuHolder, InventoryClickEvent event) {
    return getArguments();
  }

  /**
   * コマンドを設定します。
   *
   * @param command the command
   */
  public void setCommand(Command command) {
    this.command = Objects.requireNonNull(command, "Command cannot be null");
  }

  /**
   * 引数を設定します。
   *
   * @param arguments the arguments
   */
  public void setArguments(String... arguments) {
    this.arguments = Objects.requireNonNull(arguments, "Arguments cannot be null");
  }

  /**
   * Get the result handler.
   *
   * @return {@link CommandResultHandler}が存在する場合はそれを含むOptional、そうでない場合は空のOptionalです。
   */
  public Optional<? extends CommandResultHandler<MH>> getResultHandler() {
    return Optional.ofNullable(resultHandler);
  }

  /**
   * コマンド実行後に{@link #onClick(MenuHolder, InventoryClickEvent)}メソッドから実行されるコールバックです。
   *
   * @param <MH> the menu holder type
   */
  @FunctionalInterface
  public interface CommandResultHandler<MH extends MenuHolder<?>> {

    /**
     * The callback method.
     *
     * @param player                  コマンドを実行したプレイヤー
     * @param command                 実行されたコマンド
     * @param arguments               コマンドが実行されたときの引数
     * @param wasExecutedSuccessFully コマンドが正常に実行されたかどうか
     * @param menuHolder              the menu holder
     * @param event                   コマンドを実行する原因となったイベント
     */
    void afterCommand(HumanEntity player, Command command, String[] arguments,
        boolean wasExecutedSuccessFully, MH menuHolder, InventoryClickEvent event);

  }

}
