package github.moriyoshi.comminiplugin.system;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public interface AbstractGameListener<T extends AbstractGame> extends Listener {

  @SuppressWarnings("unchecked")
  default T getGame() {
    return (T) GameSystem.nowGame();
  }

  /**
   * プレイヤーがサーバーに入ったら呼び出されます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   * @return false で そのプレイヤーを初期化します(とくに処理がない場合にfalseを返してロビーにテレポなどをします)
   */
  default boolean join(PlayerJoinEvent e) {
    return false;
  }

  /**
   * プレイヤーがサーバーから抜けたら呼び出されます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void quit(PlayerQuitEvent e) {
  }

  /**
   * プレイヤーが死んだら呼び出されます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void death(PlayerDeathEvent e) {
  }

  /**
   * @param e event
   */
  default void damage(EntityDamageEvent e) {
  }

  /**
   * Entity 同士の攻撃です instanceof Player を忘れずに
   *
   * @param e event
   */
  default void damageByEntity(EntityDamageByEntityEvent e) {
  }

  /**
   * ブロックを壊したらこのイベントに来ます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void blockBreak(BlockBreakEvent e) {
  }

  /**
   * ブロックを置いたらこのイベントが呼ばれます (すべてのゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void blockPlace(BlockPlaceEvent e) {
  }
}
