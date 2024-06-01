package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;

public class SSAdminMenu extends MenuHolder<ComMiniPlugin> {

  public SSAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:サバイバルスナイパー");
    setButton(13, GameStartButton.of());
  }
}
