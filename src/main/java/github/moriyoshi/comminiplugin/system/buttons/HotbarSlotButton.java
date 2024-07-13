package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.player.HotbarGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import github.moriyoshi.comminiplugin.system.slot.HotbarSlot;
import github.moriyoshi.comminiplugin.system.slot.HotbarSlotMenu;
import java.util.function.BiFunction;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HotbarSlotButton extends RedirectItemButton<MenuHolder<ComMiniPlugin>> {

  public HotbarSlotButton(
      BiFunction<MenuHolder<ComMiniPlugin>, InventoryClickEvent, ? extends HotbarSlot> func) {
    super(
        new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(),
        (holder, event) -> new HotbarSlotMenu(func.apply(holder, event)).getInventory());
  }

  public <T extends InterfaceGamePlayer & HotbarGamePlayer> HotbarSlotButton(Class<T> clazz) {
    super(
        new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(),
        (holder, event) ->
            new HotbarSlotMenu(
                    ComMiniPlayer.getPlayer(event.getWhoClicked().getUniqueId())
                        .getGamePlayerData(clazz)
                        .getHotbarSlot())
                .getInventory());
  }
}
