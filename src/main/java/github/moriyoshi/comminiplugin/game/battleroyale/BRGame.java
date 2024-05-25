package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.util.PrefixUtil;

public class BRGame extends AbstractGame {

  public BRGame() {
    super(
        "battleroyale",
        "<yellow>バトルロワイヤル",
        "<yellow>殺せ!殺せ!勝ち上がれ!",
        Material.GOLDEN_SWORD,
        new PrefixUtil("<gray>[<yellow>BattleRoyale<gray>]"),
        new BRListener());
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new BRAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(Player player) {
    return new BRMenu();
  }

  @Override
  public boolean addSpec(Player player) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addSpec'");
  }

  @Override
  public boolean initializeGame(Player player) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'initializeGame'");
  }

  @Override
  protected boolean innerStartGame(Player player) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'innerStartGame'");
  }

  @Override
  protected void innerFinishGame() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'innerFinishGame'");
  }

  @Override
  public boolean isGamePlayer(Player player) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isGamePlayer'");
  }

  @Override
  protected void fieldInitialize(boolean isCreatingInstance) {
  }

}
