package github.moriyoshi.comminiplugin.util.tuple;

import lombok.Data;

@Data(staticConstructor = "of")
public class Sequence<A, B, C, D> {
  private final A first;
  private final B second;
  private final C third;
  private final D fourth;
}
