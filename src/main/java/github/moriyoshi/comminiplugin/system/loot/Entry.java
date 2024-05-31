package github.moriyoshi.comminiplugin.system.loot;

import java.util.function.BooleanSupplier;

import org.bukkit.inventory.ItemStack;

import lombok.NonNull;

public class Entry {

  public final ItemStack itemStack;
  public final double weight;
  public final BooleanSupplier condition;

  public Entry(@NonNull ItemStack itemStack) {
    this.itemStack = itemStack;
    this.weight = 1;
    this.condition = () -> true;
  }

  public Entry(@NonNull ItemStack itemStack, double weight) {
    this.itemStack = itemStack;
    this.weight = weight;
    this.condition = () -> true;
  }

  public Entry(@NonNull ItemStack itemStack, double weight, @NonNull BooleanSupplier condition) {
    this.itemStack = itemStack;
    this.weight = weight;
    this.condition = condition;
  }

  public Entry(@NonNull ItemStack itemStack, @NonNull BooleanSupplier condition) {
    this.itemStack = itemStack;
    this.weight = 1;
    this.condition = condition;
  }

}
