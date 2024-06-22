package github.moriyoshi.comminiplugin.system.biggame;

import github.moriyoshi.comminiplugin.system.InterfaceGameListener;

public interface AbstractBigGameListener<T extends AbstractBigGame>
    extends InterfaceGameListener, IGetBigGame<T> {}
