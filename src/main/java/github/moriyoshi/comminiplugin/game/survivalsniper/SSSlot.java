package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.system.hotbar.HotBarSlot;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class SSSlot extends HotBarSlot {

  public SSSlot(Collection<Integer> collection) {
    super(collection);
  }

  @Override
  protected List<Supplier<ItemStack>> getDefaults() {
    return List.of(
        () -> new Sniper().getItem(),
        () -> new Jump().getItem(),
        () -> new ItemStack(Material.IRON_AXE),
        () -> new ItemStack(Material.IRON_PICKAXE),
        () -> new ItemBuilder(Material.OAK_SAPLING).amount(3).build(),
        () -> new ItemBuilder(Material.BONE_MEAL).amount(10).build(),
        () -> new ItemBuilder(Material.DIRT).amount(64).build(),
        () -> new ItemBuilder(Material.COOKED_MUTTON).amount(3).build(),
        () -> null);
  }
}
