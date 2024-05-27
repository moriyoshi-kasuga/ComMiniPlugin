package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.AbstractGame;
import github.moriyoshi.comminiplugin.system.gametype.StageTypeGame;
import github.moriyoshi.comminiplugin.system.gametype.WinnerTypeGame;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import lombok.val;

public class BRGame extends AbstractGame implements WinnerTypeGame {

  private final int MAX_RADIUS_RANGE = 600;
  private final int MIN_BORDER_RANGE = 50;
  private final int MAX_SECOND = 60 * 10;

  public final HashMap<UUID, Boolean> players = new HashMap<>();

  public BRGame() {
    super(
        "battleroyale",
        "<yellow>バトルロワイヤル",
        "<yellow>殺せ!殺せ!勝ち上がれ!",
        Material.GOLDEN_SWORD,
        new PrefixUtil("<gray>[<yellow>BattleRoyale<gray>]"),
        new BRListener());
    this.world = ComMiniWorld.GAME_WORLD;
    this.lobby = new Location(ComMiniWorld.GAME_WORLD, 1000.5, 0, 1000.5);
  }

  private StageTypeGame stageTypeGame;

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
    stageTypeGame = new StageTypeGame(world, lobby, MAX_RADIUS_RANGE, MIN_BORDER_RANGE, MAX_SECOND);
    stageTypeGame.stageInitialize();
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
    stageTypeGame.stageStart();
    return true;
  }

  @Override
  protected void innerFinishGame() {
    stageTypeGame.stageEnd();
  }

  @Override
  protected void fieldInitialize(final boolean isCreatingInstance) {
    this.stageTypeGame = null;
  }
}
