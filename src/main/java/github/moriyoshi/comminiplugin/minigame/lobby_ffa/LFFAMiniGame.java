package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.object.LeaveMiniGameItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.minigame.AbstractMiniGame;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.bukkit.entity.Player;

public class LFFAMiniGame extends AbstractMiniGame {

  private final Map<UUID, Integer> players = new java.util.HashMap<>();

  public LFFAMiniGame() {
    super("LFFA", new PrefixUtil("<gray>[<yellow>FFA<gray>]"), LFFAListener::new);
    startGame();
  }

  @Override
  public boolean isGamePlayer(Player player) {
    return players.containsKey(player.getUniqueId());
  }

  public int incrementKill(Player player) {
    val kill = players.get(player.getUniqueId()) + 1;
    players.put(player.getUniqueId(), kill);
    return kill;
  }

  public void addPlayer(Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, 0);
    GameSystem.initializeGamePlayer(player);
    ComMiniPlayer.getPlayer(uuid)
        .setCanFoodRegain(false)
        .setJoinGameKey(getKey())
        .setHideNameTag(true);
    val inv = player.getInventory();
    inv.setItem(0, new LFFAGun().getItem());
    inv.setItem(8, new LeaveMiniGameItem().getItem());
  }

  @Override
  protected void innerStartGame() {}

  @Override
  protected void innerFinishGame() {
    players.clear();
  }

  @Override
  public void leavePlayer(Player player) {
    players.remove(player.getUniqueId());
  }
}
