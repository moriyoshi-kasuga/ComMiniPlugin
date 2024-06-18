package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.lib.PrefixUtil;
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

public interface InterfaceGame extends HasGameKey {

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

  void finishGame();

  PrefixUtil getPrefix();

  void leavePlayer(Player player);

  /**
   * 全てのゲームプレイヤーに対して {@link Consumer} を適用します
   *
   * @param consumer consumer
   */
  default void runPlayers(final Consumer<Player> consumer) {
    getPlayers().forEach(consumer::accept);
  }

  default void sendPlayers(final Object message) {
    getPlayers().forEach(p -> getPrefix().send(p, message));
  }

  @SuppressWarnings("unchecked")
  default Stream<Player> getPlayers() {
    return (Stream<Player>) Bukkit.getOnlinePlayers().stream().filter(this::isGamePlayer);
  }

  @SuppressWarnings("unchecked")
  default Stream<Player> getNonGamePlayers() {
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

  default void setPlayerJoinGameIdentifier(final Player player) {
    ComMiniPlayer.getPlayer(player.getUniqueId()).setJoinGameIdentifier(getKey());
  }

  default void setPlayerJoinGameIdentifier(final UUID uuid) {
    ComMiniPlayer.getPlayer(uuid).setJoinGameIdentifier(getKey());
  }
}
