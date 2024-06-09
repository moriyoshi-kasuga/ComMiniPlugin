package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.minigame.AbstractMiniGameListener;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.IdentifierKey;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LFFAListener implements AbstractMiniGameListener<LFFAMiniGame> {

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
    val attacker = e.getDamageSource().getDirectEntity();
    if (attacker instanceof Player player) {
      val kill = getMiniGame().incrementKill(player);
      getMiniGame()
          .sendPlayers(
              "<aqua>"
                  + e.getPlayer().getName()
                  + " <gray>was killed by <aqua>"
                  + player.getName()
                  + "! <gold><b>"
                  + kill
                  + " KILL STREAK!");
      player.heal(2);
      getMiniGame().prefix.send(player, "<green>+2HP (1 heart)");
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        BukkitUtil.initializePlayer(e.getPlayer());
      }
    }.runTask(ComMiniPlugin.getPlugin());
  }
}