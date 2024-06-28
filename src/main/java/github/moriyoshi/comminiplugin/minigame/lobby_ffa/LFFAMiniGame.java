package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.object.LeaveMiniGameItem;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.type.INoSpectatorGame;
import github.moriyoshi.comminiplugin.system.type.IUniqueGame;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LFFAMiniGame extends AbstractGame implements INoSpectatorGame, IUniqueGame {

  private final Map<UUID, Integer> players = new java.util.HashMap<>();

  public LFFAMiniGame() throws GameInitializeFailedException {
    super(new PrefixUtil("<gray>[<yellow>FFA<gray>]"), LFFAListener::new);
    startGame(Bukkit.getConsoleSender());
  }

  @Override
  public String getId() {
    return "LFFA";
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
  public void leavePlayer(Player player) {
    players.remove(player.getUniqueId());
  }

  @Override
  public void predicateInitialize(Player player) throws GameInitializeFailedException {}

  @Override
  public String getName() {
    return "Lobby FFA";
  }

  @Override
  public String getDescription() {
    return "銃を左クリックで撃って、キルストリークを上げよう!";
  }

  @Override
  public boolean predicateStartGame(Audience audience) {
    return true;
  }

  @Override
  protected void innerStartGame() {}

  @Override
  protected void innerFinishGame() {
    players.clear();
  }
}
