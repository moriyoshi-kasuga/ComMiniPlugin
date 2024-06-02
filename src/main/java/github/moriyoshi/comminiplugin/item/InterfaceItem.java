package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
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
import org.bukkit.scheduler.BukkitRunnable;
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
  @Nullable
  UUID getUniqueId();

  default Optional<Supplier<UUID>> generateUUID() {
    return Optional.of(() -> UUID.randomUUID());
  }

  /**
   * このアイテムのキーを取得します {@code #getUniqueId()} が null の場合はエラーがでます<br>
   * その時は {code {@link #getItemKey(Player)}} を使用してください
   */
  @NotNull
  default CustomItemKey getItemKey() {
    return new CustomItemKey(getIdentifier(), Objects.requireNonNull(getUniqueId()));
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
   * {@link CustomItem#heldOfThis(PlayerItemHeldEvent)} で開始してほかのアイテムに変えた場合に終了する
   * アイテムを持っている間だけ1tickごとにする処理です
   *
   * @return 処理
   */
  default Optional<BiConsumer<Player, ItemStack>> heldItem() {
    return Optional.empty();
  }

  /**
   * プレイヤーのインベントリーにあるさいに常に更新されるアイテムです
   *
   * @param player player
   */
  void runTick(final Player player);

  /**
   * インベントリー上でアイテムをクリックしました
   *
   * @param e event
   */
  void clickItem(final InventoryClickEvent e);

  /**
   * このアイテムがスポーンした際に呼ばれます
   *
   * @param e event
   */
  void itemSpawn(final ItemSpawnEvent e);

  /**
   * このアイテムをメインハンドに持ってブロックを破壊したさいに呼ばれます
   *
   * @param e event
   */
  void blockBreak(final BlockBreakEvent e);

  /**
   * このアイテムからほかのにswapした時の処理
   *
   * @param e event
   */
  void heldOfOther(final PlayerItemHeldEvent e);

  /**
   * このアイテムをもちsneakをするときの処理
   *
   * @param e event
   * @parmam equipmentSlot 要求される装備スロット (null なら inventory です)
   */
  void shiftItem(final PlayerToggleSneakEvent e, final @Nullable EquipmentSlot equipmentSlot);

  /**
   * このアイテムを捨てたさいの処理
   *
   * @param e event
   */
  void dropItem(final PlayerDropItemEvent e);

  /**
   * マインハンドにアイテムを切り替えたら発動 先にオフハンドが呼び出されます、キャンセルされたらこれは呼び出されません
   *
   * @param e event
   */
  void swapToMainHand(final PlayerSwapHandItemsEvent e);

  /**
   * オフハンドに切り替えたら発動 先にこれが呼び出されキャンセルたらメインハンドは呼び出されません
   *
   * @param e event
   */
  void swapToOffHand(final PlayerSwapHandItemsEvent e);

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

  default void itemUse() {
    getItem().setAmount(getItem().getAmount() - 1);
  }

  /**
   * デフォルトではeventはキャンセルされますが {@code e.setCancelled(false)} をすることでキャンセルするのを防げます
   *
   * @param e event
   */
  void interact(final PlayerInteractEvent e);

  /**
   * ほかのアイテムからこのカスタムアイテムにswapした時の処理
   *
   * @param e event
   */
  default void heldOfThis(final PlayerItemHeldEvent e) {
    val player = e.getPlayer();
    val v = this;
    val opt = heldItem();
    if (opt.isPresent()) {
      val consumer = opt.get();
      new BukkitRunnable() {

        @Override
        public void run() {
          val item = player.getInventory().getItemInMainHand();
          if (CustomItem.isCustomItem(item) && v.equals(CustomItem.getCustomItem(item))) {
            consumer.accept(player, item);
            return;
          }
          this.cancel();
        }
      }.runTaskTimer(ComMiniPlugin.getPlugin(), 0L, 1L);
    }
  }

  /**
   * このアイテムで projectile を launch したときに呼ばれます
   *
   * @param e
   */
  void projectileLaunch(ProjectileLaunchEvent e, Player player);
}
