package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.IGetGame;

public class BRMenu extends MenuHolder<ComMiniPlugin> implements IGetGame<BRGame> {

  public BRMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<yellow>バトルロワイヤル");
  }

}
