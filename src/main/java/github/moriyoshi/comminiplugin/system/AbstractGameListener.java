package github.moriyoshi.comminiplugin.system;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public interface AbstractGameListener<T extends AbstractGame> extends Listener {

  @SuppressWarnings("unchecked")
  default T getGame() {
    return (T) GameSystem.nowGame();
  }

  /**
   * プレイヤーがサーバーに入ったら呼び出されます
   * (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void join(PlayerJoinEvent e) {
  }

  /**
   * プレイヤーがサーバーから抜けたら呼び出されます
   * (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void quit(PlayerQuitEvent e) {
  }

  /**
   * プレイヤーが死んだら呼び出されます
   * (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  @NotNull
  default void death(PlayerDeathEvent e) {
  }

  /**
   * @param e event
   */
  default void damage(EntityDamageEvent e) {
    return;
  }

  /**
   * Entity 同士の攻撃です instanceof Player を忘れずに
   *
   * @param e event
   * @return is cancel
   */
  default void damageByEntity(EntityDamageByEntityEvent e) {
  }

  /**
   * ブロックを壊したらこのイベントに来ます
   * (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   * @return is cancel
   */
  default void breakBlock(BlockBreakEvent e) {
  }
}
