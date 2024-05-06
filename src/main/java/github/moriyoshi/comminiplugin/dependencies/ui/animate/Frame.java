package github.moriyoshi.comminiplugin.dependencies.ui.animate;

import java.util.Objects;

import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.Mask;
import github.moriyoshi.comminiplugin.dependencies.ui.mask.Pattern;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.util.IntBiConsumer;
import github.moriyoshi.comminiplugin.dependencies.ui.util.IntGenerator;
import github.moriyoshi.comminiplugin.dependencies.ui.util.Option;

/**
 * アニメーションのフレームを表現する。
 *
 * @param <Symbol> the symbol type
 * @param <Item>   the container element type
 */
@SuppressWarnings("rawtypes")
public record Frame<Symbol, Item>(Pattern<Symbol> pattern, Mask<Symbol, Item> mask,
    IntGenerator activeSlots) {

  /**
   * フレームを組み立てる
   *
   * @param pattern     この枠に使われている柄
   * @param mask        パターンを使って適用されるマスク
   * @param activeSlots このフレームが適用されるスロットを指定します。
   */
  public Frame(Pattern<Symbol> pattern, Mask<Symbol, Item> mask, IntGenerator activeSlots) {
    this.pattern = Objects.requireNonNull(pattern, "pattern cannot be null");
    this.mask = Objects.requireNonNull(mask, "mask cannot be null");
    this.activeSlots = Objects.requireNonNull(activeSlots, "activeSlots cannot be null");
  }

  /**
   * コンテナにフレームを適用する。
   *
   * @param container コンテナを使用します。通常、これは
   *                  {@link MenuHolder#setButton(int, MenuButton)} or
   *                  {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)};
   */
  public void apply(IntBiConsumer<? super Item> container) {
    activeSlots.forEachRemaining((int i) -> {
      Symbol symbol = pattern.getSymbol(i);
      Option<Item> item = mask.getItem(symbol);
      if (item.isPresent()) {
        container.accept(i, item.get());
      }
    });
    activeSlots.reset();
  }

  /**
   * フレームをコピーして、新しいパターンを使用します。
   *
   * @param pattern the new pattern
   * @return a new Frame
   */
  @SuppressWarnings("unchecked")
  public Frame withNewPattern(Pattern<Symbol> pattern) {
    return new Frame(pattern, mask, activeSlots);
  }

  /**
   * フレームをコピーしますが、新しいマスクを使用します。
   *
   * @param mask the new mask
   * @return a new Frame
   */
  @SuppressWarnings("unchecked")
  public Frame withNewMask(Mask<Symbol, Item> mask) {
    return new Frame(pattern, mask, activeSlots);
  }

  /**
   * フレームをコピーして、新しいアクティブスロットを使用します。
   *
   * @param activeSlots the slots at which the new frame will be applied
   * @return a new frame
   */
  @SuppressWarnings("unchecked")
  public Frame withNewActiveSlots(IntGenerator activeSlots) {
    return new Frame(pattern, mask, activeSlots);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Frame that)) {
      return false;
    }

    return Objects.equals(this.pattern, that.pattern)
        && Objects.equals(this.mask, that.mask)
        && Objects.equals(this.activeSlots, that.activeSlots);
  }

  @Override
  public String toString() {
    return "Frame(pattern=" + pattern + ",mask=" + mask + ",activeSlots=" + activeSlots + ")";
  }

}
