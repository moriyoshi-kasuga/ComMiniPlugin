package github.moriyoshi.comminiplugin.game.battleroyale.items;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RecallClockItem extends CustomItem implements CooldownItem {
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
    val loc = player.getLocation();
    new BukkitRunnable() {

      private final ItemStack item = getItem();

      @Override
      public void run() {
        if (item == null || item.isEmpty()) {
          return;
        }
        NBT.modify(
            item,
            nbt -> {
              val compound = nbt.getCompound(nbtKey);
              if (!compound.hasTag("locations")) {
                new ItemBuilder(item).name("<light_purple>リコールクロノ<gray>:<red>turn back the clock");
                player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
              }
              compound.setString("locations", ComMiniPlugin.gson.toJson(loc));
              return;
            });
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 300);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(true);
      return;
    }
    val loc =
        NBT.modify(
            getItem(),
            nbt -> {
              val compound = nbt.getCompound(nbtKey);
              if (!compound.hasTag("locations")) {
                return null;
              }
              return ComMiniPlugin.gson.fromJson(compound.getString("locations"), Location.class);
            });
    val player = e.getPlayer();
    if (loc == null) {
      ComMiniPrefix.MAIN.send(player, "<red>15秒前の記憶はありません");
      return;
    }
    setCooldown(2400);
    player.teleport(loc);
  }
}
