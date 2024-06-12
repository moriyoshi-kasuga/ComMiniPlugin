package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.util.HasKey;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

  @SuppressWarnings("unchecked")
  default List<Player> getNonGamePlayers() {
    return (List<Player>) Bukkit.getOnlinePlayers().stream().filter(p -> !isGamePlayer(p)).toList();
  }

  @SuppressWarnings("unchecked")
  default Stream<Player> getNonGamePlayersStream() {
    return (Stream<Player>) Bukkit.getOnlinePlayers().stream().filter(p -> !isGamePlayer(p));
  }

  /**
   * get game players and not game players
   *
   * @return true is game players, false is not game players
   */
  default Map<Boolean, List<Player>> getAllPlayers() {
    return Bukkit.getOnlinePlayers().stream()
        .collect(Collectors.partitioningBy(this::isGamePlayer));
  }

  default void teleportLobby(final Player player) {
    player.teleport(getLobby());
  }

  void finishGame();

  PrefixUtil getPrefix();

  default void setPlayerJoinGameIdentifier(final Player player) {
    ComMiniPlayer.getPlayer(player.getUniqueId()).setJoinGameIdentifier(getKey());
  }

  default void setPlayerJoinGameIdentifier(final UUID uuid) {
    ComMiniPlayer.getPlayer(uuid).setJoinGameIdentifier(getKey());
  }
}
