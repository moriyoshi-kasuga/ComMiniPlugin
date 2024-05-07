package github.moriyoshi.comminiplugin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

  /**
   * プレイヤーから特定のアイテムを個数文消します
   *
   * @param player    消すプレイヤー
   * @param itemStack 消すアイテム
   * @param amount    消す数
   */
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

  /**
   * プレイヤーの持っているアイテムの個数を数えます
   *
   * @param player    数えるプレイヤー
   * @param itemStack 数えるアイテム
   * @return 合計
   */
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

  /**
   * プレイヤーがアイテムを個数以上持っているか
   *
   * @param player    player
   * @param itemStack item
   * @param amount    amount
   * @return 持っているならtrue
   */
  public static boolean hasItemStack(Player player, ItemStack itemStack, int amount) {
    return player.getInventory().containsAtLeast(itemStack, amount);
  }
}
