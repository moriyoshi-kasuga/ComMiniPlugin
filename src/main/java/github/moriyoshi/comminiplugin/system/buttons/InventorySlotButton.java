package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InventoryGamePlayer;
import github.moriyoshi.comminiplugin.system.slot.InventorySlot;
import github.moriyoshi.comminiplugin.system.slot.InventorySlotMenu;
import java.util.function.BiFunction;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventorySlotButton extends RedirectItemButton<MenuHolder<ComMiniPlugin>> {

  public InventorySlotButton(
      BiFunction<MenuHolder<ComMiniPlugin>, InventoryClickEvent, ? extends InventorySlot> func) {
    super(
        new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(),
        (holder, event) -> new InventorySlotMenu(func.apply(holder, event)).getInventory());
  }

  public <T extends InterfaceGamePlayer & InventoryGamePlayer> InventorySlotButton(Class<T> clazz) {
    super(
        new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(),
        (holder, event) ->
            new InventorySlotMenu(
                    ComMiniPlayer.getPlayer(event.getWhoClicked().getUniqueId())
                        .getGamePlayerData(clazz)
                        .getInventorySlot())
                .getInventory());
  }
}
