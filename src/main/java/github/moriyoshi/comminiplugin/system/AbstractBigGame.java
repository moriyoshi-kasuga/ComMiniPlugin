package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.system.type.IUniqueGame;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBigGame extends AbstractGame implements IUniqueGame {

  public AbstractBigGame(
      Player player, PrefixUtil prefix, Function<IdentifierKey, IGameListener<?>> listener)
      throws GameInitializeFailedException {
    super(player, prefix, listener);
  }

  public abstract Material getIcon();

  public abstract MenuHolder<ComMiniPlugin> createHelpMenu();

  public abstract MenuHolder<ComMiniPlugin> createAdminMenu();

  public abstract MenuHolder<ComMiniPlugin> createGameMenu(Player player);

  @Override
  public final void predicateInitialize(@Nullable Player player)
      throws GameInitializeFailedException {
    if (player == null) {
      throw new GameInitializeFailedException("<red>BigGameの初期化にはプレイヤーが必要です");
    }
    if (GameSystem.getGames().stream().anyMatch(game -> game instanceof AbstractBigGame)) {
      throw new GameInitializeFailedException("<red>既にBigGameは開催されています");
    }
    predicateInnerInitialize(player);
  }

  protected void predicateInnerInitialize(@NotNull Player player)
      throws GameInitializeFailedException {}
}
