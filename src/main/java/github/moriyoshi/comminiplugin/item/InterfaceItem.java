package github.moriyoshi.comminiplugin.item;

import java.util.UUID;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InterfaceItem {

  // カスタムアイテムのnbtのpathです
  String nbtKey = "customitem";

  /**
   * アイテムの識別子を取得します
   *
   * @return 識別子
   */
  @NotNull
  String getIdentifier();

  /**
   * アイテムのUUIDです
   *
   * @return uuid
   */
  @NotNull
  UUID getUniqueId();

  /**
   * これを true にするとスタックできます、しかし内部でuuidを固定にしているので、<br>
   * {@code #getItemKey()} を使った処理が使えなくなります
   *
   * @return can stack
   */
  default boolean canStack() {
    return false;
  }

  default boolean canMoveOtherInv(final InventoryClickEvent e) {
    return true;
  }

  /**
   * {@code CustomItemsCommand } に載せるかどうか?
   *
   * @return true なら載せる
   */
  default boolean canShowing() {
    return true;
  }

  /**
   * このアイテムのキーを取得します {@code #getUniqueId()} が null の場合はエラーがでます<br>
   * その時は {code {@link #getItemKey(Player)}} を使用してください
   */
  @NotNull
  default CustomItemKey getItemKey() {
    return new CustomItemKey(getIdentifier(), getUniqueId());
  }

  /**
   * {@code #getUniqueId()} が null の場合 こちらを使ってください <br>
   * プレイヤーで共有のアイテムのキーを作成したい場合に使ってください
   *
   * @param player
   */
  @NotNull
  default CustomItemKey getItemKey(Player player) {
    return new CustomItemKey(getIdentifier(), player.getUniqueId());
  }

  /**
   * {@code #getUniqueId()} が null の場合 こちらを使ってください <br>
   * プレイヤーで共有のアイテムのキーを作成したい場合に使ってください
   *
   * @param uuid uuid
   */
  @NotNull
  default CustomItemKey getItemKey(@NonNull UUID uuid) {
    return new CustomItemKey(getIdentifier(), uuid);
  }

  /**
   * このカスタムアイテムのインスタンスで使用しているアイテムを返します
   *
   * @return itemstack
   */
  @NotNull
  ItemStack getItem();

  /**
   * アイテムを持っている間だけ1tickごとにする処理です
   *
   * @return 処理
   */
  default void heldItem(final Player player) {}

  /**
   * プレイヤーのインベントリーにあるさいに常に更新されるアイテムです
   *
   * @param player player
   */
  default void runTick(final Player player) {}

  /**
   * インベントリー上でアイテムをクリックしました
   *
   * @param e event
   */
  default void clickItem(final InventoryClickEvent e) {}

  /**
   * このアイテムがスポーンした際に呼ばれます
   *
   * @param e event
   */
  default void itemSpawn(final ItemSpawnEvent e) {}

  /**
   * このアイテムをメインハンドに持ってブロックを破壊したさいに呼ばれます
   *
   * @param e event
   */
  default void blockBreak(final BlockBreakEvent e) {}

  /**
   * このアイテムからほかのにswapした時の処理
   *
   * @param e event
   */
  default void heldOfOther(final PlayerItemHeldEvent e) {}

  /**
   * このアイテムをもちsneakをするときの処理
   *
   * @param e event
   * @parmam equipmentSlot 要求される装備スロット (null なら inventory です)
   */
  default void shiftItem(
      final PlayerToggleSneakEvent e, final @Nullable EquipmentSlot equipmentSlot) {}

  /**
   * このアイテムを捨てたさいの処理
   *
   * @param e event
   */
  default void dropItem(final PlayerDropItemEvent e) {}

  /**
   * マインハンドにアイテムを切り替えたら発動 先にオフハンドが呼び出されます、キャンセルされたらこれは呼び出されません
   *
   * @param e event
   */
  default void swapToMainHand(final PlayerSwapHandItemsEvent e) {}

  /**
   * オフハンドに切り替えたら発動 先にこれが呼び出されキャンセルたらメインハンドは呼び出されません
   *
   * @param e event
   */
  default void swapToOffHand(final PlayerSwapHandItemsEvent e) {}

  /**
   * デフォルトではeventはキャンセルされますが {@code e.setCancelled(false)} をすることでキャンセルするのを防げます
   *
   * @param e event
   */
  default void interact(final PlayerInteractEvent e) {}

  /**
   * ほかのアイテムからこのカスタムアイテムにswapした時の処理
   *
   * @param e event
   */
  default void heldOfThis(final PlayerItemHeldEvent e) {}

  /**
   * このアイテムで projectile を launch したときに呼ばれます
   *
   * @param e
   */
  default void projectileLaunch(ProjectileLaunchEvent e, Player player) {}

  default void itemUse() {
    getItem().setAmount(getItem().getAmount() - 1);
  }
}
