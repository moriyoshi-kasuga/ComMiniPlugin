package github.moriyoshi.comminiplugin.lib.tuple;

import lombok.Data;

@Data(staticConstructor = "of")
public class Triple<A, B, C> {
  private final A first;
  private final B second;
  private final C third;
}
