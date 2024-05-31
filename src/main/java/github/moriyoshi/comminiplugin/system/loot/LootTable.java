package github.moriyoshi.comminiplugin.system.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.val;

public class LootTable {

  public final List<Pool> pools;

  public LootTable() {
    this.pools = new ArrayList<>();
  }

  public LootTable(List<Pool> pools) {
    this.pools = pools;
  }

  public LootTable(Pool pool) {
    this.pools = new ArrayList<>() {
      {
        add(pool);
      }
    };
  }

  public List<ItemStack> random() {
    return pools.stream().map(pool -> pool.random()).flatMap(List::stream).collect(Collectors.toList());
  }

  public void fillInventory(Inventory inventory) {
    val range = IntStream.range(0, inventory.getSize()).filter(i -> {
      val item = inventory.getItem(i);
      return item == null || item.isEmpty();
    }).boxed().collect(Collectors.toList());
    Collections.shuffle(range);
    for (val item : random()) {
      if (range.isEmpty()) {
        break;
      }
      inventory.setItem(range.remove(0), item);
    }
  }

  public void setChest(Location location) {
    location.getBlock().setType(Material.CHEST);
    fillInventory(((Chest) location.getBlock().getState()).getInventory());
  }

}
