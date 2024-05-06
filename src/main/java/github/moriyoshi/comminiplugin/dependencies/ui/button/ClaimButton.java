package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * クリックすると、アイテムスタックをプレイヤーのインベントリに転送するボタンを表します。
 */
public class ClaimButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  private final PlayerInventoryFullCallback<MH> inventoryFullCallback;
  private final SuccessFulTransferCallback<MH> successFulTransferCallback;
  private final boolean copy;

  /**
   * Creates the ClaimButton.
   *
   * @param item プレーヤーが請求できるアイテム
   */
  public ClaimButton(ItemStack item) {
    this(item, false, null, null);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item プレーヤーが請求できるアイテム
   * @param copy アイテムのコピーをトップインベントリに残すかどうか
   */
  public ClaimButton(ItemStack item, boolean copy) {
    this(item, copy, null, null);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item                  プレーヤーが請求できるアイテム
   * @param inventoryFullCallback アイテムがプレイヤーのインベントリに移動できなかった場合に呼び出されるコールバック - nullも可
   */
  public ClaimButton(ItemStack item, PlayerInventoryFullCallback<MH> inventoryFullCallback) {
    this(item, false, inventoryFullCallback, null);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param copy                  アイテムのコピーをトップインベントリに残すかどうか
   * @param item                  プレーヤーが請求できるアイテム
   * @param inventoryFullCallback アイテムがプレイヤーのインベントリに移動できなかった場合に呼び出されるコールバック - nullも可
   */
  public ClaimButton(ItemStack item, boolean copy,
      PlayerInventoryFullCallback<MH> inventoryFullCallback) {
    this(item, copy, inventoryFullCallback, null);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item                       プレーヤーが請求できるアイテム
   * @param successFulTransferCallback アイテムが正常に転送された後に呼び出されるコールバック - nullも可能です。
   */
  public ClaimButton(ItemStack item, SuccessFulTransferCallback<MH> successFulTransferCallback) {
    this(item, false, null, successFulTransferCallback);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item                       プレーヤーが請求できるアイテム
   * @param copy                       アイテムのコピーをトップインベントリに残すかどうか
   * @param successFulTransferCallback アイテムが正常に転送された後に呼び出されるコールバック - nullも可能です。
   */
  public ClaimButton(ItemStack item, boolean copy,
      SuccessFulTransferCallback<MH> successFulTransferCallback) {
    this(item, copy, null, successFulTransferCallback);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item                       プレーヤーが請求できるアイテム
   * @param inventoryFullCallback      アイテムがプレイヤーのインベントリに移動できなかった場合に呼び出されるコールバック - nullも可
   * @param successFulTransferCallback アイテムが正常に転送された後に呼び出されるコールバック - nullも可能です。
   */
  public ClaimButton(ItemStack item, PlayerInventoryFullCallback<MH> inventoryFullCallback,
      SuccessFulTransferCallback<MH> successFulTransferCallback) {
    this(item, false, inventoryFullCallback, successFulTransferCallback);
  }

  /**
   * Creates the ClaimButton.
   *
   * @param item                       プレーヤーが請求できるアイテム
   * @param copy                       アイテムのコピーをトップインベントリに残すかどうか
   * @param inventoryFullCallback      アイテムがプレイヤーのインベントリに移動できなかった場合に呼び出されるコールバック - nullも可
   * @param successFulTransferCallback アイテムが正常に転送された後に呼び出されるコールバック - nullも可能です。
   */
  public ClaimButton(ItemStack item, boolean copy,
      PlayerInventoryFullCallback<MH> inventoryFullCallback,
      SuccessFulTransferCallback<MH> successFulTransferCallback) {
    super(item);
    this.copy = copy;
    this.inventoryFullCallback = inventoryFullCallback;
    this.successFulTransferCallback = successFulTransferCallback;
  }

  /**
   * アイテムをプレイヤーのインベントリに移動させようとします。移動に成功した場合、メニューからボタンが削除されます。
   *
   * @param menuHolder このボタンが含まれるメニュー
   * @param event      クリックイベント
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    ItemStack clickedItem = event.getCurrentItem();
    HumanEntity player = event.getWhoClicked();

    boolean success = clickedItem == null || player.getInventory().addItem(clickedItem).isEmpty();
    if (success) {
      if (!copy) {
        event.setCurrentItem(null);
        menuHolder.unsetButton(event.getSlot());
      }
      getSuccessFulTransferCallback().ifPresent(
          (Consumer<SuccessFulTransferCallback<MH>>) callback -> callback.afterTransfer(menuHolder,
              event, clickedItem
          ));
    } else {
      getInventoryFullCallback().ifPresent(
          (Consumer<PlayerInventoryFullCallback<MH>>) callback -> callback.onPlayerInventoryFull(
              menuHolder, event));
    }
  }

  /**
   * アイテムをプレイヤーのインベントリに移動できない場合に呼び出されるコールバックを取得します。
   *
   * @return コールバックを含むOptional、コールバックが存在しない場合は空のOptional。
   */
  public Optional<? extends PlayerInventoryFullCallback<MH>> getInventoryFullCallback() {
    return Optional.ofNullable(inventoryFullCallback);
  }

  /**
   * アイテムがプレイヤーのインベントリに正常に転送された後に呼び出されるコールバックを取得します。
   *
   * @return コールバックを含むOptional、コールバックが存在しない場合は空のOptional。
   */
  public Optional<? extends SuccessFulTransferCallback<MH>> getSuccessFulTransferCallback() {
    return Optional.ofNullable(successFulTransferCallback);
  }

  /**
   * プレイヤーのインベントリが{@linkplain ClaimButton}のアイテムを受け取れない場合に、アクションを起こすために使用できるコールバックです。
   *
   * @param <MH> メニューホルダータイプ
   */
  @FunctionalInterface
  public interface PlayerInventoryFullCallback<MH extends MenuHolder<?>> extends
      BiConsumer<MH, InventoryClickEvent> {

    /**
     * ボタンがクリックされたが、アイテムが満杯でプレイヤーのインベントリに移動できない場合に実行される機能的なメソッドです。
     *
     * @param menuHolder the menu holder
     * @param event      the click event
     */
    void onPlayerInventoryFull(MH menuHolder, InventoryClickEvent event);

    /**
     * {@linkplain BiConsumer}が必要な場所で{@linkplain PlayerInventoryFullCallback}を動作させるための便利なメソッドです。 The
     * default implementation delegates to
     * {@link #onPlayerInventoryFull(MenuHolder, InventoryClickEvent)}.
     *
     * @param menuHolder the menu holder
     * @param event      the inventory click event
     */
    default void accept(MH menuHolder, InventoryClickEvent event) {
      onPlayerInventoryFull(menuHolder, event);
    }

  }

  /**
   * ItemStackが正常に転送された後、追加のアクションを実行するために使用できるコールバックです。
   *
   * @param <MH> the menu holder type
   */
  @FunctionalInterface
  public interface SuccessFulTransferCallback<MH extends MenuHolder<?>> {

    /**
     * ボタンがクリックされ、ItemStackがプレイヤーのインベントリに転送された後に実行される機能的なメソッドです。
     *
     * @param menuHolder the menu holder
     * @param event      the inventory click event
     * @param reward     プレイヤーのインベントリに転送された報酬
     */
    void afterTransfer(MH menuHolder, InventoryClickEvent event, ItemStack reward);

  }

}
