package github.moriyoshi.comminiplugin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

  /**
   * プレイヤーから特定のアイテムを個数文消します
   *
   * @param player    消すプレイヤー
   * @param itemStack 消すアイテム
   * @param amount    消す数
   */
  public static void removeItemStack(final Player player, final ItemStack itemStack, int amount) {
    final ItemStack[] inventory = player.getInventory().getContents();
    for (final ItemStack item : inventory) {
      if (item != null && item.isSimilar(itemStack)) {
        final int iAmount = item.getAmount();
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
  public static int sumItemStack(final Player player, final ItemStack itemStack) {
    final ItemStack[] inventory = player.getInventory().getContents();
    int count = 0;
    for (final ItemStack item : inventory) {
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
  public static boolean hasItemStack(final Player player, final ItemStack itemStack, final int amount) {
    return player.getInventory().containsAtLeast(itemStack, amount);
  }
}
