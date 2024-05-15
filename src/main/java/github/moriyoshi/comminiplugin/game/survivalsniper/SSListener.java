package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.util.Util;

public class SSListener implements AbstractGameListener<SSGame> {

  @Override
  public void quit(PlayerQuitEvent e) {
    var p = e.getPlayer();
    var flag = getGame().players.remove(p.getUniqueId()).getLeft();
    if (!flag) {
      return;
    }
    reducePlayer(p);
  }

  @EventHandler
  public void tp(PlayerTeleportEvent e) {
    var p = e.getPlayer();
    if (e.getCause().equals(TeleportCause.SPECTATE)
        && !getGame().getLobby().getWorld().getWorldBorder().isInside(e.getTo())) {
      getGame().prefix.send(p, "<red>範囲外にスペクテイターのテレポートは使えません");
      e.setCancelled(true);
    }
  }

  @Override
  public void death(PlayerDeathEvent e) {
    SSGame game = getGame();
    var p = e.getPlayer();
    var uuid = p.getUniqueId();
    if (game.players.get(uuid).getRight() == 0) {
      e.deathMessage(Util.mm(p.getName() + "は洞窟で酸素がなくなった..."));
    }
    p.setGameMode(GameMode.SPECTATOR);
    game.runPlayers(pl -> Util.send(pl, e.deathMessage()));
    game.players.put(uuid, Pair.of(false, -1));
    reducePlayer(p);
  }

  @EventHandler
  public void interact(PlayerInteractEvent e) {
    var p = e.getPlayer();
    if (!getGame().isGamePlayer(p)) {
      return;
    }
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    if (e.getClickedBlock() != null && p.isSneaking()) {
      e.setCancelled(true);
      switch (e.getClickedBlock().getType()) {
        case CRAFTING_TABLE:
          new SSCustomMenu().openInv(p);
          break;
        case FURNACE:
          new SSTradeMenu().openInv(p);
          break;
        default:
          e.setCancelled(false);
          break;

      }
    }
  }

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (!getGame().isCanPvP() && e.getEntity() instanceof Player
        && e.getDamager() instanceof Player attacker) {
      getGame().prefix.send(attacker, "<red>まだPvPはできません");
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void moevDimension(PlayerPortalEvent e) {
    if (getGame().isGamePlayer(e.getPlayer())) {
      getGame().prefix.send(e.getPlayer(), "<red>ポータルを使うな!");
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void entitySpawn(EntitySpawnEvent e) {
    if (e.getEntityType() == EntityType.ENDERMAN) {
      Entity entity = e.getEntity();
      if (getGame().getWorld().getWorldBorder().isInside(entity.getLocation())) {
        entity.remove();
      }
    }
  }

  private void reducePlayer(Player p) {
    SSGame game = getGame();
    var loc = p.getLocation();
    var world = p.getWorld();
    var inv = p.getInventory();
    inv.forEach(i -> {
      if (i == null || i.isEmpty()) {
        return;
      }
      world.dropItemNaturally(loc, i);
    });
    inv.clear();
    var alives = game.players.entrySet().stream().filter(entry -> entry.getValue().getLeft())
        .toList();
    if (alives.size() != 1) {
      if (alives.size() == 2) {
        game.setBorder();
      }
      game.teleportLobby(p);
      return;
    }
    game.endGame(alives.get(0).getKey());
  }

}
