package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.gametype.WinnerTypeGame;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import lombok.val;
import net.kyori.adventure.bossbar.BossBar;

public class BRGame extends AbstractGame implements WinnerTypeGame {

  public final HashMap<UUID, Boolean> players = new HashMap<>();

  private BossBar bossBar;

  private BRField field;

  public void setField(final BRField field) {
    this.field = field;
  }

  public BRGame() {
    super(
        "battleroyale",
        "<yellow>バトルロワイヤル",
        "<yellow>殺せ!殺せ!勝ち上がれ!",
        Material.GOLDEN_SWORD,
        new PrefixUtil("<gray>[<yellow>BattleRoyale<gray>]"),
        new BRListener());
    this.world = ComMiniWorld.GAME_WORLD;
  }

  public final void joinPlayer(final Player player, final boolean isPlayer) {
    val uuid = player.getUniqueId();
    if (players.containsKey(uuid)) {
      if (players.get(uuid) == isPlayer) {
        players.remove(uuid);
        prefix.cast(player.getName() + "が<white>" + (isPlayer ? "参加" : "観戦") + "を取りやめ");
        return;
      }
    }
    players.put(uuid, isPlayer);
    prefix.cast(player.getName() + "が" + (isPlayer ? "<blue>参加" : "<gray>観戦") + "します");
  }

  @Override
  public MenuHolder<ComMiniPlugin> createAdminMenu() {
    return new BRAdminMenu();
  }

  @Override
  public MenuHolder<ComMiniPlugin> createGameMenu(final Player player) {
    return new BRMenu();
  }

  @Override
  public boolean addSpec(final Player player) {
    val uuid = player.getUniqueId();
    players.put(uuid, false);
    player.setGameMode(GameMode.SPECTATOR);
    player.getInventory().clear();
    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
    teleportLobby(player);
    return true;
  }

  @Override
  public boolean initializeGame(final Player player) {
    return true;
  }

  @Override
  public boolean isGamePlayer(final Player player) {
    return players.containsKey(player.getUniqueId());
  }

  @Override
  protected boolean innerStartGame(final Player player) {
    if (2 > players.values().stream().filter(v -> v).toList().size()) {
      prefix.send(player, "<red>二人以上でしかプレイできません");
      return false;
    }
    if (field == null) {
      prefix.send(player, "<red>フィールドを選択してください");
      return false;
    }
    field.start();

    runPlayers(p -> {
      val uuid = p.getUniqueId();
      val inv = p.getInventory();
      inv.clear();
      if (players.get(uuid)) {
        val gamePlayer = ComMiniPlayer.getPlayer(uuid);
        gamePlayer.setHideNameTag(true);
        gamePlayer.getGamePlayerData(BRPlayer.class).getHotbar().setItems(inv);
        p.setSaturation(6);
        p.setGameMode(GameMode.SURVIVAL);
      } else {
        p.setGameMode(GameMode.SPECTATOR);
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
      }
      teleportLobby(p);
    });

    bossBar = BossBar.bossBar(Util.mm("<red>投下まで<u>10</u>秒"), 1f,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_10);

    new BukkitRunnable() {

      private int time = 11;

      @Override
      public void run() {
        if (--time == 0) {
          runPlayers(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1);
            p.hideBossBar(bossBar);
          });
          // TODO: ここに投下するさいの地面を開ける処理を書く
          this.cancel();
          return;
        }
        bossBar.name(Util.mm("<red>投下まで<u>" + time + "</u>秒")).progress((float) time / (float) 10);
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);

    hidePlayer();
    return true;
  }

  @Override
  protected void innerFinishGame() {
    field.stop();
    players.clear();
    if (bossBar != null) {
      runPlayers(p -> p.hideBossBar(bossBar));
    }
    showPlayer();
  }

  @Override
  protected void fieldInitialize(final boolean isCreatingInstance) {
    this.field = null;
    this.bossBar = null;
  }

  @Override
  public void teleportLobby(Player player) {
    player.teleport(field.getLobby());
  }
}
