package github.moriyoshi.comminiplugin.game.battleroyale.items;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RecallClockItem extends CustomItem {
  public RecallClockItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<light_purple>リコールクロノ")
            .lore("<gray>時間を巻き戻すことができる砂時計。", "<gray>使用すると5秒前の地点に戻ることができる")
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
              nbt.getCompound(nbtKey).setString("locations", ComMiniPlugin.gson.toJson(loc));
              return;
            });
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 100);
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
      ComMiniPrefix.MAIN.send(player, "<red>５秒前の記憶はありません");
      return;
    }
    player.teleport(loc);
  }
}
