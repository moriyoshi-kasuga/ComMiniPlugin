package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.system.minigame.AbstractMiniGameListener;
import github.moriyoshi.comminiplugin.util.IdentifierKey;
import lombok.Getter;

public class LFFAListener implements AbstractMiniGameListener<LFFAMiniGame> {

  @Getter private final IdentifierKey key;

  public LFFAListener(IdentifierKey key) {
    this.key = key;
  }
}
