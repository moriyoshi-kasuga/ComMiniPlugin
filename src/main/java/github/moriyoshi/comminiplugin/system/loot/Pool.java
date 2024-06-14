package github.moriyoshi.comminiplugin.system.loot;

import github.moriyoshi.comminiplugin.lib.RandomCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import lombok.val;
import org.bukkit.inventory.ItemStack;

public class Pool {

  protected final Set<Entry> entries = new HashSet<>();
  private final int rolls;
  private final Function<Random, Integer> bonusRolls;

  public Pool() {
    this(1);
  }

  public Pool(int rolls) {
    this.rolls = rolls;
    this.bonusRolls = (r) -> 0;
  }

  public Pool(IntSupplier bonusRolls) {
    this.rolls = 1;
    this.bonusRolls = (r) -> bonusRolls.getAsInt();
  }

  public Pool(int rolls, IntSupplier bonusRolls) {
    this.rolls = rolls;
    this.bonusRolls = (r) -> bonusRolls.getAsInt();
  }

  /**
   * origin 以上、 below 以下の範囲
   *
   * @param rolls default
   * @param origin origin
   * @param below below
   */
  public Pool(int rolls, int origin, int below) {
    this.rolls = rolls;
    this.bonusRolls = (r) -> r.nextInt(origin, below + 1);
  }

  public Pool(int rolls, Function<Random, Integer> bonusRolls) {
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

  public List<ItemStack> random(Random r) {
    final RandomCollection<Entry> random = new RandomCollection<>(r);
    val list = new ArrayList<ItemStack>();
    entries.forEach(entry -> random.add(entry.weight, entry));
    for (int i = 0; i < (rolls + bonusRolls.apply(r)); i++) {
      val entry = random.next();
      if (!entry.isDuplicatable()) {
        random.remove(entry);
      }
      val item = entry.supplier.get();
      if (item != null && !item.isEmpty() && entry.condition.getAsBoolean()) {
        if (entry.getItemRolls() != null) {
          item.setAmount(entry.getItemRolls().apply(r));
          if (item.isEmpty()) {
            continue;
          }
        }
        list.add(item);
      }
    }

    return list;
  }
}
