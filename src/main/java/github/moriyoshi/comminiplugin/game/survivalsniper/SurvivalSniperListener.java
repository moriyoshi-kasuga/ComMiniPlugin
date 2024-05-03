package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.util.Util;

public class SurvivalSniperListener implements AbstractGameListener<SurvivalSniperGame> {

  @EventHandler
  public void tp(PlayerTeleportEvent e) {
    var p = e.getPlayer();
    if (e.getCause().equals(TeleportCause.SPECTATE)) {
      getGame().prefix.send(p, "<red>スペクテイターのテレポートは使えません");
      e.setCancelled(true);
    }
  }

  @Override
  public void death(PlayerDeathEvent e) {
    SurvivalSniperGame game = getGame();
    var p = e.getPlayer();
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
}
