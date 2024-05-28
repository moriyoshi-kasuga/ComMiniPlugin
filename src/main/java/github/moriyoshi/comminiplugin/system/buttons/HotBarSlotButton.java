package github.moriyoshi.comminiplugin.system.buttons;

import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.hotbar.HotBarSlot;
import github.moriyoshi.comminiplugin.system.hotbar.HotBarSlotMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class HotBarSlotButton extends RedirectItemButton<MenuHolder<ComMiniPlugin>> {

  public HotBarSlotButton(BiFunction<MenuHolder<ComMiniPlugin>, InventoryClickEvent, ? extends HotBarSlot> func) {
    super(new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(), (holder,
        event) -> new HotBarSlotMenu(func.apply(holder, event)).getInventory());
  }
}
