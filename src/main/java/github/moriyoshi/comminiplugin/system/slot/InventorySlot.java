package github.moriyoshi.comminiplugin.system.slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class InventorySlot extends ArrayList<Integer> {
  public InventorySlot() {
    super(IntStream.range(0, 36).boxed().toList());
  }

  public InventorySlot(final Collection<Integer> collection) {
    super(
        Stream.concat(collection.stream(), IntStream.range(collection.size(), 36).boxed())
            .limit(36)
            .toList());
  }

  // TOOD: inventory slot button

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
