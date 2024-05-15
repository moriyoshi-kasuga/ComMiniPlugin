package github.moriyoshi.comminiplugin.system;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSSlot;
import lombok.Getter;
import lombok.Setter;

public class GamePlayer extends JsonAPI {

  private static Team hidenametag;

  private static final HashMap<UUID, GamePlayer> players = new HashMap<>();

  private static final Type survivapsniperSlotType = new TypeToken<ArrayList<Integer>>() {
  }.getType();

  public static void save() {
    for (Entry<UUID, GamePlayer> entry : players.entrySet()) {
      GamePlayer p = entry.getValue();
      p.saveFile();
    }
  }

  public static void gameInitialize() {
    Scoreboard score = Bukkit.getScoreboardManager().getMainScoreboard();

    Team t = score.getTeam("hidenametag");
    if (t == null) {
      t = score.registerNewTeam("hidenametag");
    }
    t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    hidenametag = t;
  }

  public static GamePlayer getPlayer(UUID uuid) {
    if (players.containsKey(uuid)) {
      return players.get(uuid);
    }
    var p = new GamePlayer(uuid);
    players.put(uuid, p);
    return p;
  }

  private final UUID uuid;

  @Getter
  @Setter
  private boolean isHunger;

  @Getter
  @Setter
  private boolean isDebug;

  private GamePlayer(UUID uuid) {
    super(ComMiniPlugin.getPlugin(), "gameplayers", uuid.toString());
    this.uuid = uuid;
    this.initialize();
  }

  public void initialize() {
    this.isHunger = false;
    hidenametag.removeEntry(Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName()));
  }

  public void setHideNameTag(boolean isHideNameTag) {
    var p = Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName());
    if (isHideNameTag) {
      hidenametag.addEntry(p);
    } else {
      hidenametag.removeEntry(p);
    }
  }

  public boolean isHideNameTag() {
    return hidenametag.hasEntry(
        Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName()));
  }

  @Getter
  private SSSlot survivapsniperSlot;

  private final String SURVIVAPSNIPER_SLOT = "survivapsniperSlot";

  @Override
  protected JsonObject generateSaveData() {
    var object = new JsonObject();
    object.add(SURVIVAPSNIPER_SLOT, ComMiniPlugin.gson.toJsonTree(survivapsniperSlot));
    return object;
  }

  @Override
  protected void generateLoadData(JsonObject data) {
    if (data.has(SURVIVAPSNIPER_SLOT)) {
      survivapsniperSlot = new SSSlot(
          ComMiniPlugin.gson.fromJson(data.get(SURVIVAPSNIPER_SLOT), survivapsniperSlotType));
    } else {
      survivapsniperSlot = new SSSlot(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
    }
  }
}
