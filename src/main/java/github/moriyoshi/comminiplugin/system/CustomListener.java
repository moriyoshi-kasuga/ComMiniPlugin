package github.moriyoshi.comminiplugin.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import lombok.val;

public class CustomListener implements Listener {

  private static final CustomListener INSTANCE = new CustomListener();

  private CustomListener() {
    new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        val flag = tick <= 0;
        Bukkit.getOnlinePlayers().forEach(p -> p.getInventory().forEach(i -> {
          if (CustomItem.isCustomItem(i)) {
            val custom = CustomItem.getCustomItem(i);
            custom.runTick(p);
            if (flag) {
              custom.runSecond(p);
            }
          }
        }));
        if (flag) {
          tick = 20;
          return;
        }
        tick--;
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  public static CustomListener getInstance() {
    return INSTANCE;
  }

  public static Optional<Boolean> getCustomItemFlag(ItemStack item, CustomItemFlag flag) {
    return getCustomItemFlag(item, flag.id);
  }

  public static Optional<Boolean> getCustomItemFlag(ItemStack item, String flag) {
    if (item == null || item.isEmpty()) {
      return Optional.empty();
    }
    return NBT.get(item, nbt -> {
      if (!nbt.hasTag("customitemflag")) {
        return Optional.empty();
      }
      return Optional.of(nbt.getCompound("customitemflag").getBoolean(flag));
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dropItem(final PlayerDropItemEvent e) {
    final ItemStack item = e.getItemDrop().getItemStack();
    if (!getCustomItemFlag(item, CustomItemFlag.DROP).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    final CustomItem customItem = CustomItem.getCustomItem(item);
    customItem.dropItem(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void shiftItem(final PlayerToggleSneakEvent e) {
    final Player player = e.getPlayer();
    final ItemStack item = player.getInventory().getItemInMainHand();
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    final CustomItem customItem = CustomItem.getCustomItem(item);
    customItem.shiftItem(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void interact(final PlayerInteractEvent e) {
    final ItemStack item = e.getItem();
    if (!getCustomItemFlag(item, CustomItemFlag.CLICK_INTERACT).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!CustomItem.isCustomItem(item)) {
      customBlockInteract(e);
      return;
    }
    e.setCancelled(true);
    CustomItem.getCustomItem(item).interact(e);
    if (e.useInteractedBlock() != Result.DENY) {
      customBlockInteract(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void blockBreak(final BlockBreakEvent e) {
    val item = e.getPlayer().getInventory().getItemInMainHand();
    if (CustomItem.isCustomItem(item)) {
      CustomItem.getCustomItem(item).blockBreak(e);
      if (e.isCancelled()) {
        return;
      }
    }
    val block = e.getBlock();
    if (!CustomBlock.isCustomBlock(block)) {
      return;
    }
    e.setCancelled(true);
    e.setDropItems(false);
    CustomBlock.getCustomBlock(block).blockBreak(e);
  }

  public boolean customBlockInteract(PlayerInteractEvent e) {
    if (!e.hasBlock()) {
      return false;
    }
    val block = e.getClickedBlock();
    if (!CustomBlock.isCustomBlock(block)) {
      return false;
    }
    e.setCancelled(true);
    CustomBlock.getCustomBlock(block).interact(e);
    return true;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void swapItem(final PlayerItemHeldEvent e) {
    final Player player = e.getPlayer();
    final PlayerInventory inventory = player.getInventory();
    final ItemStack previousItem = inventory.getItem(e.getPreviousSlot());
    final ItemStack newItem = inventory.getItem(e.getNewSlot());
    if (CustomItem.isCustomItem(previousItem)) {
      CustomItem.getCustomItem(previousItem).heldOfOther(e);
    }
    if (CustomItem.isCustomItem(newItem)) {
      CustomItem.getCustomItem(newItem).heldOfThis(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void offhandItem(final PlayerSwapHandItemsEvent e) {
    val main = e.getMainHandItem();
    val off = e.getOffHandItem();
    if (CustomItem.isCustomItem(off)) {
      CustomItem.getCustomItem(off).swapToOffHand(e);
      if (e.isCancelled()) {
        return;
      }
    }
    if (CustomItem.isCustomItem(main)) {
      CustomItem.getCustomItem(main).swapToMainHand(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void inventoryClick(final InventoryClickEvent e) {
    val item = e.getCursor();
    val click = e.getCurrentItem();
    if (!getCustomItemFlag(item, CustomItemFlag.MOVE_INV).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!getCustomItemFlag(click, CustomItemFlag.MOVE_INV).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    final List<CustomItem> list = new ArrayList<>();
    if (CustomItem.isCustomItem(item)) {
      list.add(CustomItem.getCustomItem(item));
    }
    if (CustomItem.isCustomItem(click)) {
      list.add(CustomItem.getCustomItem(click));
    }
    if (!list.isEmpty()) {
      if (list.stream().anyMatch(i -> {
        val type = e.getView().getTopInventory().getType();
        return switch (type) {
          case CRAFTING, WORKBENCH -> false;
          default -> !i.canMoveOtherInv(e);
        };
      })) {
        e.setCancelled(true);
        return;
      }
    }
    for (final CustomItem customItem : list) {
      customItem.clickItem(e);
      if (e.isCancelled()) {
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void itemSpawn(final ItemSpawnEvent e) {
    val item = e.getEntity().getItemStack();
    if (!getCustomItemFlag(item, CustomItemFlag.ITEM_SPAWN).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (CustomItem.isCustomItem(item)) {
      CustomItem.getCustomItem(item).itemSpawn(e);
    }
  }
}
