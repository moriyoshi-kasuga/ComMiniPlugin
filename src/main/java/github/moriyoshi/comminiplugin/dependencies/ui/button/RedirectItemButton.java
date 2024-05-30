package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 別メニューにリダイレクトするボタンで、アイコンが付いているもの。
 *
 * @param <MH> the type of your MenuHolder
 */
public class RedirectItemButton<MH extends MenuHolder<?>> extends ItemButton<MH> implements
    RedirectButton<MH> {

  private final BiFunction<MH, InventoryClickEvent, ? extends Inventory> redirect;

  /**
   * 指定されたアイコンとリダイレクトを持つボタンを作成します。
   *
   * @param icon     the icon
   * @param redirect the redirect
   */
  public RedirectItemButton(ItemStack icon, Supplier<? extends Inventory> redirect) {
    this(icon, (menuHolder, event) -> redirect.get());
    Objects.requireNonNull(redirect, "Redirect cannot be null");
  }

  /**
   * 指定されたアイコンとリダイレクト関数を持つボタンを作成します。
   *
   * @param icon     the icon
   * @param redirect the redirect function
   */
  public RedirectItemButton(ItemStack icon,
      BiFunction<MH, InventoryClickEvent, ? extends Inventory> redirect) {
    super(icon);
    this.redirect = Objects.requireNonNull(redirect, "Redirect cannot be null");
  }

  /**
   * リダイレクトを評価する。
   *
   * @param menuHolder the MenuHolder
   * @param event      the InventoryClickEvent
   * @return the Inventory the player is redirected towards.
   */
  @Override
  public final Inventory to(MH menuHolder, InventoryClickEvent event) {
    return redirect.apply(menuHolder, event);
  }

  /**
   * リンク
   * {@link #to(MenuHolder, InventoryClickEvent)}によって供給されるインベントリにプレイヤーをリダイレクトします。
   * <p>
   * このメソッドをオーバーライドするサブクラスは、常に{@code super.onClick(menuHolder, event);}を呼び出す必要があります。
   *
   * @param menuHolder the MenuHolder
   * @param event      the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    RedirectButton.super.onClick(menuHolder, event);
  }

}
