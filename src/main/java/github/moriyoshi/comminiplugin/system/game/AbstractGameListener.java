package github.moriyoshi.comminiplugin.system.game;

import github.moriyoshi.comminiplugin.system.InterfaceGameListener;

public interface AbstractGameListener<T extends AbstractGame>
    extends InterfaceGameListener, IGetGame<T> {}
