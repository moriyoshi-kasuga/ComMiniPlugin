package github.moriyoshi.comminiplugin.dependencies.ui.mask.patterns;

import github.moriyoshi.comminiplugin.dependencies.ui.mask.Pattern;
import java.util.stream.IntStream;

public class HeightPattern implements Pattern<Boolean> {

  private final int width;

  private final int start;

  private final int end;

  private final IntStream intStream;

  private HeightPattern(int width, int start, int end) {
    this.width = width;
    if (start >= end) {
      throw new IllegalArgumentException("start が end 以下の場合、HeightPattern を作成できません");
    }
    this.start = start;
    this.end = end;
    this.intStream = IntStream.rangeClosed(start, end);
  }

  public static HeightPattern of(int width) {
    return new HeightPattern(width, 0, 5);
  }

  public static HeightPattern of(int width, int start, int end) {
    return new HeightPattern(width, start, end);
  }

  @Override
  public Boolean getSymbol(int location) {
    int inventoryColumn = location % 9;
    if (width != inventoryColumn) {
      return false;
    }
    int inventoryRow = location / 9;
    return intStream.anyMatch(value -> value == inventoryRow);
  }

  public int getWidth() {
    return width;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

}
