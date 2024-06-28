package github.moriyoshi.comminiplugin.biggame.battleroyale;

import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.system.IGameListener;
import lombok.Getter;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BRListener implements IGameListener<BRBigGame> {

  @Getter private final IdentifierKey key;

  public BRListener(final IdentifierKey key) {
    this.key = key;
  }

  @Override
  public void blockBreak(BlockBreakEvent e) {
    e.setCancelled(true);
  }

  @Override
  public void blockPlace(BlockPlaceEvent e) {
    e.setCancelled(true);
  }

  @Override
  public void quit(final PlayerQuitEvent e) {
    val p = e.getPlayer();
    val flag = getGame().players.remove(p.getUniqueId());
    if (!flag) {
      return;
    }
    if (getGame().isStarted()) {
      getGame().leavePlayer(p);
    }
  }

  @EventHandler
  public void tp(final PlayerTeleportEvent event) {
    val player = event.getPlayer();
    if (!getGame().isGamePlayer(player)) {
      return;
    }
    if (event.getCause() == TeleportCause.ENDER_PEARL) {
      event.setCancelled(true);
      player.teleport(event.getTo());
      return;
    }
    if (event.getCause().equals(TeleportCause.SPECTATE)
        && !getGame().getLobby().getWorld().getWorldBorder().isInside(event.getTo())) {
      getGame().prefix.send(player, "<red>範囲外にスペクテイターのテレポートは使えません");
      event.setCancelled(true);
    }
  }

  @Override
  public void death(PlayerDeathEvent e) {
    deathSound(e);
    val p = e.getPlayer();
    p.setGameMode(GameMode.SPECTATOR);
    val game = getGame();
    game.runPlayers(pl -> BukkitUtil.send(pl, e.deathMessage()));
    game.players.put(p.getUniqueId(), false);
    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
    game.leavePlayer(p);
  }

  @Override
  public void damage(EntityDamageEvent e, Player player) {
    if (e.getCause().equals(DamageCause.FLY_INTO_WALL)) {
      e.setCancelled(true);
      return;
    }
  }

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e, Player attacker, Player victim) {
    if (!getGame().isCanPvP()) {
      getGame().prefix.send(attacker, "<red>まだPvPはできません");
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void interact(PlayerInteractEvent e) {
    val player = e.getPlayer();
    if (getGame().isGamePlayer(player) && e.hasBlock()) {
      val block = e.getClickedBlock();
      if (!(block.getState() instanceof Container)) {
        return;
      }
      val field = getGame().getField();
      if (field == null) {
        return;
      }
      if (!field.getTreasure().containsLocation(block.getLocation())) {
        e.setCancelled(true);
        getGame().prefix.send(player, "<red>宝箱以外のチェストは開けません");
      }
    }
  }
}
