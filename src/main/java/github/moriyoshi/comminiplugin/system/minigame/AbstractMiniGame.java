package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.InterfaceGame;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.IdentifierKey;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.HandlerList;

public abstract class AbstractMiniGame implements InterfaceGame {

  public final String id;
  @Getter public final PrefixUtil prefix;
  public final AbstractMiniGameListener<?> listener;
  @Getter protected World world;
  @Getter protected Location lobby;
  @Getter public final IdentifierKey key;

  public AbstractMiniGame(
      final String id,
      final PrefixUtil prefix,
      final Function<IdentifierKey, AbstractMiniGameListener<?>> func) {
    this.id = id;
    this.prefix = prefix;
    this.key = new IdentifierKey("minigame-" + id, UUID.randomUUID());
    this.listener = func.apply(key);
    MiniGameSystem.register(getKey(), this);
  }

  public final void startGame() {
    hidePlayer();
    ComMiniPlugin.getPlugin().registerEvent(listener);
    runPlayers(p -> ComMiniPlayer.getPlayer(p.getUniqueId()).setJoinGameIdentifier(getKey()));
    innerStartGame();
  }

  @Override
  public final void finishGame() {
    showPlayer();
    HandlerList.unregisterAll(listener);
    runPlayers(BukkitUtil::initializePlayer);
    innerFinishGame();
    MiniGameSystem.unregister(getKey());
  }

  protected abstract void innerStartGame();

  protected abstract void innerFinishGame();
}
