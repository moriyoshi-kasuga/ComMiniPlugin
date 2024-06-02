package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.inventory.ItemStack;

/**
 * アイコンが付いたボタンです。
 *
 * @param <MH> the menu holder type
 */
public class ItemButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

  private final WeakHashMap<MH, Set<Integer>> inventoriesContainingMe = new WeakHashMap<>();

  /** このボタンのアイコンです アイコンを更新するには inventoryを更新したいボタンは、代わりに {@link #setIcon(ItemStack)} を使用してください。 */
  protected ItemStack stack;

  /** アイコンを持たないItemButtonを作成します。 */
  protected ItemButton() {}

  /**
   * 指定されたItemStackを持つItemButtonを作成します。 ボタンはItemStackのクローンを使用します。
   *
   * @param stack the icon
   */
  public ItemButton(ItemStack stack) {
    this.stack = stack == null ? null : stack.clone();
  }

  /**
   * アイコンを取得します
   *
   * @return コンストラクタで提供されたItemStackのクローン、またはアイコンがない場合はnull。
   */
  @Override
  public final ItemStack getIcon() {
    return stack == null ? null : stack.clone();
  }

  /**
   * アイコンスタックを設定します。このボタンを含むメニューは、それに応じてインベントリが更新されます。
   *
   * @param icon the icon
   */
  public final void setIcon(ItemStack icon) {
    stack = icon == null ? null : icon.clone();
    inventoriesContainingMe.forEach(
        (menuHolder, slots) ->
            slots.forEach(slot -> menuHolder.getInventory().setItem(slot, stack)));
  }

  /**
   * このボタンがメニューに追加されたときに呼び出される。 ItemButton は、自身が含まれるメニューのキャッシュを保持する。 また、{@link #setIcon(ItemStack)}
   * によってアイコンが更新されると、それらのインベントリ内のアイテムスタックが更新されます。
   *
   * @param menuHolder the menu
   * @param slot 設置するslot
   * @return ボタンがメニューに追加できるかどうか
   */
  @Override
  public final boolean onAdd(MH menuHolder, int slot) {
    return inventoriesContainingMe.computeIfAbsent(menuHolder, mh -> new HashSet<>()).add(slot);
  }

  /**
   * メニューが含まれているメニューのキャッシュからメニューを削除する。
   *
   * @param menuHolder the menu
   * @param slot 削除するslot
   * @return ボタンがメニューから外せるかどうか
   */
  @Override
  public final boolean onRemove(MH menuHolder, int slot) {
    Set<Integer> slots = inventoriesContainingMe.get(menuHolder);
    if (slots != null) {
      boolean result = slots.remove(slot);
      if (slots.isEmpty()) {
        inventoriesContainingMe.remove(menuHolder);
      }
      return result;
    }
    return true;
  }
}
