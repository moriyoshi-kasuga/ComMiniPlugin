package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import net.kyori.adventure.text.Component;

public class Jump extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<yellow>ジャンプ");
  private static final Component DESCRIPTION = Util.mm("<red>注意! 着地した後一定時間操作不能");
  private static final int DEFAULT_COOLDOWN_TICK = 100;
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
  public boolean isInteractCancel() {
    return true;
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    e.setCancelled(true);
    if (e.getAction().isLeftClick()) {
      return;
    }
    var p = e.getPlayer();
    var eyeLoc = p.getEyeLocation();
    if (inCooldown()) {
      p.playSound(eyeLoc, Sound.BLOCK_DISPENSER_FAIL, 1, 1);
      return;
    }
    setCooldown(DEFAULT_COOLDOWN_TICK);
    p.setVelocity(eyeLoc.getDirection().multiply(MULTIPLY));
    p.getInventory().setItemInMainHand(new ItemBuilder(getItem()).type(Material.CLOCK).build());
    new BukkitRunnable() {

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        p.setFallDistance(0);
        if (p.isInWater()) {
          this.cancel();
        }
        if (p.isOnGround()) {
          p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 30, 138, true));
          p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 256, true));
          this.cancel();
        }
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 10, 1);
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

}
