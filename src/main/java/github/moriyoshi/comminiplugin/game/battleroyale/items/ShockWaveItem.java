package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.JumpState;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShockWaveItem extends CustomItem {
  public ShockWaveItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#874B2A>ショックウェーブ")
            .lore(
                "<gray>左クリックで半径15ブロック以内の相手すベてを",
                " <gray>遠くに（縦横５～７ブロック）吹き飛ばす",
                "<gray>右クリックは自分を前に移動させる")
            .customModelData(24)
            .build());
  }

  public ShockWaveItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void damageEntity(EntityDamageByEntityEvent e, Player player) {
    spawn(player, true);
  }

  private void spawn(Player player, boolean isLeftClick) {
    val loc = player.getLocation();
    val world = loc.getWorld();
    useItemAmount();
    if (isLeftClick) {
      val vec = loc.toVector();
      loc.getNearbyPlayers(15, p -> !player.equals(p))
          .forEach(
              p ->
                  BukkitUtil.setVelocity(
                      p,
                      p.getLocation().toVector().subtract(vec).normalize().multiply(2),
                      JumpState.FREE));
      world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 3, 1);
    } else {
      loc.setPitch(Math.min(Math.max(loc.getPitch(), -20), 20));
      BukkitUtil.setVelocity(player, loc.getDirection().multiply(2), JumpState.FREE);
      world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 3, 1);
    }
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    spawn(e.getPlayer(), e.getAction().isLeftClick());
  }

  @Override
  public boolean canStack() {
    return true;
  }
}

