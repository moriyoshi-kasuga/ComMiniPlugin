package github.moriyoshi.comminiplugin.dependencies.ui.menu;

import github.moriyoshi.comminiplugin.dependencies.ui.GuiInventoryHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * アクションの確認やキャンセルを促すメニューのこと。 このメニューは、「はい」ボタンまたは「いいえ」ボタンがクリックされたときに閉じます。 実行するアクションがNULLの場合、何も起こりません。
 *
 * @param <P> your plugin's type
 */
public class YesNoMenu<P extends Plugin> extends MenuHolder<P> {

  private static final String ARE_YOU_SURE = "Are you sure?";
  private static final ItemStack YES_STACK = new ItemBuilder(Material.LIME_CONCRETE).name(
      "Yes - continue").build();
  private static final ItemStack NO_STACK = new ItemBuilder(Material.RED_CONCRETE).name(
      "No - cancel").build();

  /**
   * はいボタン、いいえボタンのコールバックについて
   */
  protected final Consumer<InventoryClickEvent> yesAction;
  protected final Consumer<InventoryClickEvent> noAction;
  protected boolean isCloseable = false;

  /**
   * Create the YesNoMenu.
   *
   * @param plugin    the plugin
   * @param type      the inventory's type
   * @param title     the inventory's title
   * @param yesAction the action to perform when the yes-button is clicked
   * @param noAction  the action to perform when the no-button is clicked
   */
  public YesNoMenu(P plugin, InventoryType type, String title,
      Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
    super(plugin, type, title);

    this.yesAction = yesAction;
    this.noAction = noAction;
    setupButtons();
  }

  /**
   * Create the YesNoMenu.
   *
   * @param plugin    the plugin
   * @param size      the inventory's size
   * @param title     the inventory's title
   * @param yesAction the action to perform when the yes-button is clicked
   * @param noAction  the action to perform when the no-button is clicked
   */
  public YesNoMenu(P plugin, int size, String title, Consumer<InventoryClickEvent> yesAction,
      Consumer<InventoryClickEvent> noAction) {
    super(plugin, size, title);

    this.yesAction = yesAction;
    this.noAction = noAction;
    setupButtons();
  }

  /**
   * Creates the YesNoMenu.
   *
   * @param plugin    the plugin
   * @param inventory the inventory
   * @param yesAction the action to perform when the yes-button is clicked
   * @param noAction  the action to perform when the no-button is clicked
   * @see GuiInventoryHolder#GuiInventoryHolder(Plugin, Inventory)
   */
  public YesNoMenu(P plugin, Inventory inventory, Consumer<InventoryClickEvent> yesAction,
      Consumer<InventoryClickEvent> noAction) {
    super(plugin, inventory);

    this.yesAction = yesAction;
    this.noAction = noAction;
    setupButtons();
  }

  /**
   * Creates the YesNoMenu.
   *
   * @param plugin    the plugin
   * @param title     the title of the inventory
   * @param yesAction the action to perform when the yes-button is clicked
   * @param noAction  the action to perform when the no-button is clicked
   */
  public YesNoMenu(P plugin, String title, Consumer<InventoryClickEvent> yesAction,
      Consumer<InventoryClickEvent> noAction) {
    this(plugin, InventoryType.HOPPER, title, yesAction, noAction);
  }

  /**
   * Creates the YesNoMenu. The inventory title defaults to {@code "Are you sure?"}.
   *
   * @param plugin    the plugin
   * @param yesAction the action to perform when the yes-button is clicked
   * @param noAction  the action to perform when the no-button is clicked
   */
  public YesNoMenu(P plugin, Consumer<InventoryClickEvent> yesAction,
      Consumer<InventoryClickEvent> noAction) {
    this(plugin, InventoryType.HOPPER, ARE_YOU_SURE, yesAction, noAction);
  }

  @NotNull
  protected ItemStack getYesStack() {
    return YES_STACK;
  }

  @NotNull
  protected ItemStack getNoStack() {
    return NO_STACK;
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    if (!isCloseable) {
      getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
        event.getPlayer().openInventory(getInventory());
        Util.send(event.getPlayer(),
            "<red>このインベントリーを閉じることはできるません<gray>(noボタンを押して閉じてください)"
        );
      });
    }
  }

  /**
   * コンストラクタから呼び出されます。オーバーライドしてボタンをカスタマイズする。
   */
  protected void setupButtons() {
    setButton(0, makeButton(true));
    setButton(getInventory().getSize() - 1, makeButton(false));
    getMiddleItem()
        .ifPresent(itemStack -> setButton((int) (getInventory().getSize() / 2.0),
            new ItemButton<>(itemStack)
        ));
  }

  public Optional<ItemStack> getMiddleItem() {
    return Optional.empty();
  }

  /**
   * ボタンの作成を補助するヘルパーメソッドです。オーバーライドすることで、ボタンの作成をカスタマイズできます。
   *
   * @param yesOrNo - yesボタンを作成する場合はtrue、noボタンを作成する場合はfalseを指定します。
   * @return a new button
   */
  protected MenuButton<YesNoMenu<P>> makeButton(boolean yesOrNo) {
    ItemStack stack = yesOrNo ? getYesStack() : getNoStack();
    Consumer<InventoryClickEvent> action = yesOrNo ? yesAction : noAction;

    return new ItemButton<>(stack) {

      @Override
      public void onClick(@NotNull YesNoMenu<P> holder, @NotNull InventoryClickEvent event) {
        getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
          if (isCloseable) {
            event.getView().close();
          } else {
            isCloseable = true;
            event.getView().close();
            isCloseable = false;
          }
          if (action != null) {
            action.accept(event);
          }
        });
      }
    };
  }

}
