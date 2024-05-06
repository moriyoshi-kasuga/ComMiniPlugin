package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class SurvivalSniperSlot {
  @Getter
  public final List<Integer> slots;

  public static final Map<Integer, Supplier<ItemStack>> defaults = new HashMap<>() {
    {
      put(0, () -> new Sniper().getItem());
      put(1, () -> new Jump().getItem());
      put(2, () -> new ItemStack(Material.IRON_AXE));
      put(3, () -> new ItemStack(Material.IRON_PICKAXE));
      put(4, () -> new ItemBuilder(Material.OAK_SAPLING).amount(3).build());
      put(5, () -> new ItemBuilder(Material.BONE_MEAL).amount(10).build());
      put(6, () -> new ItemBuilder(Material.DIRT).amount(64).build());
      put(7, () -> new ItemBuilder(Material.BREAD).amount(5).build());
      put(8, () -> null);
    }
  };

  public ItemStack[] toItemStacks() {
    return slots.stream().map(slot -> defaults.get(slot).get()).toArray(ItemStack[]::new);
  }
}
