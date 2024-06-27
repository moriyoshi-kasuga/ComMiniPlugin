package github.moriyoshi.comminiplugin.system.type;

import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.system.IGame;
import java.util.UUID;

public interface IMultipleGame extends IGame {
  @Override
  default IdentifierKey createKey() {
    return new IdentifierKey(getId(), UUID.randomUUID());
  }
}
