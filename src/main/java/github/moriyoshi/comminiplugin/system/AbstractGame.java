package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.system.AbstractGameKey.MiniGameKey;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.HandlerList;

public abstract class AbstractGame implements IGame {

  @Getter public final String id;
  @Getter public final PrefixUtil prefix;
  private final IGameListener listener;
  @Getter protected World world;
  @Getter protected Location lobby;
  @Getter public final MiniGameKey key;

  public AbstractGame(
      final String id,
      final PrefixUtil prefix,
      final Function<IdentifierKey, IGameListener> func) {
    this.id = id;
    this.prefix = prefix;
    this.key = new MiniGameKey(id, UUID.randomUUID());
    this.listener = func.apply(key);
    GameSystem.register(getKey(), this);
  }

  public final void startGame() {
    ComMiniPlugin.getPlugin().registerEvent(listener);
    runPlayers(this::setPlayerJoinGameIdentifier);
    innerStartGame();
  }

  @Override
  public final void finishGame() {
    HandlerList.unregisterAll(listener);
    runPlayers(GameSystem::initializePlayer);
    innerFinishGame();
    GameSystem.unregister(getKey());
  }

  protected abstract void innerStartGame();

  protected abstract void innerFinishGame();
}
