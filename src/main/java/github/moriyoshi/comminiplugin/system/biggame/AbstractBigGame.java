package github.moriyoshi.comminiplugin.system.biggame;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.system.AbstractGameKey.BigGameKey;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.IGame;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public abstract class AbstractBigGame implements IGame {

  public final String id;
  public final String name;
  public final String description;
  public final Material material;
  @Getter public final PrefixUtil prefix;
  public final AbstractBigGameListener<?> listener;
  @Getter protected World world;
  @Getter protected Location lobby;
  @Getter private boolean isStarted = false;
  @Getter private final BigGameKey key;

  public AbstractBigGame(
      final String id,
      final String name,
      final String description,
      final Material material,
      final PrefixUtil prefix,
      final AbstractBigGameListener<?> listener) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.material = material;
    this.prefix = prefix;
    this.listener = listener;
    this.key = new BigGameKey(id);
    this.fieldInitialize(true);
  }

  public abstract MenuHolder<ComMiniPlugin> createAdminMenu();

  public abstract MenuHolder<ComMiniPlugin> createGameMenu(Player player);

  public final boolean startGame(final Player player) {
    if (!predicateGame(player)) {
      return false;
    }
    hidePlayers();
    val list = getPlayers();
    runPlayers(
        p -> {
          GameSystem.initializeGamePlayer(p);
          setPlayerJoinGameIdentifier(p);
          list.forEach(
              s -> {
                p.showPlayer(ComMiniPlugin.getPlugin(), s);
              });
        });
    innerStartGame();
    isStarted = true;
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
  }

  protected abstract void fieldInitialize(boolean isCreatingInstance);

  protected abstract void innerStartGame();

  protected abstract boolean predicateGame(Player player);

  protected abstract void innerFinishGame();

  /**
   * ゲームが始まってからの観戦
   *
   * @param player 観戦させたい人
   * @return true で参加させ、false で観戦不可能
   */
  protected abstract void innerAddSpec(Player player);

  protected abstract boolean predicateSpec(Player player);

  public final boolean addSpec(Player player) {
    if (predicateSpec(player)) {
      getPlayers()
          .forEach(
              shower -> {
                player.showPlayer(ComMiniPlugin.getPlugin(), shower);
                shower.showPlayer(ComMiniPlugin.getPlugin(), player);
              });
      innerAddSpec(player);
      return true;
    }
    return false;
  }

  /**
   * ゲームの初期化
   *
   * @param player 初期化する人
   * @return true で初期化し、false で初期化不可能
   */
  public abstract boolean initializeGame(Player player);

  public abstract MenuHolder<ComMiniPlugin> createHelpMenu();
}
