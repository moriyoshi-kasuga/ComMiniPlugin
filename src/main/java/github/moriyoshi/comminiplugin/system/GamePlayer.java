package github.moriyoshi.comminiplugin.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * GamePlayer
 */
public class GamePlayer {
  private static Team hidenametag;

  public static void gameInitialize() {
    Scoreboard score = Bukkit.getScoreboardManager().getMainScoreboard();

    Team t = score.getTeam("hidenametag");
    if (t == null) {
      t = score.registerNewTeam("hidenametag");
    }
    t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    hidenametag = t;
  }

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
    hidenametag.removeEntry(Bukkit.getOfflinePlayer(this.uuid).getName());
  }

  private boolean isHunger;

  public boolean isHunger() {
    return isHunger;
  }

  public GamePlayer setHunger(boolean isHunger) {
    this.isHunger = isHunger;
    return this;
  }

  public GamePlayer setHideNameTag(boolean isHideNameTag) {
    var p = Bukkit.getOfflinePlayer(this.uuid).getName();
    if (isHideNameTag) {
      hidenametag.addEntry(p);
    } else {
      hidenametag.removeEntry(p);
    }
    return this;
  }
}
