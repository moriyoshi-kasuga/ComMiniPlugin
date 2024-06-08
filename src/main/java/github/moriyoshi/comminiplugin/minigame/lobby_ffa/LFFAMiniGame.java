package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.system.minigame.AbstractMiniGame;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import lombok.val;

import java.util.Map;
import java.util.UUID;
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

  public void addPlayer(Player player) {
    players.put(player.getUniqueId(), 0);
    val inv = player.getInventory();
    inv.setItem(0, new LFFAGun().getItem());
    inv.clear();
  }

  @Override
  protected void innerStartGame() {}

  @Override
  protected void innerFinishGame() {
    players.clear();
  }
}
