package github.moriyoshi.comminiplugin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

  public static void removeItemStack(Player player, ItemStack itemStack, int amount) {
    ItemStack[] inventory = player.getInventory().getContents();
    for (ItemStack item : inventory) {
      if (item != null && item.isSimilar(itemStack)) {
        int iAmount = item.getAmount();
        if (iAmount <= amount) {
          item.setAmount(0);
          amount -= iAmount;
        } else {
          item.setAmount(iAmount - amount);
          break;
        }
      }
    }
  }

  public static int sumItemStack(Player player, ItemStack itemStack) {
    ItemStack[] inventory = player.getInventory().getContents();
    int count = 0;
    for (ItemStack item : inventory) {
      if (item != null && item.isSimilar(itemStack)) {
        count += item.getAmount();
      }
    }
    return count;
  }

  public static boolean hasItemStack(Player player, ItemStack itemStack, int amount) {
    return player.getInventory().containsAtLeast(itemStack, amount);
  }
}
