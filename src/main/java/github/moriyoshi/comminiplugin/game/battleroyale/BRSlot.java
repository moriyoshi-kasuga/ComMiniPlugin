package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.system.slot.HotbarSlot;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BRSlot extends HotbarSlot {

  public BRSlot() {
    super();
  }

  public BRSlot(final Collection<Integer> collection) {
    super(collection);
  }

  @Override
  protected ItemStack apply(int slot) {
    return switch (slot) {
      case 0 -> new ItemBuilder(Material.BOW).unbreakable(true).build();
      case 1 -> new ItemBuilder(Material.CROSSBOW).unbreakable(true).build();
      case 2 -> new ItemBuilder(Material.ARROW).amount(20).build();
      default -> null;
    };
  }
}
