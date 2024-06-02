package github.moriyoshi.comminiplugin.item;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
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
  @Nullable
  UUID getUniqueId();

  /**
   * このアイテムのキーを取得します {@code #getUniqueId()} が null の場合はエラーがでます<br>
   * その時は {code {@link #getItemKey(Player)}} を使用してください
   *
   * @return
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
   * @return
   */
  @NotNull
  default CustomItemKey getItemKey(Player player) {
    return new CustomItemKey(getIdentifier(), player.getUniqueId());
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
