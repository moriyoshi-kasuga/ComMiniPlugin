package github.moriyoshi.comminiplugin.system;

import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.constant.MenuItem;
import github.moriyoshi.comminiplugin.game.battleroyale.BRGame;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame;
import lombok.Getter;

public class GameSystem {

  public static final HashMap<String, AbstractGame> games = new HashMap<>() {
    {
      List.of(
          // ALl Game
          new SSGame(),
          new BRGame()
      // End
      ).forEach(g -> put(g.id, g));
    }
  };

  @Getter
  private static AbstractGame game = null;

  public static <T extends AbstractGame> T getGame(Class<T> t) {
    return t.cast(game);
  }

  /**
   * 運営がこの関数を通してゲームを呼びます
   *
   * @param player   呼び出す運営
   * @param gameName 呼び出すゲーム
   * @return 呼び出せたらtrue
   */
  public static boolean initializeGame(Player player, String gameName) {
    if (isIn()) {
      ComMiniPrefix.SYSTEM.send(
          player,
          "<green>現在は <u>" + game.name + "<reset><green>が開催されています!");
      return false;
    }
    if (!games.containsKey(gameName)) {
      ComMiniPrefix.SYSTEM.important(
          "<red>" + gameName + "ゲームの識別子が正しくありません! 開発者に連絡してください。");
      return false;
    }
    var temp = games.get(gameName);
    if (!temp.initializeGame(player)) {
      ComMiniPrefix.SYSTEM.send(player,
          "<red>" + gameName + "を始められません、初期化条件が存在します!");
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
      ComMiniPrefix.SYSTEM.send(player, "<red>現在は何も開催されていません!");
    }
    if (isStarted()) {
      game.prefix.send(player, "<red>すでに始まっています!");
    }
    game.startGame(player);
    game.prefix.cast("<green>開始します");
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

  public static boolean isStarted() {
    return isIn() && game.isStarted();
  }

  /**
   * サーバー参加時やロビーに返す時、ゲーム終了時に使えるメゾット
   *
   * @param p target player
   */
  public static void initializePlayer(Player p) {
    GamePlayer.getPlayer(p.getUniqueId()).initialize();
    p.getInventory().clear();
    p.getInventory().addItem(new MenuItem().getItem());
    p.setExperienceLevelAndProgress(0);
    p.teleport(ComMiniWorld.LOBBY);
    p.setGameMode(GameMode.SURVIVAL);
    p.clearActivePotionEffects();
  }

}
