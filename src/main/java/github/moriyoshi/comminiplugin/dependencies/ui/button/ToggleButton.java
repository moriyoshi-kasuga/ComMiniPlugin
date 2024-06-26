package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * トグル可能なボタンです。 ボタンは、有効な状態でも無効な状態でもかまいません。
 *
 * @param <MH> the menu holder type
 * @see #beforeToggle(MenuHolder, InventoryClickEvent)
 * @see #afterToggle(MenuHolder, InventoryClickEvent)
 * @see CycleButton
 */
public class ToggleButton<MH extends MenuHolder<?>> extends CycleButton<Boolean, MH> {

  /**
   * 指定されたアイコンのトグルボタンを作成します。デフォルトではトグルボタンはオフになっています。
   *
   * @param icon the icon
   */
  public ToggleButton(ItemStack icon) {
    this(icon, false);
  }

  /**
   * 指定されたアイコンとtoggle-stateを持つトグルボタンを作成します。
   *
   * @param icon the icon
   * @param enabled whether the icon is enabled from the start
   */
  public ToggleButton(ItemStack icon, boolean enabled) {
    super(icon, new Boolean[] {false, true}, enabled ? 1 : 0, false);
  }

  protected ItemStack enable(ItemStack stack) {
    if (stack == null) {
      return null;
    }

    new ItemBuilder(stack).glow();

    return stack;
  }

  protected ItemStack disable(ItemStack stack) {
    if (stack == null) {
      return null;
    }

    new ItemBuilder(stack).unGlow();

    return stack;
  }

  /**
   * このボタンがトグルオンになっているかどうかを取得します。
   *
   * @return true if this button is toggled on, otherwise false
   */
  public final boolean isEnabled() {
    return getCurrentState();
  }

  /**
   * アイコンの外観を決定します。 実装では、このメソッドをオーバーライドできます。
   *
   * @param menuHolder the inventory holder for the menu
   * @param event ボタンがトグルする原因となった InventoryClickEvent。
   * @return the updated icon.
   */
  @Override
  public ItemStack updateIcon(MH menuHolder, InventoryClickEvent event) {
    return isEnabled() ? enable(getIcon()) : disable(getIcon());
  }
}
