package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.tuple.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RecallClockItem extends CustomItem implements CooldownItem {

  private static Map<UUID, Pair<Location, Double>> datas = new HashMap<>();

  public RecallClockItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<light_purple>リコールクロノ")
            .lore("<gray>時間を巻き戻すことができる砂時計。", "<gray>使用すると15秒前の地点に戻ることができる")
            .customModelData(21)
            .build());
  }

  public RecallClockItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void runTick(final Player player) {
    new BukkitRunnable() {

      private final Location loc = player.getLocation();
      private final double health = player.getHealth();
      private final UUID key = getUniqueId();

      @Override
      public void run() {
        val item = getInventoryCustomItem(player.getInventory(), key);
        if (!datas.containsKey(key)) {
          new ItemBuilder(item.getItem())
              .name("<light_purple>リコールクロノ<gray>:<red>turn back the clock");
          player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
        }
        datas.put(key, Pair.of(loc, health));
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 15 * 20);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(true);
      return;
    }
    if (inCooldown()) {
      return;
    }
    val player = e.getPlayer();
    val pair = datas.get(getUniqueId());
    if (pair == null) {
      ComMiniPrefix.MAIN.send(player, "<red>15秒前の記憶はありません");
      return;
    }
    val loc = pair.getFirst();
    setCooldown(120 * 20);
    val world = player.getWorld();
    world.spawnParticle(
        Particle.DUST,
        player.getLocation(),
        300,
        1,
        1,
        1,
        0,
        new Particle.DustOptions(Color.fromRGB(0x8C4CBB), 1),
        true);
    world.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, SoundCategory.MASTER, 1, 1);
    player.teleport(loc);
    player.setHealth(pair.getSecond());
    world.spawnParticle(
        Particle.DUST,
        loc,
        300,
        1,
        1,
        1,
        0,
        new Particle.DustOptions(Color.fromRGB(0x61DF80), 1),
        true);
    world.playSound(loc, Sound.BLOCK_CONDUIT_ACTIVATE, SoundCategory.MASTER, 1, 1);
  }
}
