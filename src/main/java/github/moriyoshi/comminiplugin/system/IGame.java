package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.HasKey;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Nullable;

public interface IGame extends HasKey {

  public static class GameInitializeFailedException extends Exception {
    public GameInitializeFailedException(String message) {
      super(message);
    }
  }

  @FunctionalInterface
  public static interface GameInitializeFailedSupplier<T> {
    T get() throws GameInitializeFailedException;
  }

  void predicateInitialize() throws GameInitializeFailedException;

  String getId();

  String getName();

  String getDescription();

  /** スタートしてるかどうか? ({@code #startGame()} を呼び出したあと) */
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

  boolean startGame(Audience audience);

  void finishGame();

  PrefixUtil getPrefix();

  default void leavePlayer(Player player) {}

  boolean addSpec(Player player);

  /**
   * 全てのゲームプレイヤーに対して {@link Consumer} を適用します
   *
   * @param consumer consumer
   */
  default void runPlayers(final Consumer<Player> consumer) {
    getPlayers().forEach(consumer);
  }

  default void sendPlayers(final Object message) {
    getPlayers().forEach(p -> getPrefix().send(p, message));
  }

  @SuppressWarnings("unchecked")
  default List<Player> getPlayers() {
    return (List<Player>) Bukkit.getOnlinePlayers().stream().filter(this::isGamePlayer).toList();
  }

  @SuppressWarnings("unchecked")
  default List<Player> getNonGamePlayers() {
    return (List<Player>) Bukkit.getOnlinePlayers().stream().filter(p -> !isGamePlayer(p)).toList();
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

  default void hidePlayers() {
    val hiders = getNonGamePlayers();
    getPlayers()
        .forEach(
            player -> {
              hiders.forEach(
                  hider -> {
                    player.hidePlayer(ComMiniPlugin.getPlugin(), hider);
                    hider.hidePlayer(ComMiniPlugin.getPlugin(), player);
                  });
            });
  }

  /**
   * 個別にゲームプレイヤーではない人を隠します
   *
   * @param player target
   */
  default void hidePlayer(final Player player) {
    getNonGamePlayers()
        .forEach(
            hider -> {
              player.hidePlayer(ComMiniPlugin.getPlugin(), hider);
              hider.hidePlayer(ComMiniPlugin.getPlugin(), player);
            });
  }

  default void showPlayers() {
    val showers = getNonGamePlayers();
    getPlayers()
        .forEach(
            player -> {
              showers.forEach(
                  shower -> {
                    player.showPlayer(ComMiniPlugin.getPlugin(), shower);
                    shower.showPlayer(ComMiniPlugin.getPlugin(), player);
                  });
            });
  }

  /**
   * 個別にゲームプレイヤーではない人を表示します
   *
   * @param player target
   */
  default void showPlayer(final Player player) {
    getNonGamePlayers()
        .forEach(
            shower -> {
              player.showPlayer(ComMiniPlugin.getPlugin(), shower);
              shower.showPlayer(ComMiniPlugin.getPlugin(), player);
            });
  }

  @OverrideOnly
  boolean predicateStartGame(Audience audience);

  @OverrideOnly
  boolean predicateSpec(Player player);

  /**
   * ゲームが始まってからの観戦
   *
   * @param player 観戦させたい人
   * @return true で参加させ、false で観戦不可能
   */
  @OverrideOnly
  void innerAddSpec(Player player);

  @OverrideOnly
  IdentifierKey createKey();
}
