package github.moriyoshi.comminiplugin.item;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;

public class CustomItemListner implements Listener {

  private static final CustomItemListner INSTANCE = new CustomItemListner();

  public static CustomItemListner getInstance() {
    return INSTANCE;
  }

  private CustomItemListner() {
    new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        var flag = tick <= 0;
        Bukkit.getOnlinePlayers().forEach(p -> {
          p.getInventory().forEach(i -> {
            if (CustomItem.isCustomItem(i)) {
              var custom = CustomItem.getCustomItem(i);
              custom.runTick(p);
              if (flag) {
                custom.runSecond(p);
              }
            }
          });
        });
        if (flag) {
          tick = 20;
          return;
        }
        tick--;
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void interact(PlayerInteractEvent e) {
    EquipmentSlot hand = e.getHand();
    if (hand != null && hand.equals(EquipmentSlot.HAND)) {
      customItemInteract(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dropItem(PlayerDropItemEvent e) {
    ItemStack item = e.getItemDrop().getItemStack();
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    CustomItem customItem = CustomItem.getCustomItem(item);
    customItem.dropItem(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void shiftItem(PlayerToggleSneakEvent e) {
    Player player = e.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    CustomItem customItem = CustomItem.getCustomItem(item);
    customItem.shiftItem(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public boolean customItemInteract(PlayerInteractEvent e) {
    ItemStack item = e.getItem();
    if (!CustomItem.isCustomItem(item)) {
      return false;
    }
    CustomItem customItem = CustomItem.getCustomItem(item);
    e.setCancelled(true);
    customItem.interact(e);
    return true;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void swapItem(PlayerItemHeldEvent e) {
    Player player = e.getPlayer();
    PlayerInventory inventory = player.getInventory();
    ItemStack previousItem = inventory.getItem(e.getPreviousSlot());
    ItemStack newItem = inventory.getItem(e.getNewSlot());
    if (CustomItem.isCustomItem(previousItem)) {
      CustomItem.getCustomItem(previousItem).heldOfOther(e);
    }
    if (CustomItem.isCustomItem(newItem)) {
      CustomItem.getCustomItem(newItem).heldOfThis(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void offhandItem(PlayerSwapHandItemsEvent e) {
    var main = e.getMainHandItem();
    var off = e.getOffHandItem();
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
  public void inventoryClick(InventoryClickEvent e) {
    var item = e.getCursor();
    var click = e.getCurrentItem();
    List<CustomItem> list = new ArrayList<>();
    if (CustomItem.isCustomItem(item)) {
      list.add(CustomItem.getCustomItem(item));
    }
    if (CustomItem.isCustomItem(click)) {
      list.add(CustomItem.getCustomItem(click));
    }
    if (list.size() > 0) {
      if (list.stream().allMatch(i -> {
        if (i.canMoveOtherInv(e)) {
          return false;
        }
        var type = e.getView().getTopInventory().getType();
        return switch (type) {
          case CRAFTING, WORKBENCH -> false;
          default -> true;
        };
      })) {
        e.setCancelled(true);
        return;
      }
    }
    for (CustomItem customItem : list) {
      customItem.clickItem(e);
      if (e.isCancelled()) {
        return;
      }
    }
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void itemSpawn(ItemSpawnEvent e){
    var item = e.getEntity().getItemStack();
    if (CustomItem.isCustomItem(item)){
      CustomItem.getCustomItem(item).itemSpawn(e);
    }
  }
}
