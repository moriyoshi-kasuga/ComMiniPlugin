package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import java.util.function.Function;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public abstract class AbstractGame implements IGame {

  @Getter public final PrefixUtil prefix;
  final IGameListener<?> listener;
  @Getter protected World world;
  @Getter protected Location lobby;
  @Getter public final IdentifierKey key;
  @Getter private boolean isStarted = false;

  /**
   * please call {@code GameSystem#createGame(java.util.function.Supplier)}
   *
   * @throws GameInitializeFailedException
   */
  public AbstractGame(PrefixUtil prefix, Function<IdentifierKey, IGameListener<?>> listener)
      throws GameInitializeFailedException {
    predicateInitialize();
    this.prefix = prefix;
    this.key = createKey();
    this.listener = listener.apply(key);
    GameSystem.register(getKey(), this);
    this.fieldInitialize(true);
  }

  public final boolean addSpec(Player player) {
    if (predicateSpec(player)) {
      getPlayers()
          .forEach(
              shower -> {
                player.showPlayer(ComMiniPlugin.getPlugin(), shower);
                shower.showPlayer(ComMiniPlugin.getPlugin(), player);
              });
      ComMiniPlayer.getPlayer(player.getUniqueId()).setJoinGameKey(getKey());
      innerAddSpec(player);
      return true;
    }
    return false;
  }

  public final boolean startGame(Audience audience) {
    if (!predicateStartGame(audience)) {
      return false;
    }
    hidePlayers();
    val list = getPlayers();
    runPlayers(
        p -> {
          val gp = GameSystem.initializeGamePlayer(p);
          gp.setJoinGameKey(getKey());
          list.forEach(s -> p.showPlayer(ComMiniPlugin.getPlugin(), s));
        });
    isStarted = true;
    innerStartGame();
    ComMiniPlugin.getPlugin().registerEvent(listener);
    return true;
  }

  @Override
  public final void finishGame() {
    isStarted = false;
    HandlerList.unregisterAll(listener);
    showPlayers();
    runPlayers(GameSystem::initializePlayer);
    innerFinishGame();
    fieldInitialize(false);
    GameSystem.unregister(getKey());
  }

  protected void fieldInitialize(boolean isCreatingInstance) {}

  protected abstract void innerStartGame();

  protected abstract void innerFinishGame();
}
