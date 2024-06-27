package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.Result;
import github.moriyoshi.comminiplugin.lib.Result.Err;
import github.moriyoshi.comminiplugin.lib.Result.Ok;
import github.moriyoshi.comminiplugin.minigame.lobby_ffa.LFFAMiniGame;
import github.moriyoshi.comminiplugin.object.MenuItem;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedException;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedSupplier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameSystem {
  static final HashMap<IdentifierKey, AbstractGame> games = new HashMap<>();

  public static Collection<AbstractGame> getGames() {
    return games.values();
  }

  public static void load() {
    createGame(LFFAMiniGame::new).throwOk();
  }

  public static void clear() {
    new ArrayList<>(games.values()).forEach(AbstractGame::finishGame);
  }

  /**
   * ゲームを作成します
   *
   * @param supplier create game supplier
   * @return look {@link Result}
   */
  @NotNull
  public static <T extends AbstractGame> Result<T, GameInitializeFailedException> createGame(
      GameInitializeFailedSupplier<T> supplier) {
    try {
      return Ok.of(supplier.get());
    } catch (GameInitializeFailedException e) {
      return Err.of(e);
    }
  }

  @Nullable
  public static AbstractGame getGame(IdentifierKey key) {
    return games.get(key);
  }

  @NotNull
  public static <T extends AbstractGame> T getGame(IdentifierKey key, Class<T> clazz) {
    return clazz.cast(games.get(key));
  }

  @NotNull
  public static <T extends AbstractGame> T getUniqueGame(String id, Class<T> t) {
    return t.cast(
        games.entrySet().stream()
            .filter(x -> x.getKey().getIdentifier().equals(id))
            .findFirst()
            .orElseThrow()
            .getValue());
  }

  /**
   * サーバー参加時やロビーに返す時、ゲーム終了時に使えるメゾット
   *
   * @param p target player
   */
  public static ComMiniPlayer initializePlayer(Player p) {
    val player = initializeGamePlayer(p);
    p.getInventory().addItem(new MenuItem().getItem());
    p.teleport(ComMiniWorld.LOBBY);
    p.playerListName(null);
    return player;
  }

  public static ComMiniPlayer initializeGamePlayer(Player p) {
    p.setArrowsInBody(0);
    p.getInventory().clear();
    p.setGameMode(GameMode.SURVIVAL);
    p.clearActivePotionEffects();
    p.setHealth(20);
    p.setExperienceLevelAndProgress(0);
    val player = ComMiniPlayer.getPlayer(p.getUniqueId());
    player.initialize(p);
    return player;
  }

  static void register(IdentifierKey key, AbstractGame game) {
    games.put(key, game);
  }

  static void unregister(IdentifierKey key) {
    games.remove(key);
  }
}
