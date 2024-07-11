package github.moriyoshi.comminiplugin.lib.item;

import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.entity.Player;
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

  /**
   * これを true にするとスタックできます、しかし内部でuuidを固定にしているので、<br>
   * {@code #getItemKey()} を使った処理が使えなくなります
   *
   * @return can stack
   */
  default boolean canStack() {
    return false;
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
  default IdentifierKey getItemKey() {
    return new IdentifierKey(getIdentifier(), getUniqueId());
  }

  /**
   * {@code #getUniqueId()} が null の場合 こちらを使ってください <br>
   * プレイヤーで共有のアイテムのキーを作成したい場合に使ってください
   *
   * @param player get by palyer's uuid
   */
  @NotNull
  default IdentifierKey getItemKey(final Player player) {
    return new IdentifierKey(getIdentifier(), player.getUniqueId());
  }

  /**
   * {@code #getUniqueId()} が null の場合 こちらを使ってください <br>
   * プレイヤーで共有のアイテムのキーを作成したい場合に使ってください
   *
   * @param uuid uuid
   */
  @NotNull
  default IdentifierKey getItemKey(@NonNull final UUID uuid) {
    return new IdentifierKey(getIdentifier(), uuid);
  }

  /**
   * このカスタムアイテムのインスタンスで使用しているアイテムを返します
   *
   * @return itemstack
   */
  @NotNull
  ItemStack getItem();

  default void useItemAmount() {
    getItem().setAmount(getItem().getAmount() - 1);
  }
}
