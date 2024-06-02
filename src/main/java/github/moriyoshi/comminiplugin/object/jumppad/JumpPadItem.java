package github.moriyoshi.comminiplugin.object.jumppad;

import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class JumpPadItem extends CustomItem {

  public JumpPadItem() {
    this(
        new ItemBuilder(Material.SLIME_BLOCK)
            .name("<red>Jump Pad Tool")
            .lore(
                "<gray>Right Click to create!",
                "<gray>if look block is already jumppad,",
                "<gray> open jumppad settings menu",
                "<gray>Left Click to remove!")
            .build());
  }

  public JumpPadItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    e.setCancelled(true);
    val player = e.getPlayer();
    val block = e.getClickedBlock();
    if (block == null || block.isEmpty()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>please look at a target block");
      return;
    }
    if (e.getAction().isLeftClick()) {
      if (!CustomBlock.isCustomBlock(block, JumpPadBlock.class)) {
        ComMiniPrefix.SYSTEM.send(player, "<red>please look at a jumppad block");
        return;
      }
      CustomBlock.getCustomBlock(block).remove();
      ComMiniPrefix.SYSTEM.send(player, "<green>successfully removed jumppad block");
    } else {
      if (CustomBlock.isCustomBlock(block)) {
        if (!CustomBlock.isCustomBlock(block, JumpPadBlock.class)) {
          ComMiniPrefix.SYSTEM.send(
              player, "<red>please look at a jumppad block not a custom block");
          return;
        }
        CustomBlock.getCustomBlock(block, JumpPadBlock.class).settings(player);
        return;
      }
      new JumpPadBlock(block, player);
    }
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
