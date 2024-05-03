package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.constant.MenuItem;
import github.moriyoshi.comminiplugin.dependencies.fastboard.FastBoard;
import github.moriyoshi.comminiplugin.game.survivalsniper.SurvivalSniperGame;
import java.util.HashMap;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class GameSystem {

  public static final FastBoard board = new FastBoard(
      Bukkit.getOnlinePlayers().stream().map(p -> (Player) p).toList());

  public static final HashMap<String, AbstractGame> games = new HashMap<>() {
    {
      List.of(
          // ALl Game
          SurvivalSniperGame.getInstance()
      // End
      ).forEach(g -> put(g.id, g));
    }
  };
  private static boolean _boardShowing = false;
  private static AbstractGame _nowGame = null;

  public static boolean boardShowing() {
    return _boardShowing;
  }

  public static void boardShow() {
    _boardShowing = true;
    Bukkit.getOnlinePlayers().forEach(board::addViewer);
    board.show();
  }

  public static void boardHide() {
    _boardShowing = false;
    board.hide();
    board.removeViewers();
  }

  public static AbstractGame nowGame() {
    return _nowGame;
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
          "<green>現在は <u>" + _nowGame.name + "<reset><green>が開催されています!");
      return false;
    }
    if (!games.containsKey(gameName)) {
      ComMiniPrefix.OP.send(player,
          "<red>" + gameName + "ゲームの識別子が正しくありません! 開発者に連絡してください。");
      return false;
    }
    var temp = games.get(gameName);
    if (!temp.initializeGame(player)) {
      ComMiniPrefix.OP.send(player,
          "<red>" + gameName + "を始められません、初期化条件が存在します!");
      return false;
    }
    _nowGame = temp;
    _nowGame.prefix.cast("<green>開催します!");
    _nowGame._isRunning = true;
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
      _nowGame.prefix.send(player, "<red>すでに始まっています!");
      return false;
    }
    if (!_nowGame.startGame(player)) {
      return false;
    }
    _nowGame._isStarted = true;
    _nowGame.prefix.cast("<green>開始します");
    ComMiniPlugin.getPlugin().registerEvent(_nowGame.listener);
    return true;
  }

  public static boolean inGame() {
    return _nowGame != null;
  }

  public static boolean isStarted() {
    return inGame() && _nowGame._isStarted;
  }

  public static boolean canOpenMenu() {
    return inGame() && _nowGame._canOpenMenu;
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
    _nowGame._isRunning = false;
    _nowGame._isStarted = false;
    _nowGame._canOpenMenu = true;
    HandlerList.unregisterAll(_nowGame.listener);
    _nowGame.runPlayers(p -> {
      GamePlayer.getPlayer(p.getUniqueId()).initialize();
      p.getInventory().clear();
      p.getInventory().addItem(new MenuItem().getItem());
      p.setGameMode(GameMode.SURVIVAL);
      p.teleport(ComMiniWorld.LOBBY);
    });
    _nowGame.finishGame();
    _nowGame.prefix.cast("<green>閉幕です");
    _nowGame = null;
    board.updateLines(Component.empty());
    return true;
  }

}
