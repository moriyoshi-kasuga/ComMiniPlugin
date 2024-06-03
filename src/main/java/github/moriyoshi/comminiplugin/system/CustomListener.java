package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.item.PlayerCooldownItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Arrays;
import java.util.Objects;
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
                              val item = CustomItem.getCustomItem(i);
                              if (item != null) item.runTick(p);
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
    val customItem = CustomItem.getCustomItem(item);
    if (customItem != null) customItem.dropItem(e);
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
    val custom = CustomItem.getCustomItem(item);
    if (custom == null) {
      customBlockInteract(e);
      return;
    }
    custom.interact(e);
    if (e.useInteractedBlock() != Result.DENY) {
      customBlockInteract(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void blockBreak(final BlockBreakEvent e) {
    val item = e.getPlayer().getInventory().getItemInMainHand();
    val custom = CustomItem.getCustomItem(item);
    if (custom != null) {
      custom.blockBreak(e);
      if (e.isCancelled()) {
        return;
      }
    }
    val block = CustomBlock.getCustomBlock(e.getBlock());
    if (block == null) {
      return;
    }
    e.setCancelled(true);
    e.setDropItems(false);
    block.blockBreak(e);
  }

  public boolean customBlockInteract(PlayerInteractEvent e) {
    if (!e.hasBlock()) {
      return false;
    }
    val block = CustomBlock.getCustomBlock(e.getClickedBlock());
    if (block == null) {
      return false;
    }
    e.setCancelled(true);
    block.interact(e);
    return true;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void swapItem(final PlayerItemHeldEvent e) {
    val player = e.getPlayer();
    val inventory = player.getInventory();
    val previousItem = inventory.getItem(e.getPreviousSlot());
    val newItem = inventory.getItem(e.getNewSlot());
    val previousCustom = CustomItem.getCustomItem(previousItem);
    if (previousCustom != null) {
      previousCustom.heldOfOther(e);
      if (e.isCancelled()) {
        return;
      }
    }
    val newCustom = CustomItem.getCustomItem(newItem);
    if (newCustom != null) {
      newCustom.heldOfThis(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void offhandItem(final PlayerSwapHandItemsEvent e) {
    val main = e.getMainHandItem();
    val off = e.getOffHandItem();
    val offCustom = CustomItem.getCustomItem(off);
    if (offCustom != null) {
      offCustom.swapToOffHand(e);
      if (e.isCancelled()) {
        return;
      }
    }
    val mainCustom = CustomItem.getCustomItem(main);
    if (mainCustom != null) {
      mainCustom.swapToMainHand(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void inventoryClick(final InventoryClickEvent e) {
    val items = Arrays.asList(e.getCursor(), e.getCurrentItem());
    for (val item : items) {
      if (!ItemBuilder.getCustomItemFlag(item, CustomItemFlag.MOVE_INV).orElse(true)) {
        e.setCancelled(true);
        return;
      }
    }
    val customs =
        items.stream()
            .filter(item -> !(item == null || item.isEmpty()))
            .map(
                item -> {
                  return CustomItem.getCustomItem(item);
                })
            .filter(Objects::nonNull)
            .toList();
    if (customs.isEmpty()) {
      return;
    }
    if (customs.stream()
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
    for (val customItem : customs) {
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
    val custom = CustomItem.getCustomItem(item);
    if (custom != null) {
      custom.itemSpawn(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void projectileLaunch(ProjectileLaunchEvent e) {
    val shooter = e.getEntity().getShooter();
    if (shooter != null && shooter instanceof Player player) {
      val item = player.getInventory().getItemInMainHand();
      val custom = CustomItem.getCustomItem(item);
      if (custom != null) {
        custom.projectileLaunch(e, player);
      }
    }
  }
}
