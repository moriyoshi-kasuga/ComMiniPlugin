package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface RedirectButton<MH extends MenuHolder<?>> extends MenuButton<MH> {

  /**
   * ボタンがクリックされたときに呼び出されるコールバックです。 1回クリックすると、プレイヤーのインベントリは閉じられ、インベントリに供給された
   * {@link #to(MenuHolder, InventoryClickEvent)}が開かれます。
   *
   * @param holder the MenuHolder
   * @param event  the InventoryClickEvent
   */
  @Override
  default void onClick(@NotNull MH holder, @NotNull InventoryClickEvent event) {
    holder.getPlugin().getServer().getScheduler().runTask(holder.getPlugin(), () -> {
      event.getView().close();

      HumanEntity player = event.getWhoClicked();
      Inventory to = to(holder, event);
      if (to != null) {
        player.openInventory(to);
      }
    });
  }

  /**
   * このボタンがリダイレクトするインベントリを取得します。
   *
   * @param MenuHolder the MenuHolder
   * @param event      the InventoryClickEvent
   * @return the inventory
   */
  Inventory to(MH MenuHolder, InventoryClickEvent event);

}
