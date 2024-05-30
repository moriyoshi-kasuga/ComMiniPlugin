package github.moriyoshi.comminiplugin.dependencies.ui.button;

import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import lombok.NonNull;

/**
 * クリックすると、プレイヤーをある場所にテレポートさせるボタンです。
 *
 * @param <MH> the menu holder type
 */
public class TeleportButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  private Location location;

  private TeleportCause cause;
  /**
   * TeleportButtonを作成します。
   *
   * @param icon the icon
   * @param to   プレイヤーがテレポートする場所を指定します。
   */
  public TeleportButton(ItemStack icon, Location to) {
    super(icon);
    setTo(to);
    setCause(TeleportCause.PLUGIN);
  }

  public TeleportButton(ItemStack icon, Location to, TeleportCause cause) {
    super(icon);
    setTo(to);
    setCause(cause);
  }

  /**
   * 固定された目的地を持たないTeleportButtonsのためのプロテクテッドコンストラクタです。
   * このボタンを使用するサブクラスは、{@link #getTo()}または{@link #getTo(MenuHolder,
   * InventoryClickEvent)}のいずれかをオーバーライドする必要があります。
   *
   * @param icon the icon
   */
  protected TeleportButton(ItemStack icon) {
    super(icon);
  }

  /**
   * プレイヤーをテレポートさせる。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    event.getWhoClicked().teleport(getTo(menuHolder, event), getCause());
  }

  public void setCause(@NonNull TeleportCause cause) {
    this.cause = cause;
  }

  public TeleportCause getCause() {
    return cause;
  }

  /**
   * プレイヤーのテレポート先となる場所を取得します。
   *
   * @return the location
   */
  public Location getTo() {
    return location.clone();
  }

  /**
   * このボタンがプレイヤーをテレポートさせる場所を設定します。
   *
   * @param to the destination location.
   */
  public void setTo(@NonNull Location to) {
    this.location = to.clone();
  }

  /**
   * ボタンがクリックされたときに、プレイヤーがテレポートする場所を取得します。 サブクラスはこのメソッドをオーバーライドして、定数でない位置を使用できます。
   * デフォルトの実装では、{@link #getTo()}に委譲されます。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   * @return the location to which the player will be teleported.
   */
  protected Location getTo(MH menuHolder, InventoryClickEvent event) {
    return getTo();
  }

}
