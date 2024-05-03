package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * これは{@link PermissionButton}の一般化である。 述語が満たされたときに、他のボタンに委譲するボタンを表現する。
 *
 * @param <MH> the MenuHolder type
 */
public class PredicateButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

  private final BiPredicate<MH, InventoryClickEvent> predicate;
  private final BiConsumer<MH, InventoryClickEvent> predicateFailedCallback;
  protected MenuButton<MH> delegate;

  /**
   * PredicateButtonを作成する。
   *
   * @param delegate  このボタンが委譲するボタン
   * @param predicate デリゲートが呼び出されるために満たされる必要がある述語です。
   */
  public PredicateButton(MenuButton<MH> delegate, BiPredicate<MH, InventoryClickEvent> predicate) {
    this(delegate, predicate, null);
  }

  /**
   * PredicateButton を作成します
   *
   * @param delegate                このボタンが委譲するボタン
   * @param predicate               デリゲートが呼び出されるために満たす必要がある述語
   * @param predicateFailedCallback 述語が満たされなかったときに呼び出されるコールバック
   */
  public PredicateButton(MenuButton<MH> delegate, BiPredicate<MH, InventoryClickEvent> predicate,
      BiConsumer<MH, InventoryClickEvent> predicateFailedCallback) {
    this.delegate = Objects.requireNonNull(delegate, "Delegate button cannot be null");
    this.predicate = Objects.requireNonNull(predicate, "Predicate cannot be null");
    this.predicateFailedCallback = predicateFailedCallback;
  }

  /**
   * {@link MenuHolder}によって呼び出される -
   * 述語が満たされるかどうかをテストし、デリゲートボタンの{@link MenuButton#onClick(MenuHolder, InventoryClickEvent)}を呼び出します。
   *
   * @param menuHolder the MenuHolder
   * @param event      the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    if (getPredicate().test(menuHolder, event)) {
      getDelegate().onClick(menuHolder, event);
    } else {
      getPredicateFailedCallback().ifPresent(callback -> callback.accept(menuHolder, event));
    }
  }

  /**
   * ボタンのアイコンを取得します。
   *
   * @return デリゲートボタンのアイコン
   */
  @Override
  public ItemStack getIcon() {
    return delegate.getIcon();
  }

  /**
   * このボタンをメニューに追加できるかどうかを決定する。 デフォルトの実装では、デリゲートに委任します。
   *
   * @param menuHolder the menu
   * @param slot       the position in the menu
   * @return true if the button can be added to the menu, otherwise false
   * @see MenuHolder#setButton(int, MenuButton)
   */
  @Override
  public boolean onAdd(MH menuHolder, int slot) {
    return getDelegate().onAdd(menuHolder, slot);
  }

  /**
   * このボタンをメニューから削除できるかどうかを決定する。 デフォルトの実装では、デリゲートに委任します。
   *
   * @param menuHolder the menu
   * @param slot       the position in the menu
   * @return true if the button can be removed from the menu, otherwise false
   * @see MenuHolder#unsetButton(int)
   */
  @Override
  public boolean onRemove(MH menuHolder, int slot) {
    return getDelegate().onRemove(menuHolder, slot);
  }

  /**
   * 述語が満たされたときに、このボタンが委譲するボタンを取得する。 このメソッドをオーバーライドする実装は、決して null を返してはならない。
   *
   * @return デリゲートボタン
   */
  protected MenuButton<MH> getDelegate() {
    return delegate;
  }

  /**
   * このボタンの述語を取得する
   *
   * @return the predicate
   */
  protected BiPredicate<MH, InventoryClickEvent> getPredicate() {
    return predicate;
  }

  /**
   * オプションで、ボタンがクリックされ、述語が満たされない場合に実行されるコールバックを取得する。
   *
   * @return コールバックがある場合は、コールバックを含むOptionalを返します。コールバックがない場合は、空のOptionalが返される。
   */
  protected Optional<BiConsumer<MH, InventoryClickEvent>> getPredicateFailedCallback() {
    return Optional.ofNullable(predicateFailedCallback);
  }

}
