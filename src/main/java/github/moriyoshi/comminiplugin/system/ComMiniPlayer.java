package github.moriyoshi.comminiplugin.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class ComMiniPlayer extends JsonAPI {

  private static Team hidenametag;

  private static final HashMap<UUID, ComMiniPlayer> players = new HashMap<>();

  public static void save() {
    for (final Entry<UUID, ComMiniPlayer> entry : players.entrySet()) {
      final ComMiniPlayer p = entry.getValue();
      p.saveFile();
    }
  }

  public static void gameInitialize() {
    final Scoreboard score = Bukkit.getScoreboardManager().getMainScoreboard();

    Team t = score.getTeam("hidenametag");
    if (t == null) {
      t = score.registerNewTeam("hidenametag");
    }
    t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    hidenametag = t;
  }

  public static ComMiniPlayer getPlayer(final UUID uuid) {
    var player = players.get(uuid);
    if (player != null) {
      return player;
    }
    player = new ComMiniPlayer(uuid);
    players.put(uuid, player);
    return player;
  }

  private final UUID uuid;

  @Getter @Setter private boolean isHunger;

  @Getter @Setter private boolean canFoodRegain;

  @Getter @Setter private boolean isDebug = false;

  @Getter @Setter private boolean shouldLoadResourcePack;

  @Getter @Setter private boolean isJoinGame;

  private ComMiniPlayer(final UUID uuid) {
    super(ComMiniPlugin.getPlugin(), "gameplayers", uuid.toString());
    this.uuid = uuid;
    this.initialize();
  }

  public void initialize() {
    this.isHunger = false;
    this.canFoodRegain = true;
    this.isJoinGame = false;
    hidenametag.removeEntry(Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName()));
  }

  public void setHideNameTag(final boolean isHideNameTag) {
    val p = Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName());
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

  private final Map<Class<? extends InterfaceGamePlayer>, InterfaceGamePlayer> gamePlayerDatas =
      new HashMap<>();

  private JsonObject datas;

  @SuppressWarnings("unchecked")
  public <T extends InterfaceGamePlayer> T getGamePlayerData(final Class<T> clazz) {
    T data = (T) gamePlayerDatas.get(clazz);
    if (data != null) {
      return data;
    }
    try {
      data = clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
    }
    val name = clazz.getSimpleName();
    data.generateLoadData(
        Optional.ofNullable(datas.getAsJsonObject(name)).orElseGet(JsonObject::new));
    gamePlayerDatas.put(clazz, data);
    return data;
  }

  @Override
  protected JsonElement generateSaveData() {
    val object = new JsonObject();
    val finalDatas = datas;
    gamePlayerDatas.forEach(
        (clazz, instance) -> finalDatas.add(clazz.getSimpleName(), instance.generateSaveData()));
    object.add("datas", finalDatas);
    object.addProperty("shouldLoadResourcePack", shouldLoadResourcePack);
    return object;
  }

  @Override
  protected void generateLoadData(final JsonElement dataElement) {
    val data = dataElement.getAsJsonObject();
    if (data.has("datas")) {
      datas = data.getAsJsonObject("datas");
    } else {
      datas = new JsonObject();
    }
    if (data.has("shouldLoadResourcePack")) {
      shouldLoadResourcePack = data.get("shouldLoadResourcePack").getAsBoolean();
    } else {
      shouldLoadResourcePack = true;
    }
  }
}
