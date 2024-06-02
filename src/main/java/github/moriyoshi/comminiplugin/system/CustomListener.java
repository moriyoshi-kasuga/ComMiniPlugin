package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.item.PlayerCooldownItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomListener implements Listener {

  private static final CustomListener INSTANCE = new CustomListener();

  private CustomListener() {
    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getOnlinePlayers()
            .forEach(
                p ->
                    p.getInventory()
                        .forEach(
                            i -> {
                              if (CustomItem.isCustomItem(i)) {
                                CustomItem.getCustomItem(i).runTick(p);
                              }
                            }));

        val it = PlayerCooldownItem.COOLDOWN.entrySet().iterator();
        while (it.hasNext()) {
          val entry = it.next();
          var num = entry.getValue();
          if (0 >= --num) {
            PlayerCooldownItem.COOLDOWN.remove(entry.getKey());
            return;
          }
          PlayerCooldownItem.COOLDOWN.put(entry.getKey(), num);
        }
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  public static CustomListener getInstance() {
    return INSTANCE;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dropItem(final PlayerDropItemEvent e) {
    val item = e.getItemDrop().getItemStack();
    if (!ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DROP).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!CustomItem.isCustomItem(item)) {
      return;
    }
    val customItem = CustomItem.getCustomItem(item);
    customItem.dropItem(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void shiftItem(final PlayerToggleSneakEvent e) {
    val player = e.getPlayer();
    val inv = player.getInventory();
    val heldSlot = inv.getHeldItemSlot();
    CustomItem.getCustomItemOptional(inv.getItemInMainHand())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.HAND));
    CustomItem.getCustomItemOptional(inv.getItemInOffHand())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.OFF_HAND));
    CustomItem.getCustomItemOptional(inv.getHelmet())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.HEAD));
    CustomItem.getCustomItemOptional(inv.getChestplate())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.CHEST));
    CustomItem.getCustomItemOptional(inv.getLeggings())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.LEGS));
    CustomItem.getCustomItemOptional(inv.getBoots())
        .ifPresent(custom -> custom.shiftItem(e, EquipmentSlot.FEET));
    for (int i = 0; i < 36; i++) {
      if (heldSlot == i) {
        continue;
      }
      val item = inv.getItem(i);
      CustomItem.getCustomItemOptional(item).ifPresent(custom -> custom.shiftItem(e, null));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void interact(final PlayerInteractEvent e) {
    val item = e.getItem();
    if (!ItemBuilder.getCustomItemFlag(item, CustomItemFlag.CLICK_INTERACT).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!CustomItem.isCustomItem(item)) {
      customBlockInteract(e);
      return;
    }
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
    val player = e.getPlayer();
    val inventory = player.getInventory();
    val previousItem = inventory.getItem(e.getPreviousSlot());
    val newItem = inventory.getItem(e.getNewSlot());
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
    if (!ItemBuilder.getCustomItemFlag(item, CustomItemFlag.MOVE_INV).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (!ItemBuilder.getCustomItemFlag(click, CustomItemFlag.MOVE_INV).orElse(true)) {
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
      if (list.stream()
          .anyMatch(
              i -> {
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
    if (!ItemBuilder.getCustomItemFlag(item, CustomItemFlag.ITEM_SPAWN).orElse(true)) {
      e.setCancelled(true);
      return;
    }
    if (CustomItem.isCustomItem(item)) {
      CustomItem.getCustomItem(item).itemSpawn(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void projectileLaunch(ProjectileLaunchEvent e) {
    val shooter = e.getEntity().getShooter();
    if (shooter != null && shooter instanceof Player player) {
      val item = player.getInventory().getItemInMainHand();
      if (CustomItem.isCustomItem(item)) {
        CustomItem.getCustomItem(item).projectileLaunch(e, player);
      }
    }
  }
}
