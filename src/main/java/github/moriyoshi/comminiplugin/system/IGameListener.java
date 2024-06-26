package github.moriyoshi.comminiplugin.system;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public interface IGameListener extends Listener {

  /**
   * プレイヤーがサーバーから抜けたら呼び出されます (すべてゲームのプレイヤーだけです) (このメゾットはゲーム開始前でも呼ばれます,initialize時からです)
   *
   * @param e event
   */
  default void quit(final PlayerQuitEvent e) {}

  /**
   * プレイヤーが死んだら呼び出されます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void death(final PlayerDeathEvent e) {}

  default void deathSound(final PlayerDeathEvent event) {
    if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
      val victim = event.getPlayer();
      victim
          .getWorld()
          .playSound(
              victim.getLocation(),
              event.getDeathSound(),
              event.getDeathSoundCategory(),
              event.getDeathSoundVolume(),
              event.getDeathSoundPitch());
    }
  }

  /**
   * プレイヤーがダメージを受けた時です (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void damage(final EntityDamageEvent e, final Player player) {}

  /**
   * どちらもゲームプレイヤーです
   *
   * @param e event
   */
  default void damageByEntity(
      final EntityDamageByEntityEvent e, final Player attacker, final Player victim) {}

  /**
   * ブロックを壊したらこのイベントに来ます (すべてゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void blockBreak(final BlockBreakEvent e) {}

  /**
   * ブロックを置いたらこのイベントが呼ばれます (すべてのゲームのプレイヤーだけです)
   *
   * @param e event
   */
  default void blockPlace(final BlockPlaceEvent e) {}
}
