package github.moriyoshi.comminiplugin.system.type;

import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.system.IGame;

public interface IUniqueGame extends IGame {

  @Override
  default IdentifierKey createKey() {
    return new IdentifierKey(getId(), null);
  }
}
