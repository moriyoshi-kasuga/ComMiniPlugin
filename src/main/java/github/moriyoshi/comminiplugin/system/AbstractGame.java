package github.moriyoshi.comminiplugin.system;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import lombok.Getter;
import lombok.val;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public abstract class AbstractGame {

  public final String id;
  public final String name;
  public final String description;
  public final Material material;
  public final PrefixUtil prefix;
  final AbstractGameListener<?> listener;

  public AbstractGame(String id, String name, String description, Material material, PrefixUtil prefix,
      AbstractGameListener<?> listener) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.material = material;
    this.prefix = prefix;
    this.listener = listener;
    this.fieldInitialize(true);
  }
  
  protected abstract void fieldInitialize(boolean isCreatingInstance);

  @Getter
  private boolean isStarted = false;

  @Getter
  protected World world;

  @Getter
  protected Location lobby;

  public abstract MenuHolder<ComMiniPlugin> createAdminMenu();

  public abstract MenuHolder<ComMiniPlugin> createGameMenu(Player player);

  public abstract boolean addSpec(Player player);

  public abstract boolean initializeGame(Player player);

  public final boolean startGame(final Player player) {
    if (!innerStartGame(player)) {
      return false;
    }
    isStarted = true;
    ComMiniPlugin.getPlugin().registerEvent(listener);
    return true;
  }

  public final void finishGame() {
    isStarted = false;
    HandlerList.unregisterAll(listener);
    runPlayers(GameSystem::initializePlayer);
    innerFinishGame();
    fieldInitialize(false);
  }

  protected abstract boolean innerStartGame(Player player);

  protected abstract void innerFinishGame();

  public abstract boolean isGamePlayer(Player player);

  public final void runPlayers(final Consumer<Player> consumer) {
    Bukkit.getOnlinePlayers().forEach(p -> {
      if (isGamePlayer(p)) {
        consumer.accept(p);
      }
    });
  }

  public final void teleportLobby(final Player player) {
    player.teleport(this.lobby);
  }

  public final void hidePlayer() {
    final List<UUID> list = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !isGamePlayer(p))
        .map(Entity::getUniqueId).toList();
    final ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

  public final void showPlayer() {
    val list = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !isGamePlayer(p))
        .map(p -> ((CraftPlayer) p).getHandle()).toList();
    final ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

}
