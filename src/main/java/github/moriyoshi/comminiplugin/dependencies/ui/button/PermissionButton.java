package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * ボタンをクリックしたプレイヤーに権限があるときのみ動作するボタン
 *
 * @param <MH> the menu type
 */
public class PermissionButton<MH extends MenuHolder<?>> extends PredicateButton<MH> {

  /**
   * -- GETTER -- このボタンの使用許可を取得します
   */
  @Getter
  private final String permission;
  private final Consumer<? super HumanEntity> noPermissionCallback;

  /**
   * パーミッションボタンを作成します
   *
   * @param permission このボタンを使用するために必要なパーミッション
   * @param proxy      プレーヤーがパーミッションを持つ場合にクリックを委譲するプロキシを指定します。
   */
  public PermissionButton(String permission, MenuButton<MH> proxy) {
    this(permission, proxy, null);
  }

  /**
   * パーミッションボタンを作成します
   *
   * @param permission           このボタンを使うために必要なパーミッション
   * @param proxy                プレイヤーがパーミッションを持つときにクリックを委譲するプロキシ
   * @param noPermissionCallback プレイヤーがボタンをクリックしたがパーミッションを持たないときに実行するコールバック
   */
  public PermissionButton(String permission, MenuButton<MH> proxy,
      Consumer<? super HumanEntity> noPermissionCallback) {
    super(proxy, (menuHolder, event) -> event.getWhoClicked().hasPermission(permission));
    this.permission = Objects.requireNonNull(permission, "Permission cannot be null");
    this.noPermissionCallback = noPermissionCallback;
  }

  /**
   * オプションで、ボタンがクリックされ、インベントリ・クリッカーに権限がない場合に実行されるコールバックを取得できます。
   *
   * @return コールバックがある場合は、コールバックを含むOptionalを返します。コールバックがない場合は、空のOptionalが返される。
   */
  @Override
  protected Optional<BiConsumer<MH, InventoryClickEvent>> getPredicateFailedCallback() {
    return getNoPermissionCallback().map(
        consumer -> (menuHolder, event) -> consumer.accept(event.getWhoClicked()));
  }

  /**
   * 無許可コールバックが存在する場合は、それを取得する。
   *
   * @return コールバックがこのボタンに存在する場合は、コールバックを含むオプション、そうでない場合は空のオプション
   */
  public Optional<Consumer<? super HumanEntity>> getNoPermissionCallback() {
    return Optional.ofNullable(noPermissionCallback);
  }

}
