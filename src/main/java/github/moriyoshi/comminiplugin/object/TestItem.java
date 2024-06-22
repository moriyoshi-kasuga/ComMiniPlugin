package github.moriyoshi.comminiplugin.object;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.biggame.battleroyale.TreasureChest;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import java.util.List;
import java.util.Optional;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TestItem extends CustomItem {

  public TestItem() {
    this(new ItemBuilder(Material.BARRIER).name("<red>test").build());
  }

  public TestItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    e.setCancelled(true);
    val p = e.getPlayer();
    // treasure(e);
  }

  private void treasure(PlayerInteractEvent e) {
    val player = e.getPlayer();
    val block = player.getTargetBlockExact(10);
    if (block == null || block.isEmpty()) {
      ComMiniPlugin.SYSTEM.send(player, "<red>please look at a target block");
      return;
    }
    if (e.getAction().isLeftClick()) {
      if (!CustomBlock.isCustomBlock(block, TreasureChest.class)) {
        ComMiniPlugin.SYSTEM.send(player, "<red>please look at a Treasure block");
        return;
      }
      CustomBlock.getCustomBlock(block).remove();
      ComMiniPlugin.SYSTEM.send(player, "<green>successfully removed Treasure block");
    } else {
      if (CustomBlock.isCustomBlock(block)) {
        ComMiniPlugin.SYSTEM.send(player, "<red>already a custom block");
        return;
      }
      AnvilInputs.getInput(
              ComMiniPlugin.getPlugin(),
              "<red>Create Treasure Chest",
              (str, state) -> {
                try {
                  return Optional.of(Integer.parseInt(str));
                } catch (NumberFormatException ignore) {
                  return Optional.empty();
                }
              },
              (num, state) -> {
                new TreasureChest(block, state.getPlayer().getFacing(), num);
                return List.of(ResponseAction.close());
              })
          .open(player);
    }
  }
}
