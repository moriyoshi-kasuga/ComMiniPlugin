package github.moriyoshi.comminiplugin.system.biggame;

import github.moriyoshi.comminiplugin.system.IGameListener;

public interface AbstractBigGameListener<T extends AbstractBigGame>
    extends IGameListener, IGetBigGame<T> {}
