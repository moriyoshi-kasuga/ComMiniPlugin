package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.system.type.IUniqueGame;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class AbstractBigGame extends AbstractGame implements IUniqueGame {

  public AbstractBigGame(PrefixUtil prefix, Function<IdentifierKey, IGameListener<?>> listener)
      throws GameInitializeFailedException {
    super(prefix, listener);
  }

  public abstract Material getIcon();

  public abstract MenuHolder<ComMiniPlugin> createHelpMenu();

  public abstract MenuHolder<ComMiniPlugin> createAdminMenu();

  public abstract MenuHolder<ComMiniPlugin> createGameMenu(Player player);

  @Override
  public void predicateInitialize() throws GameInitializeFailedException {
    if (GameSystem.getGames().stream().anyMatch(game -> game instanceof AbstractBigGame)) {
      throw new GameInitializeFailedException("<red>既にBigGameは開催されています");
    }
  }
}
