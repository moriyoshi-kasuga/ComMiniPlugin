package github.moriyoshi.comminiplugin.system.hotbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

public abstract class HotBarSlot extends ArrayList<Integer> {
  protected abstract List<Supplier<ItemStack>> getDefaults();

  public HotBarSlot(Collection<Integer> collection) {
    super(collection);
  }

  public ItemStack[] toItemStacks() {
    return stream().map(slot -> getDefaults().get(slot).get()).toArray(ItemStack[]::new);
  }

  public void swap(int first, int second) {
    Collections.swap(this, first, second);
  }
}
