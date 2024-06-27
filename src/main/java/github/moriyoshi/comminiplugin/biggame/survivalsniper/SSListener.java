package github.moriyoshi.comminiplugin.biggame.survivalsniper;

import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import github.moriyoshi.comminiplugin.system.biggame.AbstractBigGameListener;

import java.util.Optional;
import java.util.stream.Collectors;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@SuppressWarnings("deprecation")
public class SSListener implements AbstractBigGameListener<SSBigBigGame> {

  @Override
  public void damage(EntityDamageEvent e, Player player) {
    if (e.getCause().equals(DamageCause.FALL)) {
      e.setCancelled(false);
      e.setDamage(e.getFinalDamage() / 2);
    }
  }

  @Override
  public void quit(final PlayerQuitEvent e) {
    val p = e.getPlayer();
    val flag = getGame().players.remove(p.getUniqueId()).getFirst() == -1;
    if (flag) {
      return;
    }
    if (getGame().isStarted()) {
      reducePlayer(p);
    }
  }

  @EventHandler
  public void tp(final PlayerTeleportEvent e) {
    val game = getGame();
    val p = e.getPlayer();
    if (!game.isGamePlayer(p)) {
      return;
    }
    if (e.getCause().equals(TeleportCause.SPECTATE)
        && !game.getLobby().getWorld().getWorldBorder().isInside(e.getTo())) {
      game.prefix.send(p, "<red>範囲外にスペクテイターのテレポートは使えません");
      e.setCancelled(true);
    }
  }

  @Override
  public void death(final PlayerDeathEvent e) {
    deathSound(e);
    final SSBigBigGame game = getGame();
    val p = e.getPlayer();
    val uuid = p.getUniqueId();
    if (game.players.get(uuid).getFirst() == 0) {
      e.deathMessage(BukkitUtil.mm(p.getName() + "は洞窟で酸素がなくなった..."));
    }
    game.runPlayers(pl -> BukkitUtil.send(pl, e.deathMessage()));
    game.setSpec(p);
    reducePlayer(p);
  }

  @EventHandler
  public void interact(final PlayerInteractEvent e) {
    val p = e.getPlayer();
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
  public void damageByEntity(EntityDamageByEntityEvent e, Player attacker, Player victim) {
    val game = getGame();
    if (!game.isCanPvP()) {
      getGame().prefix.send(attacker, "<red>まだPvPはできません");
      e.setCancelled(true);
      return;
    }
    ChatColor color1 =
        Optional.ofNullable(game.players.get(victim.getUniqueId()))
            .map(Pair::getSecond)
            .orElse(null);
    val color2 = game.players.get(attacker.getUniqueId()).getSecond();
    if (color2 != null && color1 == color2) {
      e.setCancelled(true);
      return;
    }

    val main = attacker.getInventory().getItemInMainHand().getType();
    if (EnchantmentTarget.TOOL.includes(main)
        && (main.name().contains("STONE") || main.name().contains("WOODEN"))) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void moveDimension(final PlayerPortalEvent e) {
    if (getGame().isGamePlayer(e.getPlayer())) {
      getGame().prefix.send(e.getPlayer(), "<red>ポータルを使うな!");
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void entitySpawn(final EntitySpawnEvent e) {
    if (e.getEntityType() == EntityType.ENDERMAN) {
      final Entity entity = e.getEntity();
      if (getGame().getWorld().getWorldBorder().isInside(entity.getLocation())) {
        entity.remove();
      }
    }
  }

  private void reducePlayer(final Player p) {
    final SSBigBigGame game = getGame();
    val loc = p.getLocation();
    val world = p.getWorld();
    val inv = p.getInventory();
    inv.forEach(
        i -> {
          if (i == null || i.isEmpty()) {
            return;
          }
          world.dropItemNaturally(loc, i);
        });
    inv.clear();
    if (getGame().getMode() == SSBigBigGame.Mode.FFA) {
      val alives =
          game.players.entrySet().stream()
              .filter(entry -> entry.getValue().getFirst() != -1)
              .toList();
      if (alives.size() != 1) {
        if (alives.size() == 2) {
          game.speedUpBorder();
        }
        p.teleport(game.getLobby());
        return;
      }
      game.endGame(Bukkit.getPlayer(alives.getFirst().getKey()).getName());
    } else {
      val alives =
          game.players.entrySet().stream()
              .filter(entry -> entry.getValue().getSecond() != null)
              .collect(Collectors.groupingBy(entry -> entry.getValue().getSecond()));
      if (alives.isEmpty()) {
        getGame().prefix.broadCast("<red>エラーです。残りのプレイヤーが0人です");
        BigGameSystem.finalGame();
        return;
      }
      if (alives.size() != 1) {
        if (alives.size() == 2) {
          game.speedUpBorder();
        }
        p.teleport(game.getLobby());
        return;
      }
      alives.forEach(
          (color, list) ->
              game.endGame("<#" + BukkitUtil.chatColorToHex(color) + ">" + color.name() + " チーム"));
    }
  }
}
