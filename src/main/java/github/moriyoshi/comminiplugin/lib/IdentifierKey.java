package github.moriyoshi.comminiplugin.lib;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Getter
public class IdentifierKey {
  private final String identifier;
  private final java.util.UUID uuid;
}
