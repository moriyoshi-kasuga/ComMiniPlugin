package github.moriyoshi.comminiplugin.system.game;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.InterfaceGame;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.IdentifierKey;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public abstract class AbstractGame implements InterfaceGame {

  public final String id;
  public final String name;
  public final String description;
  public final Material material;
  @Getter public final PrefixUtil prefix;
  public final AbstractGameListener<?> listener;
  @Getter protected World world;
  @Getter protected Location lobby;
  @Getter private boolean isStarted = false;
  @Getter private final IdentifierKey key;

  public AbstractGame(
      final String id,
      final String name,
      final String description,
      final Material material,
      final PrefixUtil prefix,
      final AbstractGameListener<?> listener) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.material = material;
    this.prefix = prefix;
    this.listener = listener;
    this.key = new IdentifierKey("game-" + id, null);
    this.fieldInitialize(true);
  }

  public abstract MenuHolder<ComMiniPlugin> createAdminMenu();

  public abstract MenuHolder<ComMiniPlugin> createGameMenu(Player player);

  public final boolean startGame(final Player player) {
    if (!innerStartGame(player)) {
      return false;
    }
    hidePlayer();
    runPlayers(p -> ComMiniPlayer.getPlayer(p.getUniqueId()).setJoinGameIdentifier(getKey()));
    isStarted = true;
    ComMiniPlugin.getPlugin().registerEvent(listener);
    return true;
  }

  @Override
  public final void finishGame() {
    isStarted = false;
    HandlerList.unregisterAll(listener);
    showPlayer();
    runPlayers(BukkitUtil::initializePlayer);
    innerFinishGame();
    fieldInitialize(false);
  }

  protected abstract void fieldInitialize(boolean isCreatingInstance);

  protected abstract boolean innerStartGame(Player player);

  protected abstract void innerFinishGame();

  /**
   * ゲームが始まってからの観戦
   *
   * @param player 観戦させたい人
   * @return true で参加させ、false で観戦不可能
   */
  public abstract boolean addSpec(Player player);

  /**
   * ゲームの初期化
   *
   * @param player 初期化する人
   * @return true で初期化し、false で初期化不可能
   */
  public abstract boolean initializeGame(Player player);

  public abstract MenuHolder<ComMiniPlugin> createHelpMenu();
}
