package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.system.hotbar.HotbarSlot;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SSSlot extends HotbarSlot {

  public SSSlot() {
    super();
  }

  public SSSlot(final Collection<Integer> collection) {
    super(collection);
  }

  @Override
  protected List<Supplier<ItemStack>> getDefaults() {
    return generate(
        () -> new Sniper().getItem(),
        () -> new JumpItem().getItem(),
        () ->
            new ItemBuilder(Material.IRON_AXE)
                .name("<red>Iron Axe")
                .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
                .addAttribute(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier("damage", -8, Operation.ADD_NUMBER))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build(),
        () ->
            new ItemBuilder(Material.IRON_PICKAXE)
                .name("<red>Iron Pickaxe")
                .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
                .addAttribute(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier("damage", -3, Operation.ADD_NUMBER))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build(),
        () -> new ItemBuilder(Material.OAK_SAPLING).amount(3).build(),
        () -> new ItemBuilder(Material.BONE_MEAL).amount(10).build(),
        () -> new ItemBuilder(Material.DIRT).amount(64).build(),
        () -> new ItemBuilder(Material.COOKED_MUTTON).amount(3).build(),
        () -> null);
  }
}
