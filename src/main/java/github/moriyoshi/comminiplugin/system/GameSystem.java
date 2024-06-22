package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.object.MenuItem;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GameSystem {

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
    p.setArrowsInBody(0);
  }

  public static void initializeGamePlayer(Player p) {
    p.getInventory().clear();
    p.setGameMode(GameMode.SURVIVAL);
    p.clearActivePotionEffects();
    p.setHealth(20);
    p.setExperienceLevelAndProgress(0);
    ComMiniPlayer.getPlayer(p.getUniqueId()).initialize();
  }
}
