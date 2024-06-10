package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.JumpState;
import github.moriyoshi.comminiplugin.util.Util;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class SSJumpItem extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<yellow>ジャンプ");
  private static final Component DESCRIPTION = Util.mm("<red>注意! 着地した後一定時間操作不能");
  private static final int DEFAULT_COOLDOWN_TICK = 140;
  private static final Vector MULTIPLY = new Vector(3, 2.4, 3);

  public SSJumpItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name(DEFAULT_NAME)
            .lore(DESCRIPTION)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_OTHER_INV, true)
            .build());
  }

  public SSJumpItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(final PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    val p = e.getPlayer();
    val loc = p.getLocation();
    if (inCooldown()) {
      p.playSound(loc, Sound.BLOCK_DISPENSER_FAIL, 1, 1);
      return;
    }
    p.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 2, 1);
    setCooldown(DEFAULT_COOLDOWN_TICK);
    p.setFallDistance(0);
    BukkitUtil.setVelocity(p, loc.getDirection().multiply(MULTIPLY), JumpState.FREE);
    new ItemBuilder(getItem()).type(Material.CLOCK);
    new BukkitRunnable() {

      int num = 0;

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        p.setFallDistance(0);
        if (5 > num) {
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
  public void endCountDown() {
    new ItemBuilder(getItem()).type(Material.PHANTOM_MEMBRANE);
  }
}
