package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.Util;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

  @NotNull
  default CustomItemKey getItemKey() {
    return new CustomItemKey(getIdentifier(), getUniqueId());
  }

  /**
   * このカスタムアイテムのインスタンスで使用しているアイテムを返します
   *
   * @return itemstack
   */
  @NotNull
  ItemStack getItem();

  /**
   * {@link interact} を無効にするかです。 別にe.setCanceldで上書きできます
   *
   * @return is cancel
   */
  default boolean isInteractCancel() {
    return true;
  }

  /**
   * {@link #heldItem()} 中にアクションバーにメッセージを送る文字列を返します {@link Optional#empty()} の場合はメッセージを送りません
   *
   * @param player プレイヤーです
   * @return 文字列のoptionalです
   */
  @NotNull
  Optional<String> getActionBarMessage(Player player);

  /**
   * {@link CustomItem#heldOfThis(PlayerItemHeldEvent)} で開始してほかのアイテムに変えた場合に終了する
   * アイテムを持っている間だけ1tickごとにする処理です
   *
   * @return 処理
   */
  @NotNull
  default Optional<Consumer<Player>> heldItem() {
    return Optional.of(player -> {
      getActionBarMessage(player).ifPresent(s -> player.sendActionBar(Util.mm(s)));
    });
  }

  /**
   * プレイヤーのインベントリーにあるさいに常に更新されるアイテムです
   *
   * @param player player
   */
  @NotNull
  default void runTick(Player player) {
  }

  /**
   * プレイヤーのインベントリーにあるさいに一秒ごとに更新されます
   *
   * @param player player
   */
  @NotNull
  default void runSecond(Player player) {
  }

}
