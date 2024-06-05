package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.Util;
import github.moriyoshi.comminiplugin.util.tuple.Triple;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("deprecation")
public class SSListener implements AbstractGameListener<SSGame> {

  @Override
  public void damage(EntityDamageEvent e, Player player) {
    if (e.getCause().equals(DamageCause.FALL)) {
      e.setCancelled(false);
    }
  }

  @Override
  public void quit(final PlayerQuitEvent e) {
    val p = e.getPlayer();
    val flag = getGame().players.remove(p.getUniqueId()).getFirst();
    if (!flag) {
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
    final SSGame game = getGame();
    val p = e.getPlayer();
    val uuid = p.getUniqueId();
    if (game.players.get(uuid).getSecond() == 0) {
      e.deathMessage(Util.mm(p.getName() + "は洞窟で酸素がなくなった..."));
    }
    p.setGameMode(GameMode.SPECTATOR);
    game.runPlayers(pl -> Util.send(pl, e.deathMessage()));
    game.players.put(uuid, Triple.of(false, -1, null));
    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false));
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
  public void damageByEntity(final EntityDamageByEntityEvent e) {
    if (e.getEntity() instanceof Player entity && e.getDamager() instanceof final Player attacker) {
      val game = getGame();
      if (!game.isCanPvP()) {
        game.prefix.send(attacker, "<red>まだPvPはできません");
        e.setCancelled(true);
        return;
      }
      ChatColor color1 =
          Optional.ofNullable(game.players.get(entity.getUniqueId()))
              .map(t -> t.getThird())
              .orElse(null);
      val color2 = game.players.get(attacker.getUniqueId()).getThird();
      if (color1 != null && color2 != null && color1 == color2) {
        e.setCancelled(true);
        return;
      }

      val main = attacker.getInventory().getItemInMainHand().getType();
      if (EnchantmentTarget.TOOL.includes(main)
          && (main.name().contains("STONE") || main.name().contains("WOODEN"))) {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void moevDimension(final PlayerPortalEvent e) {
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
    final SSGame game = getGame();
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
    if (getGame().getMode() == SSGame.Mode.FFA) {
      val alives =
          game.players.entrySet().stream().filter(entry -> entry.getValue().getFirst()).toList();
      if (alives.size() != 1) {
        if (alives.size() == 2) {
          game.speedUpBorder();
        }
        game.teleportLobby(p);
        return;
      }
      game.endGame(Bukkit.getPlayer(alives.getFirst().getKey()).getName());
    } else {
      val alives =
          game.players.entrySet().stream()
              .collect(Collectors.groupingBy(entry -> entry.getValue().getThird()));
      if (alives.isEmpty()) {
        getGame().prefix.cast("<red>エラーです。残りのプレイヤーが0人です");
        GameSystem.finalGame();
        return;
      }
      if (alives.size() != 1) {
        if (alives.size() == 2) {
          game.speedUpBorder();
        }
        game.teleportLobby(p);
        return;
      }
      alives.forEach(
          (color, list) -> {
            game.endGame(Util.colorToComponent(color, color.name() + " チーム"));
          });
    }
  }
}
