package github.moriyoshi.comminiplugin.system.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootTable {

  private final List<Pool> pools;

  public LootTable() {
    this.pools = new ArrayList<>();
  }

  public LootTable(List<Pool> pools) {
    this.pools = pools;
  }

  public LootTable(Pool pool) {
    this.pools =
        new ArrayList<>() {
          {
            add(pool);
          }
        };
  }

  public List<ItemStack> random() {
    val r = new Random();
    return pools.stream()
        .map(pool -> pool.random(r))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  public void fillInventory(Inventory inventory) {
    val range =
        IntStream.range(0, inventory.getSize())
            .filter(
                i -> {
                  val item = inventory.getItem(i);
                  return item == null || item.isEmpty();
                })
            .boxed()
            .collect(Collectors.toList());
    Collections.shuffle(range);
    for (val item : random()) {
      if (range.isEmpty()) {
        break;
      }
      inventory.setItem(range.removeFirst(), item);
    }
  }

  public void setChest(Location location) {
    location.getBlock().setType(Material.CHEST);
    fillInventory(((Chest) location.getBlock().getState()).getInventory());
  }
}
