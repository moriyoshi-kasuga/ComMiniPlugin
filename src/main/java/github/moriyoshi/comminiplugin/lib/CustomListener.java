package github.moriyoshi.comminiplugin.lib;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.PlayerCooldownItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public final class CustomListener implements Listener {

  private static final CustomListener INSTANCE = new CustomListener();

  private CustomListener() {
    new BukkitRunnable() {
      @Override
      public void run() {
        PlayerCooldownItem.allCountDown();
        Bukkit.getOnlinePlayers()
            .forEach(
                p -> {
                  val inv = p.getInventory();
                  val main = CustomItem.getCustomItem(inv.getItemInMainHand());
                  if (main != null) main.heldItem(p);
                  inv.forEach(
                      i -> {
                        val item = CustomItem.getCustomItem(i);
                        if (item != null) {
                          item.runTick(p);
                          if (item instanceof CooldownItem cooldownItem
                              && cooldownItem.shouldAutoReduceCountDown()
                              && cooldownItem.inCooldown()) {
                            cooldownItem.countDown();
                          }
                        }
                      });
                });
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  public static CustomListener getInstance() {
    return INSTANCE;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void dropItem(final PlayerDropItemEvent e) {
    val item = e.getItemDrop().getItemStack();
    if (ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_DROP).orElse(false)) {
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
    if (ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_CLICK_INTERACT).orElse(false)) {
      e.setCancelled(true);
      return;
    }
    val custom = CustomItem.getCustomItem(item);
    if (custom == null) {
      customBlockInteract(e);
      return;
    }
    if (e.getHand() == EquipmentSlot.OFF_HAND) {
      custom.interactOffHand(e);
    } else {
      custom.interactMainHand(e);
    }
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
    if (e.getHand() == EquipmentSlot.OFF_HAND) {
      block.interactOffHand(e);
    } else {
      block.interactMainHand(e);
    }
    return true;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void swapItem(final PlayerItemHeldEvent e) {
    val player = e.getPlayer();
    player.setVisibleByDefault(false);
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
    val typeFlag =
        switch (e.getView().getTopInventory().getType()) {
          case CRAFTING, WORKBENCH -> false;
          default -> true;
        };
    for (val item : items) {
      if (ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_MOVE_INV).orElse(false)) {
        e.setCancelled(true);
        return;
      }
      if (typeFlag
          && ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_MOVE_OTHER_INV)
              .orElse(false)) {
        e.setCancelled(true);
        return;
      }
    }
    for (val customItem :
        items.stream().map(CustomItem::getCustomItem).filter(Objects::nonNull).toList()) {
      customItem.clickItem(e);
      if (e.isCancelled()) {
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void itemSpawn(final ItemSpawnEvent e) {
    val item = e.getEntity().getItemStack();
    if (ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_ITEM_SPAWN).orElse(false)) {
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
    if (shooter instanceof Player player) {
      val item = player.getInventory().getItemInMainHand();
      val custom = CustomItem.getCustomItem(item);
      if (custom != null) {
        custom.projectileLaunch(e, player);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void damageEntity(final EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player player) {
      val item = player.getInventory().getItemInMainHand();
      if (ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_ATTACK).orElse(false)) {
        e.setCancelled(true);
        return;
      }
      if (e.getEntity() instanceof Player
          && ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_ATTACK_TO_PLAYER)
              .orElse(false)) {
        e.setCancelled(true);
        return;
      }
      val custom = CustomItem.getCustomItem(item);
      if (custom != null) {
        custom.damageEntity(e, player);
        if (e.isCancelled()) {
          return;
        }
      }
    }
    if (e.getEntity() instanceof Player player) {
      val item = player.getInventory().getItemInMainHand();
      val custom = CustomItem.getCustomItem(item);
      if (custom != null) {
        custom.damageByEntityMainHand(e, player);
        if (e.isCancelled()) {
          return;
        }
      }
      val off = player.getInventory().getItemInMainHand();
      val offCustom = CustomItem.getCustomItem(off);
      if (offCustom != null) {
        offCustom.damageByEntityOffHand(e, player);
      }
    }
  }
}
