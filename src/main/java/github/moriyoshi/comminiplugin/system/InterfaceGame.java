package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.util.NMSUtil;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

  @SuppressWarnings("unchecked")
  default List<Player> getPlayers() {
    return (List<Player>) Bukkit.getOnlinePlayers().stream().filter(p -> isGamePlayer(p)).toList();
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
}
