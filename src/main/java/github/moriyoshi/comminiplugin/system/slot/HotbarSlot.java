package github.moriyoshi.comminiplugin.system.slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import lombok.val;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class HotbarSlot extends ArrayList<Integer> {
  public HotbarSlot() {
    super(IntStream.range(0, 9).boxed().toList());
  }

  public HotbarSlot(final Collection<Integer> collection) {
    super(collection.stream().limit(9).toList());
  }

  public final List<ItemStack> toItemStacks() {
    return stream().map(this::apply).toList();
  }

  public void setItems(final Inventory inv) {
    val iter = toItemStacks().listIterator();
    while (iter.hasNext()) {
      inv.setItem(iter.nextIndex(), iter.next());
    }
  }

  public void swap(final int first, final int second) {
    Collections.swap(this, first, second);
  }

  protected abstract ItemStack apply(int slot);
}
