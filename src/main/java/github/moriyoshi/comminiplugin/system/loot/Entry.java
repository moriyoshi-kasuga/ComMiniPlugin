package github.moriyoshi.comminiplugin.system.loot;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class Entry {

  public final Supplier<ItemStack> supplier;
  public final double weight;
  public final BooleanSupplier condition;
  @Getter @Setter private Function<Random, Integer> itemRolls = null;

  @Getter private boolean isDuplicatable = false;

  public Entry(double weight, @NonNull Supplier<ItemStack> supplier) {
    this(weight, supplier, () -> true);
  }

  public Entry(
      double weight, @NonNull Supplier<ItemStack> supplier, @NonNull BooleanSupplier condition) {
    this.weight = weight;
    this.supplier = supplier;
    this.condition = condition;
  }

  public Entry setItemRolls(int origin, int below) {
    this.itemRolls = (r) -> r.nextInt(origin, below + 1);
    return this;
  }

  public Entry setDuplicatable(boolean isDuplicatable) {
    this.isDuplicatable = isDuplicatable;
    return this;
  }

  public Entry enableDuplicate() {
    this.isDuplicatable = true;
    return this;
  }
}
