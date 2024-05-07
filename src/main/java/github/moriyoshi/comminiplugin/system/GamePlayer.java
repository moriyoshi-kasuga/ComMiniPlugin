package github.moriyoshi.comminiplugin.system;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.game.survivalsniper.SurvivalSniperSlot;
import lombok.Getter;
import lombok.Setter;

public class GamePlayer extends JsonAPI {

  private static Team hidenametag;

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
    new BukkitRunnable() {

      @Override
      public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> {
          var uuid = p.getUniqueId();
          var player = GamePlayer.getPlayer(uuid);
          int tick;
          if ((tick = player.getDisableMoveTick()) > -1) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 255, true, false));
            player.setDisableMoveTick(--tick);
          }
        });
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  private static final HashMap<UUID, GamePlayer> players = new HashMap<>();

  public static GamePlayer getPlayer(UUID uuid) {
    if (players.containsKey(uuid)) {
      return players.get(uuid);
    }
    var p = new GamePlayer(uuid);
    players.put(uuid, p);
    return p;
  }

  private final UUID uuid;

  private GamePlayer(UUID uuid) {
    super(ComMiniPlugin.getPlugin(), "gameplayers", uuid.toString());
    this.uuid = uuid;
    this.initialize();
  }

  public void initialize() {
    this.isHunger = false;
    hidenametag.removeEntry(Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid).getName()));
  }

  @Getter
  @Setter
  private boolean isHunger;

  @Getter
  @Setter
  private boolean isDebug;

  @Getter
  @Setter
  private int disableMoveTick = -1;

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
  private SurvivalSniperSlot survivapsniperSlot;
  private static final Type survivapsniperSlotType = new TypeToken<ArrayList<Integer>>() {
  }.getType();
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
      survivapsniperSlot = new SurvivalSniperSlot(
          ComMiniPlugin.gson.fromJson(data.get(SURVIVAPSNIPER_SLOT), survivapsniperSlotType));
    } else {
      survivapsniperSlot = new SurvivalSniperSlot(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
    }
  }
}
