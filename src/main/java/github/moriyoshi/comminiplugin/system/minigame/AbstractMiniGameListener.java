package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.system.InterfaceGameListener;

public interface AbstractMiniGameListener<T extends AbstractMiniGame>
    extends InterfaceGameListener, IGetMiniGame<T> {
}
