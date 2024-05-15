package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class SSSlot extends ArrayList<Integer> {
  public static final Map<Integer, Supplier<ItemStack>> defaults = new HashMap<>() {
    {
      put(0, () -> new Sniper().getItem());
      put(1, () -> new Jump().getItem());
      put(2, () -> new ItemStack(Material.IRON_AXE));
      put(3, () -> new ItemStack(Material.IRON_PICKAXE));
      put(4, () -> new ItemBuilder(Material.OAK_SAPLING).amount(3).build());
      put(5, () -> new ItemBuilder(Material.BONE_MEAL).amount(10).build());
      put(6, () -> new ItemBuilder(Material.DIRT).amount(64).build());
      put(7, () -> new ItemBuilder(Material.COOKED_MUTTON).amount(3).build());
      put(8, () -> null);
    }
  };

  public SSSlot(Collection<Integer> collection) {
    super(collection);
  }

  public ItemStack[] toItemStacks() {
    return stream().map(slot -> defaults.get(slot).get()).toArray(ItemStack[]::new);
  }
}