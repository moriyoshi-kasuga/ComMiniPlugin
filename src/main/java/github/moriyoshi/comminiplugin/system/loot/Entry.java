package github.moriyoshi.comminiplugin.system.loot;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import lombok.NonNull;

public class Entry {

  public final Supplier<ItemStack> supplier;
  public final double weight;
  public final BooleanSupplier condition;

  public Entry(@NonNull Supplier<ItemStack> supplier) {
    this.supplier = supplier;
    this.weight = 1;
    this.condition = () -> true;
  }

  public Entry(@NonNull Supplier<ItemStack> supplier, double weight) {
    this.supplier = supplier;
    this.weight = weight;
    this.condition = () -> true;
  }

  public Entry(@NonNull Supplier<ItemStack> supplier, double weight, @NonNull BooleanSupplier condition) {
    this.supplier = supplier;
    this.weight = weight;
    this.condition = condition;
  }

  public Entry(@NonNull Supplier<ItemStack> supplier, @NonNull BooleanSupplier condition) {
    this.supplier = supplier;
    this.weight = 1;
    this.condition = condition;
  }

}
