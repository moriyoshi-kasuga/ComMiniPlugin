package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;

public class BRAdminMenu extends MenuHolder<ComMiniPlugin> {
  public BRAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:バトルロワイヤル");
    setButton(13, new GameStartButton());
  }
}
