package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * {@link ToggleButton}の一般化です。このボタンは、固定配列の状態を循環させる。
 *
 * @param <T>  the state type
 * @param <MH> the menu holder type
 */
public class CycleButton<T, MH extends MenuHolder<?>> extends TwoWayIteratingButton<T, MH> {

  private final T[] items;
  private int cursor;

  /**
   * Creates the cycle button.
   *
   * @param icon       the icon of this button
   * @param items      このボタンで切り替わる項目
   * @param startIndex どのインデックスでサイクリングを開始するか
   * @param copyArray  ボタンが配列のコピーを使用するかどうか
   */
  protected CycleButton(ItemStack icon, T[] items, int startIndex, boolean copyArray) {
    super(icon);
    if (items == null) {
      throw new NullPointerException("Items array cannot be null");
    }
    if (items.length == 0) {
      throw new IllegalArgumentException("Items array must contain at least one element");
    }

    this.items = copyArray ? Arrays.copyOf(items, items.length) : items;
    setCursor(startIndex);
  }

  /**
   * Creates the cycle button.
   *
   * @param icon  the icon of this button
   * @param items このボタンで切り替わる項目
   */
  @SafeVarargs
  public CycleButton(ItemStack icon, T... items) {
    this(icon, items, 0);
  }

  /**
   * Creates the cycle button.
   *
   * @param icon       the icon of this button
   * @param items      このボタンで切り替わる項目
   * @param startIndex どのインデックスでサイクリングを開始するか
   */
  public CycleButton(ItemStack icon, T[] items, int startIndex) {
    this(icon, items, startIndex, true);
  }

  /**
   * Creates the cycle button.
   *
   * @param icon  the icon of this button
   * @param items このボタンで切り替わる項目
   */
  public CycleButton(ItemStack icon, Collection<? extends T> items) {
    this(icon, items, 0);
  }

  /**
   * Creates the cycle button.
   *
   * @param icon       the icon of this button
   * @param items      このボタンで切り替わる項目
   * @param startIndex どのインデックスでサイクリングを開始するか
   */
  @SuppressWarnings("unchecked")
  public CycleButton(ItemStack icon, Collection<? extends T> items, int startIndex) {
    this(icon, (T[]) items.toArray(), startIndex, false);
  }

  /**
   * すべての列挙値を循環させるサイクルボタンを作成します。
   *
   * @param icon      the icon of this button
   * @param enumClass the enumeration
   * @param <T>       the type of the state in this button
   * @param <MH>      the MenuHolder type
   * @return a new cycle button
   */
  public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(
      ItemStack icon, Class<? extends T> enumClass) {
    return new CycleButton<>(icon, enumClass.getEnumConstants(), 0, false);
  }

  /**
   * すべての列挙値を循環させるサイクルボタンを作成します。
   *
   * @param icon       the icon of this button
   * @param enumClass  the enumeration
   * @param startValue the enum value at which to start cycling
   * @param <T>        the type of the state in this button
   * @param <MH>       the MenuHolder type
   * @return a new cycle button
   */
  public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(
      ItemStack icon, Class<? extends T> enumClass, T startValue) {
    return new CycleButton<>(icon, enumClass.getEnumConstants(), startValue.ordinal(), false);
  }

  /**
   * 与えられた列挙値を循環させるサイクルボタンを返します。
   *
   * @param icon  the icon of this button
   * @param items the items this button cycles through
   * @param <T>   the type of the state in this button
   * @param <MH>  the MenuHolder type
   * @return a new cycle button
   */
  // completely redundant since users could just use the collection constructor,
  // but this allows for easier refactoring :-)
  public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(
      ItemStack icon, EnumSet<? extends T> items) {
    return new CycleButton<>(icon, items);
  }

  /**
   * 与えられた列挙値を循環させるサイクルボタンを返します。
   *
   * @param icon       the icon of this button
   * @param items      the items this button cycles through
   * @param startValue the enum value at which to start cycling
   * @param <T>        the type of the state in this button
   * @param <MH>       the MenuHolder type
   * @return a new cycle button
   */
  public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(
      ItemStack icon, EnumSet<? extends T> items, T startValue) {
    return new CycleButton<>(icon, items, startValue.ordinal());
  }

  /**
   * Moves the cursor to the next state.
   */
  protected void incrementCursor() {
    setCursor(getCursor() + 1);
  }

  /**
   * Moves the cursor to the previous state.
   */
  protected void decrementCursor() {
    setCursor(getCursor() - 1);
  }

  /**
   * Gets the current cursor position.
   *
   * @return the cursor
   */
  protected int getCursor() {
    return cursor;
  }

  /**
   * カーソル位置を設定します。
   *
   * @param cursor the new cursor
   */
  protected void setCursor(int cursor) {
    cursor = cursor % items.length;
    if (cursor < 0) {
      cursor += items.length;
    }
    this.cursor = cursor;
  }

  /**
   * ボタンの現在の状態を取得します。
   *
   * @return the state
   */
  @Override
  public T getCurrentState() {
    return items[getCursor()];
  }

  /**
   * 現在の状態を前の状態に更新する。 CycleButtonの実装は{@link #incrementCursor()}に委譲しています。
   *
   * @param menuHolder the menu holder
   * @param event      状態更新の原因となったクリックイベント
   */
  @Override
  public void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
    incrementCursor();
  }

  /**
   * 現在の状態を次の状態に更新する。 CycleButtonの実装では、{@link #decrementCursor()} に委譲しています。
   *
   * @param menuHolder the menu holder
   * @param event      状態更新の原因となったクリックイベント
   */
  @Override
  public void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
    decrementCursor();
  }

}
