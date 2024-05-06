package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import java.util.function.UnaryOperator;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * {@link CycleButton}の一般化。UnaryOperatorによって生成された状態を循環させる。
 *
 * @param <T>  the type of the state
 * @param <MH> the menu type
 */
public class IteratingButton<T, MH extends MenuHolder<?>> extends ItemButton<MH> {

  /**
   * -- GETTER -- 現在の状態を取得する。
   */
  @Getter
  protected T currentState;
  protected UnaryOperator<T> stateUpdater;

  /**
   * アイコンのみのIteratingButtonを作成します。サブクラスは {@link #currentState} と {@link #stateUpdater}
   * フィールドを設定する必要があります。
   * または、{@link #getCurrentState()}と{@link #updateCurrentState(MenuHolder,
   * InventoryClickEvent)}をオーバーライドする方法もある。
   *
   * @param icon the icon
   */
  protected IteratingButton(ItemStack icon) {
    super(icon);
  }

  /**
   * ボタンを作成します。
   *
   * @param icon         the icon
   * @param initialState the initial state
   * @param stateUpdater the state update function
   */
  public IteratingButton(ItemStack icon, T initialState, UnaryOperator<T> stateUpdater) {
    super(icon);
    this.currentState = initialState;
    this.stateUpdater = Objects.requireNonNull(stateUpdater, "StateUpdater cannot be null");
  }

  /**
   * 現在の状態を新しい状態に設定します。
   *
   * @param newState the new current state
   */
  protected void setCurrentState(T newState) {
    this.currentState = newState;
  }

  /**
   * Updates the internal state.
   *
   * @param menuHolder the MenuHolder
   * @param event      the event that caused the state update
   */
  protected void updateCurrentState(MH menuHolder, InventoryClickEvent event) {
    setCurrentState(stateUpdater.apply(getCurrentState()));
  }

  /**
   * このボタンをトグルします。サブクラスは、オーバーライドすることによって、現在の状態を更新する前と後の余分な副作用を追加できます。 {beforeToggle(MenuHolder,
   * InventoryClickEvent)}と{@link #afterToggle(MenuHolder, InventoryClickEvent)}です。
   *
   * @param holder the MenuHolder
   * @param event  the InventoryClickEvent
   */
  @Override
  public final void onClick(@NotNull MH holder, @NotNull InventoryClickEvent event) {
    boolean toggleSuccess = tryToggle(holder, event);
    if (toggleSuccess) {
      setIcon(updateIcon(holder, event));
      event.setCurrentItem(getIcon());
    }
  }

  /**
   * もし{@link #beforeToggle(MenuHolder, InventoryClickEvent)}で許可されていれば、ボタンをトグルします。
   * このメソッドは、{@link #onClick(MenuHolder, InventoryClickEvent)}によって呼び出されます。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   * @return whether the button could be toggled
   * @see #afterToggle(MenuHolder, InventoryClickEvent)
   */
  protected boolean tryToggle(MH menuHolder, InventoryClickEvent event) {
    if (!beforeToggle(menuHolder, event)) {
      return false;
    }
    this.updateCurrentState(menuHolder, event);
    afterToggle(menuHolder, event);
    return true;
  }

  /**
   * ボタンがトグル可能かどうかをチェックします。このメソッドは{@link #tryToggle(MenuHolder, InventoryClickEvent)}によって呼び出されます。
   * デフォルトの実装では、常にtrueを返します。
   *
   * @param menuHolder the inventory holder for the menu
   * @param event      the InventoryClickEvent that caused the button to toggle
   * @return true
   */
  public boolean beforeToggle(MH menuHolder, InventoryClickEvent event) {
    return true;
  }

  /**
   * ボタンがトグルされた後にサイドエフェクトを実行する。このメソッドは、{@link #tryToggle(MenuHolder, InventoryClickEvent)}から呼び出されます。
   * デフォルトの実装では、何もしません。
   *
   * @param menuHolder the inventory holder for the menu
   * @param event      the InventoryClickEvent that caused the button to toggle
   */
  public void afterToggle(MH menuHolder, InventoryClickEvent event) {
  }

  /**
   * アイコンの外観を決定します。 サブクラスは、{@link #setIcon(ItemStack)} を呼び出したりオーバーライドしたりせずに、このメソッドを安全にオーバーライドできます。
   *
   * @param menuHolder the inventory holder for the menu
   * @param event      the InventoryClickEvent that caused the button to toggle
   * @return the updated icon
   */
  public ItemStack updateIcon(MH menuHolder, InventoryClickEvent event) {
    return getIcon();
  }

}
