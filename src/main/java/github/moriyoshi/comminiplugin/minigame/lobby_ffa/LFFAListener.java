package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.IGameListener;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LFFAListener implements IGameListener<LFFAMiniGame> {

  @Getter private final IdentifierKey key;

  public LFFAListener(IdentifierKey key) {
    this.key = key;
  }

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e, Player attacker, Player victim) {
    if (3 > e.getDamage()) {
      e.setCancelled(true);
    }
  }

  @Override
  public void death(PlayerDeathEvent e) {
    deathSound(e);
    val attacker = e.getDamageSource().getDirectEntity();
    if (attacker instanceof Player player) {
      val game = getGame();
      val kill = game.incrementKill(player);
      game.sendPlayers(
          "<aqua>"
              + e.getPlayer().getName()
              + " <gray>was killed by <aqua>"
              + player.getName()
              + "! <gold><b>"
              + kill
              + " KILL STREAK!");
      player.heal(10);
      game.prefix.send(player, "<green>half healed by kill streak!");
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.initializePlayer(e.getPlayer());
      }
    }.runTask(ComMiniPlugin.getPlugin());
  }
}
