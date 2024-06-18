package github.moriyoshi.comminiplugin.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.JsonAPI;
import github.moriyoshi.comminiplugin.system.minigame.MiniGameSystem;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class ComMiniPlayer extends JsonAPI {

  private static final HashMap<UUID, ComMiniPlayer> players = new HashMap<>();
  private static Scoreboard scoreboard;
  private static Team hidenametag;
  private final UUID uuid;
  private final Map<Class<? extends InterfaceGamePlayer>, InterfaceGamePlayer> gamePlayerDatas =
      new HashMap<>();

  @Accessors(chain = true)
  @Getter
  @Setter
  private boolean isHunger;

  @Accessors(chain = true)
  @Getter
  @Setter
  private boolean canFoodRegain;

  @Accessors(chain = true)
  @Getter
  @Setter
  private boolean isDebug = false;

  @Accessors(chain = true)
  @Getter
  @Setter
  private boolean shouldLoadResourcePack;

  @Accessors(chain = true)
  @Getter
  @Setter
  @Nullable
  private AbstractGameKey joinGameIdentifier;

  private JsonObject datas;

  private ComMiniPlayer(final UUID uuid) {
    super(ComMiniPlugin.getPlugin(), "gameplayers", uuid.toString());
    this.uuid = uuid;
    this.initialize();
  }

  public static void save() {
    for (final Entry<UUID, ComMiniPlayer> entry : players.entrySet()) {
      final ComMiniPlayer p = entry.getValue();
      p.saveFile();
    }
  }

  public static void gameInitialize() {
    scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    hidenametag = scoreboard.registerNewTeam("commini_hidenametag");
    hidenametag.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    hidenametag.setCanSeeFriendlyInvisibles(false);
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

  public void initialize() {
    this.isHunger = false;
    this.canFoodRegain = true;
    if (joinGameIdentifier != null && joinGameIdentifier.isMiniGameKey()) {
      MiniGameSystem.getMiniGame(joinGameIdentifier).leavePlayer(Bukkit.getPlayer(uuid));
    }
    this.joinGameIdentifier = null;
    val player = Bukkit.getPlayer(uuid);
    if (player == null) {
      return;
    }
    hidenametag.removeEntry(player.getName());
    BukkitUtil.removeFalling(uuid);
  }

  public boolean isHideNameTag() {
    val player = Bukkit.getPlayer(uuid);
    if (player == null) {
      return false;
    }
    return hidenametag.hasEntry(player.getName());
  }

  public ComMiniPlayer setHideNameTag(final boolean isHideNameTag) {
    val player = Bukkit.getPlayer(uuid);
    if (player == null) {
      return this;
    }
    player.setScoreboard(scoreboard);
    val name = player.getName();
    if (isHideNameTag) {
      hidenametag.addEntry(name);
    } else {
      hidenametag.removeEntry(name);
    }
    return this;
  }

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
