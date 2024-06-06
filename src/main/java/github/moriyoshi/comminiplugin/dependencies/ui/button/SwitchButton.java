package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SwitchButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  private final ItemStack defaultItem;
  private final ItemStack switchedItem;
  private boolean isDefault;

  public SwitchButton(ItemStack defaultItem, ItemStack switchedItem) {
    this(defaultItem, switchedItem, true);
  }

  public SwitchButton(ItemStack defaultItem, ItemStack switchedItem, boolean isDefault) {
    super(isDefault ? defaultItem : switchedItem);
    this.defaultItem = defaultItem;
    this.switchedItem = switchedItem;
    this.isDefault = isDefault;
  }

  @Override
  public void onClick(@NotNull MH holder, @NotNull InventoryClickEvent event) {
    isDefault = !isDefault;
    if (isDefault) {
      setIcon(defaultItem);
    } else {
      setIcon(switchedItem);
    }
    afterChange(holder, event);
  }

  public void afterChange(MH holder, InventoryClickEvent event) {}
}
