package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.util.HasKey;
import github.moriyoshi.comminiplugin.util.NMSUtil;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface InterfaceGame extends HasKey {

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

  default void runPlayers(final Consumer<Player> consumer) {
    Bukkit.getOnlinePlayers()
        .forEach(
            p -> {
              if (isGamePlayer(p)) {
                consumer.accept(p);
              }
            });
  }

  default void sendPlayers(final Object message) {
    getPlayers().forEach(p -> getPrefix().send(p, message));
  }

  @SuppressWarnings("unchecked")
  default List<Player> getPlayers() {
    return (List<Player>) Bukkit.getOnlinePlayers().stream().filter(this::isGamePlayer).toList();
  }

  @SuppressWarnings("unchecked")
  default Stream<Player> getPlayersStream() {
    return (Stream<Player>) Bukkit.getOnlinePlayers().stream().filter(this::isGamePlayer);
  }

  default void teleportLobby(final Player player) {
    player.teleport(getLobby());
  }

  void finishGame();

  PrefixUtil getPrefix();

  default void hidePlayer() {
    final List<UUID> list =
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> !isGamePlayer(p))
            .map(Entity::getUniqueId)
            .toList();
    NMSUtil.sendPlayerHidePackt(getPlayers(), list);
  }

  default void showPlayer() {
    val list =
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> !isGamePlayer(p))
            .map(NMSUtil::getServerPlayer)
            .toList();
    NMSUtil.sendNMSPlayerShowPackt(getPlayers(), list);
  }

  default void setPlayerJoinGameIdentifier(final Player player) {
    ComMiniPlayer.getPlayer(player.getUniqueId()).setJoinGameIdentifier(getKey());
  }


  default void setPlayerJoinGameIdentifier(final UUID uuid) {
    ComMiniPlayer.getPlayer(uuid).setJoinGameIdentifier(getKey());
  }
}
