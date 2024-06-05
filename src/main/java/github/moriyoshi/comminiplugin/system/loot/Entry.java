package github.moriyoshi.comminiplugin.system.loot;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

public class Entry {

  public final Supplier<ItemStack> supplier;
  public final double weight;
  public final BooleanSupplier condition;

  @Getter private boolean isDuplicate = false;

  public Entry setDuplicate(boolean isDuplicate) {
    this.isDuplicate = isDuplicate;
    return this;
  }

  public Entry enableDuplicate() {
    this.isDuplicate = true;
    return this;
  }

  public Entry(double weight) {
    this(weight, () -> null);
  }

  public Entry(@NonNull Supplier<ItemStack> supplier) {
    this(1, supplier);
  }

  public Entry(double weight, @NonNull Supplier<ItemStack> supplier) {
    this(weight, supplier, () -> true);
  }

  public Entry(
      double weight, @NonNull Supplier<ItemStack> supplier, @NonNull BooleanSupplier condition) {
    this.weight = weight;
    this.supplier = supplier;
    this.condition = condition;
  }

  public Entry(@NonNull Supplier<ItemStack> supplier, @NonNull BooleanSupplier condition) {
    this(1, supplier, condition);
  }
}
