package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedSupplier;
import java.lang.ref.WeakReference;
import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BigGameSystem {

  static WeakReference<AbstractBigGame> gameRef = new WeakReference<>(null);

  public static <T extends AbstractBigGame> T getGame(Class<T> t) {
    return t.cast(gameRef.get());
  }

  @Nullable
  public static AbstractBigGame getGame() {
    return gameRef.get();
  }

  /**
   * 運営がこの関数を通してゲームを呼びます
   *
   * @param player 呼び出す運営
   * @param supplier 呼び出すゲーム
   * @return 呼び出せたらtrue
   */
  public static <T extends AbstractBigGame> boolean initializeGame(
      Player player, GameInitializeFailedSupplier<T> supplier) {
    if (isIn()) {
      ComMiniPlugin.SYSTEM.send(
          player, "<red>現在は <u>" + gameRef.get().getName() + "<reset><red>が開催されています!");
      return false;
    }
    val result = GameSystem.createGame(supplier);
    if (result.isErr()) {
      ComMiniPlugin.SYSTEM.send(player, result.unwrapErr().getMessage());
      ComMiniPlugin.SYSTEM.send(player, "<red>ゲームを始められません、初期化条件が存在します!");
      return false;
    }
    gameRef = new WeakReference<>(result.unwrap());
    gameRef.get().prefix.broadCast("<green>開催します!");
    return true;
  }

  /**
   * ゲームを開始します、initializeGameではただゲームを開催するということだけ
   *
   * @param player スタートする運営
   */
  public static void startGame(Player player) {
    val game = gameRef.get();
    if (game == null) {
      ComMiniPlugin.SYSTEM.send(player, "<red>現在は何も開催されていません!");
      return;
    }
    if (game.isStarted()) {
      game.prefix.send(player, "<red>すでに始まっています!");
      return;
    }
    if (game.startGame(player)) {
      game.prefix.broadCast("<green>開始します");
    }
  }

  /**
   * ここではゲームの設定をすべてclearしてゲームを終了します
   *
   * @return trueでゲーム終了
   */
  public static boolean finalGame() {
    val game = gameRef.get();
    if (game == null) {
      return false;
    }
    game.finishGame();
    game.prefix.broadCast("<green>閉幕です");
    return true;
  }

  public static boolean isIn() {
    return gameRef.get() != null;
  }

  public static boolean isIn(Class<? extends AbstractBigGame> clazz) {
    val game = gameRef.get();
    return game != null && clazz.isAssignableFrom(game.getClass());
  }

  public static boolean isStarted() {
    val game = gameRef.get();
    return game != null && game.isStarted();
  }

  public static boolean isStarted(Class<? extends AbstractBigGame> clazz) {
    val game = gameRef.get();
    return game != null && game.isStarted() && clazz.isAssignableFrom(game.getClass());
  }
}
