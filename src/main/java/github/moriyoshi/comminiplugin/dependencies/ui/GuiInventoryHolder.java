package github.moriyoshi.comminiplugin.dependencies.ui;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * GUI用のInventoryHolderです。
 * <p>
 * このクラスは、独自のプラグインで拡張することを想定しています。 ただ、{@link #onClick(InventoryClickEvent)},
 * {@link #onDrag(InventoryDragEvent)} をオーバーライドします。 {@link #onOpen(InventoryOpenEvent)} または
 * {@link #onClose(InventoryCloseEvent)} メソッド。
 * InventoryClickEventとInventoryDragEventは、デフォルトでキャンセルされるように設定されています。
 * しかし、サブクラスで{@code event.setCancelled(false)}を使用すれば、問題なくキャンセル解除が可能です。
 * <p>
 * ボタン付きのメニューが必要なだけなら、{@link MenuHolder} です。 を、お客様のニーズに合わせて適材適所でご提案します。
 *
 * @param <P> your Plugin type
 * @see MenuHolder
 */
public abstract class GuiInventoryHolder<P extends Plugin> implements InventoryHolder {

  protected final GuiListener guiListener;
  private final Inventory inventory;
  private final P plugin;

  /**
   * 与えられたインベントリタイプとタイトルを持つプラグイン用の新しいGuiInventoryHolderを構築します。
   *
   * @param plugin your plugin
   * @param type   the inventory type
   * @param title  the title {@link #onClick(InventoryClickEvent)} and
   *               {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(P plugin, InventoryType type, String title) {
    this(GuiListener.getInstance(), plugin, type, title);
  }

  /**
   * 与えられたインベントリタイプとタイトルを持つプラグイン用の新しいGuiInventoryHolderを構築します。
   *
   * @param plugin      your plugin
   * @param type        the inventory type
   * @param title       the title
   * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
   *                    {@link #onClick(InventoryClickEvent)} and
   *                    {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(GuiListener guiListener, P plugin, InventoryType type, String title) {
    this.guiListener = guiListener;
    this.plugin = plugin;
    this.inventory = plugin.getServer()
        .createInventory(this, type, Util.mm(title)); // implicit null check

    guiListener.registerGui(this, inventory); // implicit null check
  }

  /**
   * 与えられたサイズとタイトルで、プラグイン用の新しいGuiInventoryHolderを構築します。
   *
   * @param plugin your plugin
   * @param title  the title
   * @param size   the chest size (should be a multiple of 9 and ranging from 9 to 54)
   *               {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)}
   *               methods
   */
  public GuiInventoryHolder(P plugin, int size, String title) {
    this(GuiListener.getInstance(), plugin, size, title);
  }

  /**
   * 指定されたサイズとタイトルで、プラグイン用の新しい chest-GuiInventoryHolder を構築します。
   *
   * @param plugin      your plugin
   * @param size        the chest size (should be a multiple of 9 and ranging from 9 to 54)
   * @param title       the title
   * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
   *                    {@link #onClick(InventoryClickEvent)} and
   *                    {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(GuiListener guiListener, P plugin, int size, String title) {
    this.guiListener = guiListener;
    this.plugin = plugin;
    this.inventory = plugin.getServer()
        .createInventory(this, size, Util.mm(title)); // implicit null check

    guiListener.registerGui(this, inventory); // implicit null check
  }

  /**
   * 与えられたインベントリタイプでプラグイン用の新しいGuiInventoryHolderを構築します。
   *
   * @param plugin your plugin
   * @param type   the inventory type {@link #onClick(InventoryClickEvent)} and
   *               {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(P plugin, InventoryType type) {
    this(GuiListener.getInstance(), plugin, type);
  }

  /**
   * 与えられたインベントリタイプでプラグイン用の新しいGuiInventoryHolderを構築します。
   *
   * @param plugin      your plugin
   * @param type        the inventory type
   * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
   *                    {@link #onClick(InventoryClickEvent)} and
   *                    {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(GuiListener guiListener, P plugin, InventoryType type) {
    this.guiListener = guiListener;
    this.plugin = plugin;
    this.inventory = plugin.getServer().createInventory(this, type); // implicit null check

    guiListener.registerGui(this, inventory); // implicit null check
  }

  /**
   * 指定されたサイズのプラグイン用の新しいchest-GuiInventoryHolderを構築します。
   *
   * @param plugin your plugin
   * @param size   the chest size (should be a multiple of 9 and ranging from 9 to 54)
   *               {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)}
   *               methods
   */
  public GuiInventoryHolder(P plugin, int size) {
    this(GuiListener.getInstance(), plugin, size);
  }

  /**
   * 指定されたサイズのプラグイン用の新しいchest-GuiInventoryHolderを構築します。
   *
   * @param plugin      your plugin
   * @param size        the chest size (should be a multiple of 9 and ranging from 9 to 54)
   * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
   *                    {@link #onClick(InventoryClickEvent)} and
   *                    {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(GuiListener guiListener, P plugin, int size) {
    this.guiListener = guiListener;
    this.plugin = plugin;
    this.inventory = plugin.getServer().createInventory(this, size); // implicit null check

    guiListener.registerGui(this, inventory); // implicit null check
  }

  /**
   * 与えられたインベントリーを使用して、プラグインのGuiInventoryHolderを構築します。
   * これは、プラグインでOBCやNMSクラスを使用していて、インベントリが作成できない場合に特に便利です。
   * {@link org.bukkit.Server#createInventory(InventoryHolder, InventoryType,
   * net.kyori.adventure.text.Component)}やそのオーバーロードによるものです。
   * これを行いたい理由の1つは、独自のContainer実装でカスタムシフトクリックの動作を実装するためです。
   * <p>
   * 引数として与えられたインベントリのInventoryHolderは、この新しいGuiInventoryHolderでなければなりません。 A code example:
   *
   * <pre><code>
   * public class MyNMSInventory extends InventorySubContainer implements ITileEntityContainer {
   *
   *     private final MyBukkitWrapper bukkitInventory;
   *
   *     public MyNMSInventory(MyPlugin plugin, String title) {
   *         super(title, true, 6*9);
   *         this.bukkitInventory = new MyBukkitWrapper(this);
   *         this.bukkitOwner = new MyGuiInventoryHolder(plugin, this);
   *     }
   *
   *     {@literal @}Override
   *     public Container createContainer(PlayerInventory playerInventory, EntityHuman human) {
   *         EntityPlayer player = (EntityPlayer) human;
   *         return new MyContainer(player.nextContainerCounter(), player.getBukkitEntity(), bukkitInventory);
   *     }
   *
   *     {@literal @}Override
   *     public String getContainerName() {
   *          return "minecraft:container";
   *     }
   * }
   * </code></pre>
   *
   * @param plugin    your plugin
   * @param inventory the custom inventory {@link #onClick(InventoryClickEvent)} and
   *                  {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(P plugin, Inventory inventory) {
    this(GuiListener.getInstance(), plugin, inventory);
  }

  /**
   * 与えられたインベントリーを使用して、プラグインのGuiInventoryHolderを構築します。
   * これは、プラグインでOBCやNMSクラスを使用していて、インベントリが作成できない場合に特に便利です。
   * {@link org.bukkit.Server#createInventory(InventoryHolder, InventoryType,
   * net.kyori.adventure.text.Component)}やそのオーバーロードによるものです。
   * これを行いたい理由の1つは、独自のContainer実装でカスタムシフトクリックの動作を実装するためです。
   * <p>
   * 引数として与えられたインベントリのInventoryHolderは、この新しいGuiInventoryHolderでなければなりません。 A code example:
   *
   * <pre><code>
   * public class MyNMSInventory extends InventorySubContainer implements ITileEntityContainer {
   *
   *     private final MyBukkitWrapper bukkitInventory;
   *
   *     public MyNMSInventory(MyPlugin plugin, String title) {
   *         super(title, true, 6*9);
   *         this.bukkitInventory = new MyBukkitWrapper(this);
   *         this.bukkitOwner = new MyGuiInventoryHolder(plugin, this);
   *     }
   *
   *     {@literal @}Override
   *     public Container createContainer(PlayerInventory playerInventory, EntityHuman human) {
   *         EntityPlayer player = (EntityPlayer) human;
   *         return new MyContainer(player.nextContainerCounter(), player.getBukkitEntity(), bukkitInventory);
   *     }
   *
   *     {@literal @}Override
   *     public String getContainerName() {
   *          return "minecraft:container";
   *     }
   * }
   * </code></pre>
   *
   * @param plugin      your plugin
   * @param inventory   the custom inventory
   * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
   *                    {@link #onClick(InventoryClickEvent)} and
   *                    {@link #onClose(InventoryCloseEvent)} methods
   */
  public GuiInventoryHolder(GuiListener guiListener, P plugin, Inventory inventory) {
    this.guiListener = guiListener;
    this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
    this.inventory = Objects.requireNonNull(inventory, "Inventory cannot be null");

    guiListener.registerGui(this, inventory); // implicit null check
  }

  /**
   * イベント内でクリックされたインベントリを取得します。
   *
   * @param event the InventoryClickEvent
   * @return クリックされたインベントリ、またはインベントリ以外をクリックした場合はNULLを指定します。
   */
  protected static Inventory getClickedInventory(InventoryClickEvent event) {
    return getClickedInventory(event.getRawSlot(), event.getView());
  }

  /**
   * InventoryClickEvent を必要としない {@link #getClickedInventory(InventoryClickEvent)} のオーバーロードです。
   *
   * @param rawSlot the slot that was clicked
   * @param view    the view in which there was a click
   * @return クリックされたインベントリ、またはプレイヤーが両方のインベントリの外側をクリックした場合はnull
   */
  protected static Inventory getClickedInventory(int rawSlot, InventoryView view) {
    // Adopted from the spigot-api patches
    // See
    // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/Bukkit-Patches/0010-InventoryClickEvent-getClickedInventory.patch
    if (rawSlot < 0) {
      return null;
    } else {
      Inventory topInventory = view.getTopInventory();
      // apparently it is possible that the top inventory is null.
      // does this happen when a player opens his/her own inventory?
      if (rawSlot < topInventory.getSize()) {
        return topInventory;
      } else {
        return view.getBottomInventory();
      }
    }
  }

  /**
   * このGUIホルダーに関連するインベントリーを取得します。 このメソッドをオーバーライドするサブクラスは、常に次のメソッドを返す必要があります。
   * {@code super.getInventory();}をカスタムインベントリタイプにキャストしています。
   *
   * @return the inventory
   */
  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }

  /**
   * このGUIホルダーに関連するプラグインを取得します。
   *
   * @return the plugin
   */
  public final P getPlugin() {
    return plugin;
  }

  /**
   * 対応するInventoryViewが閉じるときに呼び出されます。
   * <p>
   * このメソッドは、サブクラスによってオーバーライドされることを意図しています。
   *
   * @param event the inventory close event
   */
  public void onClose(InventoryCloseEvent event) {
  }

  /**
   * 対応するInventoryViewがクリックされたときに呼び出されます。 この方法は、どのインベントリがクリックされたかを保証するものではありません。
   * またはインベントリが全くクリックされなかったかどうか。
   * <p>
   * デフォルトでは、イベントはキャンセルされます。
   * <p>
   * このメソッドは、サブクラスによってオーバーライドされることを意図しています。
   *
   * @param event the inventory click event
   */
  public void onClick(InventoryClickEvent event) {
  }

  /**
   * 対応するInventoryViewが開くときに呼び出される。
   * <p>
   * このメソッドは、サブクラスによってオーバーライドされることを意図しています。
   *
   * @param event the inventory open event
   */
  public void onOpen(InventoryOpenEvent event) {
  }

  /**
   * 対応するInventoryViewでアイテムがドラッグされたときに呼び出される。 この方法は、どのインベントリにドラッグされたかを保証するものではありません。
   *
   * @param event the inventory drag event
   */
  public void onDrag(InventoryDragEvent event) {
  }

  /**
   * このinventoryを開く
   *
   * @param entity 開く対象
   */
  public void openInv(HumanEntity entity) {
    entity.openInventory(getInventory());
  }

  /**
   * このinventoryを複数の対象に開く
   *
   * @param entities 開く対象達
   */
  public void openInv(HumanEntity... entities) {
    for (HumanEntity e : entities) {
      e.openInventory(getInventory());
    }
  }
}
