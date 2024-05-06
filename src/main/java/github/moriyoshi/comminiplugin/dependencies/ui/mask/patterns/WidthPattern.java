package github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns;

import github.moriyoshi.comminiplugin.dependencies.ui.mask.Pattern;
import java.util.stream.IntStream;
import lombok.Getter;

public class WidthPattern implements Pattern<Boolean> {

  @Getter
  private final int height;

  @Getter
  private final int start;

  @Getter
  private final int end;

  private final IntStream intStream;

  private WidthPattern(int height) {
    this(height, 0, 8);
  }

  private WidthPattern(int height, int start, int end) {
    this.height = height;
    if (start >= end) {
      throw new IllegalArgumentException("start が end 以下の場合、WithPattern を作成できません");
    }
    this.start = start;
    this.end = end;
    this.intStream = IntStream.rangeClosed(start, end);
  }

  public static WidthPattern of(int height) {
    return new WidthPattern(height);
  }

  public static WidthPattern of(int height, int start, int end) {
    return new WidthPattern(height, start, end);
  }

  @Override
  public Boolean getSymbol(int location) {
    int inventoryRow = location / 9;
    if (height != inventoryRow) {
      return false;
    }
    int inventoryColumn = location % 9;
    return intStream.anyMatch(value -> value == inventoryColumn);
  }

}
