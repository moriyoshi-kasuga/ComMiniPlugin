package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.system.hotbar.HotbarSlot;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class BRSlot extends HotbarSlot {

  public BRSlot() {
    super();
  }

  public BRSlot(final Collection<Integer> collection) {
    super(collection);
  }

  @Override
  protected List<Supplier<ItemStack>> getDefaults() {
    return generate(
        () -> new ItemBuilder(Material.BOW).unbreakable(true).build(),
        () -> new ItemBuilder(Material.CROSSBOW).unbreakable(true).build(),
        () -> new ItemBuilder(Material.ARROW).amount(20).build(),
        () -> null,
        () -> null,
        () -> null,
        () -> null,
        () -> null,
        () -> null);
  }

}
