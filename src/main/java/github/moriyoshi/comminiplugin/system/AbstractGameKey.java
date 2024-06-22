package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import java.util.UUID;

public sealed class AbstractGameKey extends IdentifierKey {

  private AbstractGameKey(String identifier, UUID uuid) {
    super(identifier, uuid);
  }

  public boolean isMiniGameKey() {
    return this instanceof MiniGameKey;
  }

  public boolean isBigGameKey() {
    return this instanceof BigGameKey;
  }

  public static final class MiniGameKey extends AbstractGameKey {
    public MiniGameKey(String identifier, UUID uuid) {
      super("minigame-" + identifier, uuid);
    }
  }

  public static final class BigGameKey extends AbstractGameKey {
    public BigGameKey(String identifier) {
      super("game-" + identifier, null);
    }
  }
}
