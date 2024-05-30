package github.moriyoshi.comminiplugin.util;

import lombok.Data;

@Data(staticConstructor = "of")
public class Triple<A, B, C> {
  private final A first;
  private final B second;
  private final C third;
}
