package github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns;

import github.moriyoshi.comminiplugin.dependencies.ui.mask.Pattern;
import java.util.Objects;

/**
 * このパターンは、インベントリグリッドのすべてのエッジを {@link Border#OUTER}としてハイライトします。 端にないスロットは {@link
 * Border#INNER}と表示されます。
 */
public class BorderPattern implements Pattern<BorderPattern.Border> {

  private final int width, height;

  /**
   * BorderPatternを構築する
   *
   * @param width インベントリグリッドの幅
   * @param height インベントリグリッドの高さを指定します。
   */
  public BorderPattern(int width, int height) {
    if (width < 0) {
      throw new IllegalArgumentException("Negative width: " + width);
    }
    if (height < 0) {
      throw new IllegalArgumentException("Negative height: " + height);
    }

    this.width = width;
    this.height = height;
  }

  /**
   * シンボルを取得します。
   *
   * @param index インベントリスロット
   * @return スロットがグリッドの端にある場合は {@link Border#OUTER}、インデックスが範囲外の場合は {@code null}、それ以外は {@link
   *     Border#INNER}となります。
   */
  @Override
  public Border getSymbol(int index) {
    if (index < 0) {
      return null;
    }
    int size = width * height;

    if (index >= size) {
      return null;
    }

    // top border
    if (index < width) {
      return Border.OUTER;
    }
    // bottom border
    if (index > size - width) {
      return Border.OUTER;
    }
    // left border
    if (index % width == 0) {
      return Border.OUTER;
    }
    // right border
    if (index % width == width - 1) {
      return Border.OUTER;
    }

    return Border.INNER;
  }

  @Override
  public int hashCode() {
    return Objects.hash(width, height);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof BorderPattern that)) {
      return false;
    }

    return this.width == that.width && this.height == that.height;
  }

  @Override
  public String toString() {
    return "BorderPattern(width=" + width + ",height=" + height + ")";
  }

  public enum Border {
    OUTER,
    INNER;

    public boolean isOuter() {
      return this == OUTER;
    }

    public boolean isInner() {
      return this == INNER;
    }
  }
}
