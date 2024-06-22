package github.moriyoshi.comminiplugin.system.biggame;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigBigGame;
import github.moriyoshi.comminiplugin.biggame.survivalsniper.SSBigBigGame;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

public class BigGameSystem {

  static final HashMap<String, AbstractBigGame> games = new HashMap<>();

  public static void load() {
    List.of(
            // ALl Game
            new SSBigBigGame(), new BRBigBigGame()
            // End
            )
        .forEach(g -> games.put(g.id, g));
  }

  @Getter private static AbstractBigGame game = null;

  public static <T extends AbstractBigGame> T getGame(Class<T> t) {
    return t.cast(game);
  }

  /**
   * 運営がこの関数を通してゲームを呼びます
   *
   * @param player 呼び出す運営
   * @param gameName 呼び出すゲーム
   * @return 呼び出せたらtrue
   */
  public static boolean initializeGame(Player player, String gameName) {
    if (isIn()) {
      ComMiniPlugin.SYSTEM.send(player, "<green>現在は <u>" + game.name + "<reset><green>が開催されています!");
      return false;
    }
    var temp = games.get(gameName);
    if (temp == null) {
      ComMiniPlugin.SYSTEM.important("<red>" + gameName + "ゲームの識別子が正しくありません! 開発者に連絡してください。");
      return false;
    }
    if (!temp.initializeGame(player)) {
      ComMiniPlugin.SYSTEM.send(player, "<red>" + gameName + "を始められません、初期化条件が存在します!");
      return false;
    }
    game = temp;
    game.prefix.cast("<green>開催します!");
    return true;
  }

  /**
   * ゲームを開始します、initializeGameではただゲームを開催するということだけ
   *
   * @param player スタートする運営
   */
  public static void startGame(Player player) {
    if (!isIn()) {
      ComMiniPlugin.SYSTEM.send(player, "<red>現在は何も開催されていません!");
    }
    if (isStarted()) {
      game.prefix.send(player, "<red>すでに始まっています!");
      return;
    }
    if (game.startGame(player)) {
      game.prefix.cast("<green>開始します");
    }
  }

  /**
   * ここではゲームの設定をすべてclearしてゲームを終了します
   *
   * @return trueでゲーム終了
   */
  public static boolean finalGame() {
    if (!isIn()) {
      return false;
    }
    game.finishGame();
    game.prefix.cast("<green>閉幕です");
    game = null;
    return true;
  }

  public static boolean isIn() {
    return game != null;
  }

  public static boolean isIn(Class<? extends AbstractBigGame> clazz) {
    return game != null && clazz.isAssignableFrom(game.getClass());
  }

  public static boolean isStarted() {
    return isIn() && game.isStarted();
  }

  public static boolean isStarted(Class<? extends AbstractBigGame> clazz) {
    return isIn() && game.isStarted() && clazz.isAssignableFrom(game.getClass());
  }
}
