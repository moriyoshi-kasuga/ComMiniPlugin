package github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns;

import github.moriyoshi.comminiplugin.dependencies.ui.mask.Pattern;
import java.util.Objects;

/**
 * checkerboardのタイルが白と黒で交互に並んでいます。
 * この実装は、マインクラフトのすべてのインベントリグリッドの列数（幅）が奇数であるという事実を利用しているため、非常に素朴なものです。
 */
public class CheckerboardPattern implements Pattern<CheckerboardPattern.Tile> {

  private final int size;
  private final Tile startWith;
  private CheckerboardPattern inverse;

  private CheckerboardPattern(int size, Tile startWith, CheckerboardPattern inverse) {
    this.size = size;
    this.startWith = startWith;
    this.inverse = inverse;
  }

  /**
   * CheckerboardPatternを構築する。
   *
   * @param size      インベントリグリッドの大きさ
   * @param startWith 最初のタイルの色です。
   */
  public CheckerboardPattern(int size, Tile startWith) {
    if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    }
    Objects.requireNonNull(startWith, "startWith cannot be null");

    this.size = size;
    this.startWith = startWith;
  }

  /**
   * 指定されたインデックスのタイルを取得する。
   *
   * @param index スロットのインデックス
   * @return スロットのタイル，またはインデックスが0以下またはグリッドサイズより大きい場合はNULL．
   */
  @Override
  public Tile getSymbol(int index) {
    if (index < 0 || index >= size) {
      return null;
    } else {
      return index % 2 == 0 ? startWith : startWith.other();
    }
  }

  /**
   * このパターンの逆（白と黒のタイルがすべて入れ替わる）であるチェッカーボードパターンを取得します。
   *
   * @return 逆パターン
   */
  public CheckerboardPattern inverse() {
    if (inverse == null) {
      inverse = new CheckerboardPattern(size, startWith.other(), this);
    }

    return inverse;
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, startWith);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CheckerboardPattern that)) {
      return false;
    }

    return this.size == that.size && this.startWith == that.startWith;
  }

  @Override
  public String toString() {
    return "CheckerboardPattern(size=" + size + ",startWith=" + startWith + ")";
  }

  public enum Tile {
    BLACK,
    WHITE;

    public boolean isBlack() {
      return this == BLACK;
    }

    public boolean isWhite() {
      return this == WHITE;
    }

    private Tile other() {
      return switch (this) {
        case BLACK -> WHITE;
        case WHITE -> BLACK;
      };
    }
  }

}
