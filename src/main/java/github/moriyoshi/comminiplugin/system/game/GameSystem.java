package github.moriyoshi.comminiplugin.system.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.constant.MenuItem;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame;
import lombok.Getter;

public class GameSystem {

  public static final HashMap<String, AbstractGame> games = new HashMap<>() {
    {
      List.of(
          // ALl Game
          new SSGame()
      // End
      ).forEach(g -> put(g.id, g));
    }
  };

  public static final List<AbstractMiniGame> minigames = new ArrayList<>();

  @Getter
  private static AbstractGame nowGame = null;

  public static <T extends AbstractGame> T getNowGame(Class<T> t) {
    return t.cast(nowGame);
  }

  /**
   * 運営がこの関数を通してゲームを呼びます
   *
   * @param player   呼び出す運営
   * @param gameName 呼び出すゲーム
   * @return 呼び出せたらtrue
   */
  public static boolean initializeGame(Player player, String gameName) {
    if (inGame()) {
      ComMiniPrefix.SYSTEM.send(
          player,
          "<green>現在は <u>" + nowGame.name + "<reset><green>が開催されています!");
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
    nowGame = temp;
    nowGame.prefix.cast("<green>開催します!");
    return true;
  }

  /**
   * ゲームを開始します、initializeGameではただゲームを開催するということだけ
   *
   * @param player スタートする運営
   * @return スタートできたらtrue
   */
  public static boolean startGame(Player player) {
    if (!inGame()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>現在は何も開催されていません!");
      return false;
    }
    if (isStarted()) {
      nowGame.prefix.send(player, "<red>すでに始まっています!");
      return false;
    }
    if (!nowGame.startGame(player)) {
      return false;
    }
    nowGame.prefix.cast("<green>開始します");
    return true;
  }

  /**
   * ここではゲームの設定をすべてclearしてゲームを終了します
   *
   * @return trueでゲーム終了
   */
  public static boolean finalizeGame() {
    if (!inGame()) {
      return false;
    }
    nowGame.finishGame();
    nowGame.prefix.cast("<green>閉幕です");
    nowGame = null;
    return true;
  }

  public static boolean inGame() {
    return nowGame != null;
  }

  public static boolean isStarted() {
    return inGame() && nowGame.isStarted();
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
