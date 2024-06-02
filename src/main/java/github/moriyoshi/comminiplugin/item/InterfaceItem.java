package github.moriyoshi.comminiplugin.item;

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
   * {@link CustomItem#heldOfThis(PlayerItemHeldEvent)} で開始してほかのアイテムに変えた場合に終了する
   * アイテムを持っている間だけ1tickごとにする処理です
   *
   * @return 処理
   */
  @NotNull
  default Optional<Consumer<Player>> heldItem(final ItemStack item) {
    return Optional.empty();
  }

  /**
   * プレイヤーのインベントリーにあるさいに常に更新されるアイテムです
   *
   * @param player player
   */
  default void runTick(final Player player) {}

  /**
   * プレイヤーのインベントリーにあるさいに一秒ごとに更新されます
   *
   * @param player player
   */
  default void runSecond(final Player player) {}
}
