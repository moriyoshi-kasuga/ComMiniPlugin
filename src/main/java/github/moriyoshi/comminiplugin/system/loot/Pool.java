package github.moriyoshi.comminiplugin.system.loot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.util.RandomCollection;
import lombok.val;

public class Pool {

  public final double rolls;
  public final IntSupplier bonusRolls;

  protected final Set<Entry> entries = new HashSet<>();

  public Pool() {
    this(1);
  }

  public Pool(double rolls) {
    this.rolls = rolls;
    this.bonusRolls = () -> 0;
  }

  public Pool(IntSupplier bonusRolls) {
    this.rolls = 1;
    this.bonusRolls = bonusRolls;
  }

  public Pool(double rolls, IntSupplier bonusRolls) {
    this.rolls = rolls;
    this.bonusRolls = bonusRolls;
  }

  public int size() {
    return entries.size();
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public Pool add(Entry e) {
    entries.add(e);
    return this;
  }

  public Pool addAll(Collection<? extends Entry> c) {
    entries.addAll(c);
    return this;
  }

  public List<ItemStack> random() {
    final RandomCollection<Entry> random = new RandomCollection<>();
    val list = new ArrayList<ItemStack>();
    entries.forEach(entry -> random.add(entry.weight, entry));
    for (int i = 0; i < (rolls + bonusRolls.getAsInt()); i++) {
      val entry = random.next();
      val item = entry.supplier.get();
      if (!item.isEmpty() && entry.condition.getAsBoolean()) {
        list.add(item);
      }
    }

    return list;
  }
}
