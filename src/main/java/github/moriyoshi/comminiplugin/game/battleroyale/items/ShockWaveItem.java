package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadBlock;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadBlock.JUMP_STATE;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
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
  public void interact(PlayerInteractEvent e) {
    val player = e.getPlayer();
    val loc = player.getLocation();
    val world = loc.getWorld();
    useItemAmount();
    if (e.getAction().isLeftClick()) {
      val vec = loc.toVector();
      loc.getNearbyPlayers(15, p -> !player.equals(p))
          .forEach(
              p -> JumpPadBlock.setVelocity(
                  p,
                  p.getLocation().toVector().subtract(vec).normalize().multiply(2),
                  JUMP_STATE.FREE));
      world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 3, 1);
    } else {
      loc.setPitch(Math.min(Math.max(loc.getPitch(), -20), 20));
      JumpPadBlock.setVelocity(player, loc.getDirection().multiply(2), JUMP_STATE.FREE);
      world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 3, 1);
    }
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
