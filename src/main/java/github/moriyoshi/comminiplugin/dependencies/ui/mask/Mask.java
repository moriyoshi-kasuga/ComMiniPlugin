package github.moriyoshi.comminiplugin.dependencies.ui.mask;

import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.util.IntBiConsumer;
import github.moriyoshi.comminiplugin.dependencies.ui.util.Option;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * マスクは {@link Pattern} と組み合わせて、インベントリやメニューなどのアイテムコンテナに対して一括操作を適用することができます。
 * これにより、同じ項目でレイアウトが異なるメニューを簡単に構築することができます。 マスクとパターンのもう一つの使用例として、アニメーションがあります。
 *
 * @param <Symbol> マスクが使用するシンボルです。一般的には、Boolean、Integer、Character、またはenumです。
 * @param <Item>   コンテナ内のアイテムの種類を示す。
 * @see Inventory
 * @see MenuHolder
 */
public interface Mask<Symbol, Item> {

  /**
   * 地図からマスクを作成する。
   *
   * @param map      Maskで使用されるマッピングです。
   * @param <Symbol> symbol type
   * @param <Item>   item type
   * @return 新マスク
   */
  static <Symbol, Item> Mask<Symbol, Item> ofMap(Map<Symbol, Item> map) {
    return new MapMask<>(map);
  }

  static <Symbol, Item> Mask<Symbol, Item> ofSingle(Symbol symbol, Item item) {
    return new SingleMask<>(symbol, item);
  }

  /**
   * 容器にマスクとパターンを貼る。
   *
   * @param mask           the mask
   * @param pattern        the pattern
   * @param indexGenerator どのスロットを更新するかを決定するジェネレータ
   * @param updater        コンテナのセッター機能
   * @param <Symbol>       the symbol type
   * @param <Item>         element type
   */
  static <Symbol, Item> void apply(Mask<Symbol, Item> mask, Pattern<Symbol> pattern,
      IntStream indexGenerator, IntBiConsumer<Item> updater) {
    indexGenerator.forEach(index -> {
      Symbol symbol = pattern.getSymbol(index);
      var item = mask.getItem(symbol);
      if (item.isPresent()) {
        updater.accept(index, item.get());
      }
    });
  }

  /**
   * インベントリに一括更新を適用します。パターンとマスクがサポートされているすべてのインベントリスロットが更新されます。
   *
   * @param mask      the mask
   * @param pattern   the pattern
   * @param inventory the inventory
   * @param <Symbol>  the symbol type
   */
  static <Symbol> void applyInventory(Mask<Symbol, ItemStack> mask, Pattern<Symbol> pattern,
      Inventory inventory) {
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      Symbol symbol = pattern.getSymbol(slot);
      var item = mask.getItem(symbol);
      if (item.isPresent()) {
        inventory.setItem(slot, item.get());
      }
    }
  }

  /**
   * メニューに一括更新を適用する。パターンとマスクが対応しているすべてのメニュースロットが更新されます。
   *
   * @param mask     the mask
   * @param pattern  the pattern
   * @param menu     the inventory
   * @param <Symbol> the symbol Type
   * @param <P>      the plugin type
   * @param <MH>     the MenuHolder type
   */
  static <Symbol, P extends Plugin, MH extends MenuHolder<P>> void applyMenu(
      Mask<Symbol, ? extends MenuButton<MH>> mask, Pattern<Symbol> pattern, MH menu) {
    for (int slot = 0; slot < menu.getInventory().getSize(); slot++) {
      Symbol symbol = pattern.getSymbol(slot);
      var button = mask.getItem(symbol);
      if (button.isPresent()) {
        menu.setButton(slot, button.get());
      }
    }
  }

  /**
   * シンボルがマッピングされている項目を取得する。
   *
   * @param symbol a symbol value, or null
   * @return マップされた値を含む Option、またはシンボルがこの Mask でサポートされていない場合は、空の Option を指定します。
   */
  Option<Item> getItem(Symbol symbol);

}

class SingleMask<Symbol, Item> implements Mask<Symbol, Item> {

  private final Symbol symbol;
  private final Item item;

  public SingleMask(Symbol symbol, Item item) {
    this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
    this.item = Objects.requireNonNull(item, "item cannot be null");
  }

  public Option<Item> getItem(Symbol symbol) {
    if (this.symbol.equals(symbol)) {
      return Option.some(item);
    } else {
      return Option.none();
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(symbol) + Objects.hashCode(item);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SingleMask that)) {
      return false;
    }

    return Objects.equals(this.symbol, that.symbol) && Objects.equals(this.item, that.item);
  }

  @Override
  public String toString() {
    return "SingleMask(symbol=" + symbol + ",item=" + item + ")";
  }

}

class MapMask<Symbol, Item> implements Mask<Symbol, Item> {

  private final Map<Symbol, Item> mapper;

  /**
   * Construct a Mask.
   *
   * @param mapper the mapping
   */
  public MapMask(Map<Symbol, Item> mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper cannot be null");
  }

  /**
   * シンボルがマッピングされている項目を取得する。
   *
   * @param symbol a symbol value, or null
   * @return このマスクがマッピングを含む場合はSome(item)、それ以外はNone
   */
  public Option<Item> getItem(Symbol symbol) {
    if (mapper.containsKey(symbol)) {
      return Option.some(mapper.get(symbol));
    } else {
      return Option.none();
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mapper);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MapMask that)) {
      return false;
    }

    return Objects.equals(this.mapper, that.mapper);
  }

  @Override
  public String toString() {
    return "MapMask(mapper=" + mapper + ")";
  }

}
