package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * クリックしたプレイヤーにチャットでメッセージを言わせるボタンです。
 *
 * @param <MH> the menu holder type
 */
@Getter
public class ChatButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  /**
   * -- GETTER -- Get the chat message.
   */
  private String message;

  /**
   * 定数でないチャットメッセージを使用したい ChatButtons のためのプロテクテッドコンストラクタです。
   * このコンストラクタを使用するサブクラスは、{@link #getMessage()}または{@link #getMessage(MenuHolder,
   * InventoryClickEvent)}のいずれかをオーバーライドしなければならない。
   *
   * @param icon the icon
   */
  protected ChatButton(ItemStack icon) {
    super(icon);
  }

  /**
   * ChatButtonを作成します。
   *
   * @param icon    the icon
   * @param message the chat message
   */
  public ChatButton(ItemStack icon, String message) {
    super(icon);
    setMessage(message);
  }

  /**
   * ChatButtonを作成します。アイコンの表示名は、メッセージに設定されます。
   *
   * @param material the icon material
   * @param message  the chat message
   */
  public ChatButton(Material material, String message) {
    this(new ItemBuilder(material).name(message).build(), message);
  }

  /**
   * ボタンをクリックしたプレイヤーに、チャットでメッセージを言わせる。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      player.chat(getMessage(menuHolder, event));
    }
  }

  /**
   * {@link #onClick(MenuHolder, InventoryClickEvent)}によって送信されるチャットメッセージを計算します。
   * サブクラスは、一定でないチャットメッセージのためにこのメソッドをオーバーライドできます。 デフォルトの実装では、{@link #getMessage()}に委譲されます。
   *
   * @param menuHolder the menu holder
   * @param event      the InventoryClickEvent
   * @return the customized message
   */
  protected String getMessage(MH menuHolder, InventoryClickEvent event) {
    return getMessage();
  }

  /**
   * チャットメッセージを設定します。
   *
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = Objects.requireNonNull(message, "Message cannot be null");
  }

}
