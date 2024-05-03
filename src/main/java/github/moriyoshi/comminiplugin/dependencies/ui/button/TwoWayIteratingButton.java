package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.ListIterator;
import java.util.function.UnaryOperator;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 右クリックで前の状態に、左クリックで次の状態に、内部状態を更新できるボタン。
 *
 * @param <T>  the state's type
 * @param <MH> the menu holder's type
 */
public class TwoWayIteratingButton<T, MH extends MenuHolder<?>> extends IteratingButton<T, MH> {

  private UnaryOperator<T> backwardsFunction;

  /**
   * アイコンのみを持つTwoWayIteratingButtonを作成します。
   * このコンストラクタを使用すると、サブクラスは構築時にフィールド{@link #backwardsFunction}、{@link #stateUpdater}、{@link
   * #currentState}を初期化することが必要になります。
   * あるいは、{@link #updateStateForwards(MenuHolder, InventoryClickEvent)}、{@link
   * #updateStateBackwards(MenuHolder, InventoryClickEvent)}、 {@link #getCurrentState()}
   * をオーバーライドしなければならない。
   *
   * @param icon the icon of this button
   */
  protected TwoWayIteratingButton(ItemStack icon) {
    super(icon);
  }

  /**
   * TwoWayIteratingButtonを作成します。
   *
   * @param icon              the icon of this button
   * @param initialState      the initial current state
   * @param forwardsFunction  the function that provides next states
   * @param backwardsFunction the function that provides previous states.
   */
  public TwoWayIteratingButton(ItemStack icon, T initialState, UnaryOperator<T> forwardsFunction,
      UnaryOperator<T> backwardsFunction) {
    super(icon, initialState, forwardsFunction);
    this.backwardsFunction = backwardsFunction;
  }

  /**
   * リスト・イテレータから新しいTwoWayIteratingButtonを作成します。
   *
   * @param icon         the icon
   * @param listIterator the iterator providing the states
   * @param <T>          the state type
   * @param <MH>         the menu holder type
   * @return a new TwoWayIteratingButton
   * @throws IllegalArgumentException if the listIterator has no element.
   */
  public static <T, MH extends MenuHolder<?>> TwoWayIteratingButton<T, MH> fromListIterator(
      ItemStack icon, ListIterator<T> listIterator) {
    if (!listIterator.hasNext()) {
      throw new IllegalArgumentException("ListIterator must have at least one element");
    }
    // now that the class is an anonymous class it can't be extended. is that a
    // problem? It might be. I don't want to force the adapter pattern on my users.
    return new TwoWayIteratingButton<>(icon) {

      {
        setCurrentState(listIterator.next());
      }

      @Override
      public void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
        setCurrentState(listIterator.next());
      }

      @Override
      public void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
        setCurrentState(listIterator.previous());
      }

      @Override
      public boolean beforeToggle(MH menuHolder, InventoryClickEvent event) {
        if (event.isLeftClick()) {
          return listIterator.hasNext();
        } else if (event.isRightClick()) {
          return listIterator.hasPrevious();
        } else {
          return false;
        }
      }
    };
  }

  /**
   * 現在の状態を更新します。左クリックの場合、次の状態に更新されます。 右クリックの場合は、前の状態に更新されます。
   *
   * @param menuHolder the MenuHolder
   * @param event      the event that caused the state update
   */
  @Override
  protected void updateCurrentState(MH menuHolder, InventoryClickEvent event) {
    if (event.isLeftClick()) {
      updateStateForwards(menuHolder, event);
    } else if (event.isRightClick()) {
      updateStateBackwards(menuHolder, event);
    }
  }

  /**
   * 内部状態を次の状態に更新する。
   *
   * @param menuHolder the menu holder
   * @param event      the click event that caused the state update
   */
  protected void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
    super.updateCurrentState(menuHolder, event);
  }

  /**
   * 内部状態を前の状態に更新する。
   *
   * @param menuHolder the menu holder
   * @param event      the click event that caused the state update
   */
  protected void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
    setCurrentState(backwardsFunction.apply(getCurrentState()));
  }

}
