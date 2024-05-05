package github.moriyoshi.comminiplugin.system;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.constant.MenuItem;
import github.moriyoshi.comminiplugin.item.CustomItem;

public class GameListener implements Listener {

  private static final GameListener INSTANCE = new GameListener();

  public static final GameListener getInstance() {
    return INSTANCE;
  }

  private GameListener() {
    new BukkitRunnable() {

      @Override
      public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> {
          if (GamePlayer.getPlayer(p.getUniqueId()).isHunger()) {
            return;
          }
          p.setFoodLevel(20);
        });
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  public static boolean isGamePlayer(Player p) {
    return GameSystem.isStarted() && GameSystem.nowGame().isGamePlayer(p);
  }

  @EventHandler
  public void join(PlayerJoinEvent e) {
    var p = e.getPlayer();
    p.teleportAsync(ComMiniWorld.LOBBY);
    var inv = p.getInventory();
    var flag = true;
    for (var i : inv) {
      if (CustomItem.equalsItem(i, MenuItem.class)) {
        flag = false;
        break;
      }
    }
    if (flag) {
      inv.addItem(new MenuItem().getItem());
    }
    if (isGamePlayer(p)) {
      GameSystem.nowGame().listener.join(e);
      return;
    }
    GamePlayer.getPlayer(p.getUniqueId()).initialize();
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.quit(e);
    }
  }

  @EventHandler
  public void death(PlayerDeathEvent e) {
    e.setCancelled(true);
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.death(e);
      return;
    }
    e.getPlayer().teleportAsync(ComMiniWorld.LOBBY);
  }

  @EventHandler
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player attacker
        && !isGamePlayer(attacker)
        && e.getCause().equals(DamageCause.FALL)) {
      e.setCancelled(true);
      return;
    }
    if (GameSystem.isStarted()) {
      GameSystem.nowGame().listener.damage(e);
    }
  }

  @EventHandler
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player attacker
        && !isGamePlayer(attacker)
        && attacker.getGameMode() != GameMode.CREATIVE) {
      e.setCancelled(true);
      return;
    }
    if (GameSystem.isStarted()) {
      GameSystem.nowGame().listener.damageByEntity(e);
      return;
    }
  }

  @EventHandler
  public void breakBlock(BlockBreakEvent e) {
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.breakBlock(e);
      return;
    }
    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
      e.setCancelled(true);
    }
  }
}
