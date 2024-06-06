package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;

public class BRHelpMenu extends MenuHolder<ComMiniPlugin> {

  public BRHelpMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<yellow>ヘルプ");
  }
}
