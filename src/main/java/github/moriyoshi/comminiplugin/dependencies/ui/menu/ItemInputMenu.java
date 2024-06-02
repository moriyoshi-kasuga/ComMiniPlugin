package github.moriyoshi.comminiplugin.dependencies.ui.menu;

import github.moriyoshi.comminiplugin.dependencies.ui.GuiInventoryHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ItemInputMenu<P extends Plugin> extends MenuHolder<P> {

  private final ItemStack YES_STACK =
      new ItemBuilder(Material.LIME_CONCRETE).name("Yes - continue").build();
  private final ItemStack NO_STACK =
      new ItemBuilder(Material.RED_CONCRETE).name("No - cancel").build();

  private final ItemStack defaultItemStack;
  protected final BiConsumer<ItemStack, InventoryClickEvent> yesAction;
  protected final BiConsumer<ItemStack, InventoryClickEvent> noAction;
  protected boolean isCloseable = false;
  private ItemStack itemStack;

  public ItemInputMenu(
      P plugin,
      String title,
      ItemStack defaultItemStack,
      BiConsumer<ItemStack, InventoryClickEvent> yesAction,
      BiConsumer<ItemStack, InventoryClickEvent> noAction) {
    super(plugin, InventoryType.HOPPER, title);
    this.defaultItemStack = defaultItemStack;
    this.yesAction = yesAction;
    this.noAction = noAction;
    setupButtons();
  }

  public ItemInputMenu(
      P plugin,
      String title,
      BiConsumer<ItemStack, InventoryClickEvent> yesAction,
      BiConsumer<ItemStack, InventoryClickEvent> noAction) {
    this(
        plugin,
        title,
        new ItemBuilder(Material.END_CRYSTAL).name("ドラッグでアイテムをセット").build(),
        yesAction,
        noAction);
  }

  protected void setupButtons() {
    setButton(0, makeButton(true));
    setButton(
        2,
        new ItemButton<>(defaultItemStack) {

          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            ItemStack cursor = event.getCursor();
            if (cursor.isEmpty()) {
              event.getClickedInventory().setItem(event.getSlot(), stack);
              itemStack = null;
              return;
            }
            itemStack = cursor.clone();
            event.setCurrentItem(cursor);
          }
        });
    setButton(4, makeButton(false));
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    if (!isCloseable) {
      getPlugin()
          .getServer()
          .getScheduler()
          .runTask(
              getPlugin(),
              () -> {
                event.getPlayer().openInventory(getInventory());
                Util.send(event.getPlayer(), "<red>このインベントリーを閉じることはできるません<gray>(noボタンを押して閉じてください)");
              });
    }
  }

  @Nullable
  public ItemStack getItemStack() {
    return itemStack;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    super.onClick(event);
    Inventory clickedInventory = GuiInventoryHolder.getClickedInventory(event);
    if (clickedInventory == null) {
      return;
    }
    if (clickedInventory.equals(event.getView().getBottomInventory())) {
      event.setCancelled(false);
    }
  }

  protected ItemStack getYesStack() {
    return YES_STACK;
  }

  protected ItemStack getNoStack() {
    return NO_STACK;
  }

  protected MenuButton<ItemInputMenu<P>> makeButton(boolean yesOrNo) {
    ItemStack stack = yesOrNo ? getYesStack() : getNoStack();
    var action = yesOrNo ? yesAction : noAction;
    return new ItemButton<>(stack) {

      @Override
      public void onClick(@NotNull ItemInputMenu<P> holder, @NotNull InventoryClickEvent event) {
        if (isCloseable) {
          event.getView().close();
        } else {
          isCloseable = true;
          event.getView().close();
          isCloseable = false;
        }
        if (action != null) {
          action.accept(getItemStack(), event);
        }
      }
    };
  }
}
