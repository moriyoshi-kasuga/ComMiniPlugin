package github.moriyoshi.comminiplugin.system.hotbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.val;

public abstract class HotbarSlot extends ArrayList<Integer> {
  public HotbarSlot() {
    super(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
  }

  public HotbarSlot(final Collection<Integer> collection) {
    super(collection);
  }

  public ItemStack[] toItemStacks() {
    return stream().map(slot -> getDefaults().get(slot).get()).toArray(ItemStack[]::new);
  }

  public void setItems(final Inventory inv) {
    var i = 0;
    for (val item : toItemStacks()) {
      inv.setItem(i, item);
      i++;
    }
  }

  public void swap(final int first, final int second) {
    this.set(first, this.set(second, this.get(first)));
  }

  protected abstract List<Supplier<ItemStack>> getDefaults();
}
