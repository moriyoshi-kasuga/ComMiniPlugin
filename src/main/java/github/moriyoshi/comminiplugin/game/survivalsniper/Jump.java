package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import net.kyori.adventure.text.Component;

public class Jump extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<yellow>ジャンプ");
  private static final Component DESCRIPTION = Util.mm("<red>注意! 着地した後一定時間操作不能");
  private static final int DEFAULT_COOLDOWN_TICK = 140;
  private static final Vector MULTIPLY = new Vector(3, 2.4, 3);

  public Jump() {
    this(new ItemBuilder(Material.PHANTOM_MEMBRANE).name(DEFAULT_NAME).lore(DESCRIPTION).build());
  }

  public Jump(ItemStack item) {
    super(item);
  }

  @Override
  public @NotNull String getIdentifier() {
    return "jump";
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(false);
      return;
    }
    var p = e.getPlayer();
    var eyeLoc = p.getEyeLocation();
    if (inCooldown()) {
      if ((DEFAULT_COOLDOWN_TICK - 10) > getCooldown()) {
        p.playSound(eyeLoc, Sound.BLOCK_DISPENSER_FAIL, 1, 1);
      }
      return;
    }
    p.getWorld().playSound(eyeLoc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 2, 1);
    setCooldown(DEFAULT_COOLDOWN_TICK);
    p.setFallDistance(0);
    p.setVelocity(eyeLoc.getDirection().multiply(MULTIPLY));
    p.getInventory().setItemInMainHand(new ItemBuilder(getItem()).type(Material.CLOCK).build());
    new BukkitRunnable() {

      int num = 0;

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        p.setFallDistance(0);
        if (20 > num) {
          num++;
          return;
        }
        if (p.isInWater()) {
          this.cancel();
        }
        if (p.isOnGround()) {
          BukkitUtil.disableMove(p, 30);
          this.cancel();
        }
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
  }

  @Override
  public void runTick(Player player) {
    if (!countDown()) {
      new ItemBuilder(getItem()).type(Material.PHANTOM_MEMBRANE);
    }
  }

  @Override
  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return false;
  }

  @Override
  public void dropItem(PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public void itemSpawn(ItemSpawnEvent e) {
    e.getEntity().remove();
  }
}
