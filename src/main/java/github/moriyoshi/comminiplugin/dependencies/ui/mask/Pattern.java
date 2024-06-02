package github.moriyoshi.comminiplugin.dependencies.ui.mask;

import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns.BorderPattern;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns.BorderPattern.Border;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns.CheckerboardPattern;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns.CheckerboardPattern.Tile;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.util.IntBiConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * パターンは、スロットからある種のオブジェクトへのマッピングを表します。 このオブジェクトタイプは、適切なequalsとhashCodeの実装を持たなければなりません（MUST）。
 *
 * <p>パターンはマスクと組み合わせて使用するのが最適です, see {@link Mask#applyInventory(Mask, Pattern, Inventory)}, {@link
 * Mask#applyMenu(Mask, Pattern, MenuHolder)}.
 *
 * @param <Symbol> オブジェクトの種類を表します。一般的には、Boolean、Integer、Character、またはenumです。
 * @see Mask
 */
public interface Pattern<Symbol> {

  static <Symbol> Pattern<Symbol> ofSingle(int location, Symbol symbol) {
    Objects.requireNonNull(symbol, "symbols map cannot be null");
    return i -> i == location ? symbol : null;
  }

  /**
   * Mapで裏打ちされたパターンを作成する。返されたPatternは、そのシンボルをMapで検索します。
   *
   * @param symbols the map
   * @param <Symbol> パターンが提供するシンボルの種類
   * @return 名文句
   */
  static <Symbol> Pattern<Symbol> ofMap(Map<Integer, Symbol> symbols) {
    Objects.requireNonNull(symbols, "symbols map cannot be null");

    return symbols::get;
  }

  /**
   * 配列で裏打ちされたパターンを作成します。返されたパターンは、そのシンボルを配列で検索します。
   *
   * @param symbols the array
   * @param <Symbol> パターンが提供するシンボルの種類
   * @return the pattern
   */
  static <Symbol> Pattern<Symbol> ofArray(Symbol[] symbols) {
    Objects.requireNonNull(symbols, "symbols array cannot be null");

    return i -> symbols[i];
  }

  /**
   * リストで裏打ちされたパターンを作成する。返されたPatternは、そのシンボルをリストで検索します。
   *
   * @param symbols the array
   * @param <Symbol> パターンが提供するシンボルの種類
   * @return the pattern
   */
  static <Symbol> Pattern<Symbol> ofList(List<Symbol> symbols) {
    Objects.requireNonNull(symbols, "symbols array cannot be null");

    return symbols::get;
  }

  /**
   * インデックスを指定された文字列の文字に対応させるパターンを作成します。 改行文字である '\r'、'\n'はカウントされないので、Javaのテキストブロックとの併用に適しています。
   *
   * @param grid パターンの文字列-リテラル形式
   * @return the pattern
   */
  static Pattern<Character> ofGrid(String grid) {
    Objects.requireNonNull(grid, "grid cannot be null");

    Map<Integer, Character> map = new HashMap<>();

    int slot = 0;
    for (int i = 0; i < grid.length(); i++) {
      char x = grid.charAt(i);
      if (x == '\r' || x == '\n') {
        continue;
      }
      map.put(slot++, x);
    }

    return ofMap(map);
  }

  /**
   * スロットがインベントリグリッドの端にある場合、スロットを{@link Border#OUTER}にマップするパターンを作成する。 その他のスロットは{@link
   * Border#INNER}にマッピングされます。 グリッド外のスロットインデックスはNULLにマッピングされます。
   *
   * @param width インベントリグリッドの幅
   * @param height インベントリグリッドの高さ
   * @return the pattern
   */
  static BorderPattern border(int width, int height) {
    return new BorderPattern(width, height);
  }

  /**
   * すべての偶数スロットを{@link Tile#BLACK} に、すべての奇数スロットを{@link Tile#WHITE} にマッピングするパターンです。
   *
   * @param size 在庫の大きさ
   * @return the checkerboard pattern
   */
  static CheckerboardPattern checkerboard(int size) {
    return new CheckerboardPattern(size, CheckerboardPattern.Tile.BLACK);
  }

  /**
   * すべてのスロットをそのインデックスに対応させるパターン。
   *
   * @return the pattern
   */
  static Pattern<Integer> ofIndex() {
    return Integer::valueOf;
  }

  /**
   * すべてのスロットをそのスロットタイプに対応させるパターンです。 インベントリ外のインデックスは{@link SlotType#OUTSIDE}にマッピングされます。
   *
   * @param shape the shape of the inventory.
   * @return the pattern
   */
  static Pattern<SlotType> ofShape(Shape shape) {
    return shape.toPattern();
  }

  /**
   * シンボルを取得します。このパターン以外のスロットの場合、{@code null}が返されることがある。
   *
   * @param location the inventory slot index
   * @return the symbol
   */
  Symbol getSymbol(int location);

  default <Item> void apply(
      Mask<Symbol, Item> mask, IntStream indexGenerator, IntBiConsumer<Item> updater) {
    indexGenerator.forEach(
        index -> {
          Symbol symbol = getSymbol(index);
          var item = mask.getItem(symbol);
          if (item.isPresent()) {
            updater.accept(index, item.get());
          }
        });
  }

  default void applyInventory(Mask<Symbol, ItemStack> mask, Inventory inventory) {
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      Symbol symbol = getSymbol(slot);
      var item = mask.getItem(symbol);
      if (item.isPresent()) {
        inventory.setItem(slot, item.get());
      }
    }
  }

  default <P extends Plugin, MH extends MenuHolder<P>> void applyMenu(
      Mask<Symbol, ? extends MenuButton<MH>> mask, MH menu) {
    for (int slot = 0; slot < menu.getInventory().getSize(); slot++) {
      Symbol symbol = getSymbol(slot);
      var button = mask.getItem(symbol);
      if (button.isPresent()) {
        menu.setButton(slot, button.get());
      }
    }
  }
}
