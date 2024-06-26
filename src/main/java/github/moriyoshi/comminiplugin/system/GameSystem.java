package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.minigame.lobby_ffa.LFFAMiniGame;
import github.moriyoshi.comminiplugin.object.MenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameSystem {
  static final HashMap<IdentifierKey, IGame> games = new HashMap<>();

  public static void load() {
    new LFFAMiniGame();
  }

  public static void clear() {
    new ArrayList<>(games.values()).forEach(IGame::finishGame);
  }

  public static void register(IdentifierKey key, IGame game) {
    games.put(key, game);
  }

  public static void unregister(IdentifierKey key) {
    games.remove(key);
  }

  @Nullable
  public static IGame getGame(IdentifierKey key) {
    return games.get(key);
  }

  @NotNull
  public static <T extends IGame> T getGame(IdentifierKey key, Class<T> clazz) {
    return clazz.cast(games.get(key));
  }

  @NotNull
  public static <T extends IGame> T getUniqueGame(String id, Class<T> t) {
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
  public static void initializePlayer(Player p) {
    initializeGamePlayer(p);
    p.getInventory().addItem(new MenuItem().getItem());
    p.teleport(ComMiniWorld.LOBBY);
    p.playerListName(null);
  }

  public static void initializeGamePlayer(Player p) {
    p.setArrowsInBody(0);
    p.getInventory().clear();
    p.setGameMode(GameMode.SURVIVAL);
    p.clearActivePotionEffects();
    p.setHealth(20);
    p.setExperienceLevelAndProgress(0);
    ComMiniPlayer.getPlayer(p.getUniqueId()).initialize(p);
  }
}
