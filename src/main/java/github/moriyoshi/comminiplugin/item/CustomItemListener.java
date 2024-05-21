package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

public class CustomItemListener implements Listener {

  private static final CustomItemListener INSTANCE = new CustomItemListener();

  private CustomItemListener() {
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

  public static CustomItemListener getInstance() {
    return INSTANCE;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dropItem(final PlayerDropItemEvent e) {
    final ItemStack item = e.getItemDrop().getItemStack();
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
  public void customItemInteract(final PlayerInteractEvent e) {
    final ItemStack item = e.getItem();
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    final CustomItem customItem = CustomItem.getCustomItem(item);
    e.setCancelled(true);
    customItem.interact(e);
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
    final List<CustomItem> list = new ArrayList<>();
    if (CustomItem.isCustomItem(item)) {
      list.add(CustomItem.getCustomItem(item));
    }
    if (CustomItem.isCustomItem(click)) {
      list.add(CustomItem.getCustomItem(click));
    }
    if (!list.isEmpty()) {
      if (list.stream().allMatch(i -> {
        if (i.canMoveOtherInv(e)) {
          return false;
        }
        val type = e.getView().getTopInventory().getType();
        return switch (type) {
          case CRAFTING, WORKBENCH -> false;
          default -> true;
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
    if (CustomItem.isCustomItem(item)) {
      CustomItem.getCustomItem(item).itemSpawn(e);
    }
  }
}
