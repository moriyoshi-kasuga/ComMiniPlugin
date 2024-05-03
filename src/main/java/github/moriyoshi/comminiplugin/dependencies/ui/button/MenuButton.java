package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * {@link MenuHolder} に置くことができるボタン。
 *
 * @param <MH> the menu holder type
 */
public interface MenuButton<MH extends MenuHolder<?>> {

  /**
   * このボタンがクリックされたときに呼び出されるコールバック
   * <p>
   * デフォルトの実装では、何もしません
   *
   * @param holder the MenuHolder
   * @param event  the InventoryClickEvent
   */
  default void onClick(@NotNull MH holder, @NotNull InventoryClickEvent event) {
  }

  /**
   * ボタンのアイコン
   * <p>
   * デフォルトの実装ではnullが返されます
   *
   * @return the icon
   */
  default ItemStack getIcon() {
    return null;
  }

  /**
   * メニューにボタンが追加されたときに呼び出される
   *
   * @param menuHolder the menu
   * @param slot       設置するslot
   * @return ボタンが追加されたかどうか、デフォルトでは true
   * @see MenuHolder#setButton(int, MenuButton)
   */
  default boolean onAdd(MH menuHolder, int slot) {
    return true;
  }

  /**
   * ボタンがメニューから削除されたときに呼び出される
   *
   * @param menuHolder the menu
   * @param slot       削除するslot
   * @return ボタンが削除される可能性がある、デフォルトはtrue
   * @see MenuHolder#unsetButton(int)
   */
  default boolean onRemove(MH menuHolder, int slot) {
    return true;
  }

}
