package github.moriyoshi.comminiplugin.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * GamePlayer
 */
public class GamePlayer {

  private static final HashMap<UUID, GamePlayer> players = new HashMap<>();

  public static final GamePlayer getPlayer(UUID uuid) {
    if (players.containsKey(uuid)) {
      return players.get(uuid);
    }
    var p = new GamePlayer(uuid);
    players.put(uuid, p);
    return p;
  }

  private final UUID uuid;

  private GamePlayer(UUID uuid) {
    this.uuid = uuid;
    this.initialize();
  }

  private final List<Consumer<UUID>> consumers = new ArrayList<>();

  public GamePlayer addInitialize(Consumer<UUID> consumer) {
    consumers.add(consumer);
    return this;
  }

  public void initialize() {
    this.isHunger = false;
    this.consumers.forEach(c -> c.accept(this.uuid));
  }

  private boolean isHunger;

  public boolean isHunger() {
    return isHunger;
  }

  public GamePlayer setHunger(boolean isHunger) {
    this.isHunger = isHunger;
    return this;
  }
}
