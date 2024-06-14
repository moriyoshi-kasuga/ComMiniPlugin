package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/** クリックするとインベントリを閉じるMenuButtonです。 */
public class CloseButton<P extends Plugin> extends ItemButton<MenuHolder<P>> {

  /** オークドアのアイコンと表示名 "Close "を持つクローズボタンを作成します。 */
  public CloseButton() {
    this(Material.OAK_DOOR);
  }

  /**
   * カスタム素材を使用し、表示名を「閉じる」とした閉じるボタンを作成します。
   *
   * @param material the icon material
   */
  public CloseButton(Material material) {
    this(material, "Close");
  }

  /**
   * カスタムアイコンを持つクローズボタンを作成します。
   *
   * @param icon the icon
   */
  public CloseButton(ItemStack icon) {
    super(icon);
  }

  /**
   * カスタムマテリアルとカスタム表示名を持つクローズボタンを作成します。
   *
   * @param material the icon material
   * @param displayName the display name
   */
  public CloseButton(Material material, String displayName) {
    super(new ItemBuilder(material).name(displayName).build());
  }

  /**
   * 1ティック後にインベントリを閉じます。
   *
   * @param holder the MenuHolder
   * @param event the InventoryClickEvent
   */
  @Override
  public final void onClick(@NotNull MenuHolder<P> holder, @NotNull InventoryClickEvent event) {
    event.getView().close();
  }
}
