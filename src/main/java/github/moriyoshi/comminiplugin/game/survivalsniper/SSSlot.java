package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.system.slot.InventorySlot;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SSSlot extends InventorySlot {

  public SSSlot() {
    super();
  }

  public SSSlot(final Collection<Integer> collection) {
    super(collection);
  }

  @Override
  protected ItemStack apply(int slot) {
    return switch (slot) {
      case 0 -> new SSSniper().getItem();
      case 1 -> new SSJumpItem().getItem();
      case 2 ->
          new ItemBuilder(Material.IRON_AXE)
              .name("<red>Iron Axe")
              .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
              .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
              .customItemFlag(CustomItemFlag.DISABLE_ATTACK_TO_PLAYER, true)
              .build();
      case 3 ->
          new ItemBuilder(Material.IRON_PICKAXE)
              .name("<red>Iron Pickaxe")
              .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
              .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
              .customItemFlag(CustomItemFlag.DISABLE_ATTACK_TO_PLAYER, true)
              .build();
      case 4 -> new ItemBuilder(Material.OAK_SAPLING).amount(3).build();
      case 5 -> new ItemBuilder(Material.BONE_MEAL).amount(10).build();
      case 6 -> new ItemBuilder(Material.DIRT).amount(63).build();
      case 7 -> new ItemBuilder(Material.COOKED_MUTTON).amount(3).build();
      default -> null;
    };
  }
}
