package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public interface InterfaceGame {

  /** このゲームが始まっているか */
  boolean isStarted();

  /** このゲームのワールド */
  @Nullable
  World getWorld();

  /** このゲームのロビー */
  @Nullable
  Location getLobby();

  /**
   * ゲームのプレイヤーかどうか？
   *
   * @param player player
   * @return true でゲームのプレイヤー
   */
  boolean isGamePlayer(Player player);

  default boolean isGamePlayer(final Player player, final Class<? extends Event> event) {
    return isGamePlayer(player);
  }

  default void runPlayers(final Consumer<Player> consumer) {
    Bukkit.getOnlinePlayers()
        .forEach(
            p -> {
              if (isGamePlayer(p)) {
                consumer.accept(p);
              }
            });
  }

  default void teleportLobby(final Player player) {
    player.teleport(getLobby());
  }

  boolean startGame(Player player);

  void finishGame();

  PrefixUtil getPrefix();

  default void hidePlayer() {
    final List<UUID> list =
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> !isGamePlayer(p))
            .map(Entity::getUniqueId)
            .toList();
    final ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

  default void showPlayer() {
    val list =
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> !isGamePlayer(p))
            .map(p -> ((CraftPlayer) p).getHandle())
            .toList();
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }
}
