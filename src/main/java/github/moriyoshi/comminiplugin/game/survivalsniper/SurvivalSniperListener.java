package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.util.Util;

public class SurvivalSniperListener implements AbstractGameListener<SurvivalSniperGame> {

  @Override
  public void quit(PlayerQuitEvent e) {
    var game = getGame();
    game.players.remove(e.getPlayer().getUniqueId());
    var alives = game.players.entrySet().stream().filter(entry -> entry.getValue().getLeft())
        .toList();
    if (alives.size() != 1) {
      return;
    }
    final var s = alives.get(0).getKey();
    new BukkitRunnable() {
      @Override
      public void run() {
        game.endGame(s);
      }
    }.runTask(ComMiniPlugin.getPlugin());
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
    SurvivalSniperGame game = getGame();
    var p = e.getPlayer();
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
    var uuid = p.getUniqueId();
    if (game.players.get(uuid).getRight() == 0) {
      e.deathMessage(Util.mm(p.getName() + "は洞窟で酸素がなくなった..."));
    }
    game.runPlayers(pl -> {
      Util.send(pl, e.deathMessage());
    });
    p.setGameMode(GameMode.SPECTATOR);
    game.players.put(uuid, Pair.of(false, -1));
    var alives = game.players.entrySet().stream().filter(entry -> entry.getValue().getLeft())
        .toList();
    if (alives.size() != 1) {
      game.teleportLobby(p);
      return;
    }
    final var s = alives.get(0).getKey();
    new BukkitRunnable() {
      @Override
      public void run() {
        game.endGame(s);
      }
    }.runTask(ComMiniPlugin.getPlugin());
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
    if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.CRAFTING_TABLE && p.isSneaking()) {
      e.setCancelled(true);
      new SurvivalSniperCustomMenu().openInv(p);
    }
  }

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (!getGame().canPvP() && e.getEntity() instanceof Player && e.getDamager() instanceof Player attacker) {
      getGame().prefix.send(attacker, "<red>まだPvPはできません");
      e.setCancelled(true);
    }
  }
}
