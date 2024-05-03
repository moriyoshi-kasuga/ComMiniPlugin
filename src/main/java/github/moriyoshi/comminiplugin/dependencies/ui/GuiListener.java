package github.moriyoshi.comminiplugin.dependencies.ui;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUIが開かれ、クリックされ、閉じられたときにリスニングを行うリスナーです。
 */
public class GuiListener implements Listener {

  private static final GuiListener INSTANCE = new GuiListener();

  private final WeakHashMap<Inventory, WeakReference<GuiInventoryHolder<?>>> guiInventories = new WeakHashMap<>();

  private GuiListener() {
  }

  /**
   * GuiListenerを取得します。
   *
   * @return GuiListener の singleton instance
   */
  public static GuiListener getInstance() {
    return INSTANCE;
  }

  // ===== 登録 =====

  /**
   * インベントリーGUIを登録する。
   *
   * @param holder    the gui holder
   * @param inventory gui アイテムスタックを保持するインベントリ
   * @return guiが正常に登録された場合はtrue、そうでない場合はfalseとなります。
   */
  public boolean registerGui(GuiInventoryHolder<?> holder, Inventory inventory) {
    if (holder == inventory.getHolder()) {
      return true; // yes, reference equality
    }

    return guiInventories.putIfAbsent(inventory, new WeakReference<>(holder)) == null;
  }

  /**
   * guiのインベントリの{@link Inventory#getHolder()}の代用となる。
   *
   * @param inventory the inventory
   * @return ホルダー - またはインベントリにホルダーが登録されていない場合は null。
   */
  public GuiInventoryHolder<?> getHolder(Inventory inventory) {
    InventoryHolder holder = inventory.getHolder();
    if (holder instanceof GuiInventoryHolder) {
      return (GuiInventoryHolder<?>) holder;
    }

    WeakReference<GuiInventoryHolder<?>> reference = guiInventories.get(inventory);
    if (reference == null) {
      return null;
    }

    return reference.get(); // can still be null
  }

  /**
   * インベントリがguiインベントリホルダーに登録されているかどうかをチェックします。
   *
   * @param inventory ホルダーに登録されるかもしれないインベントリー
   * @param holder    確認するホルダー
   * @return ホルダーとインベントリーが登録されているかどうか
   */
  public boolean isGuiRegistered(GuiInventoryHolder<?> holder, Inventory inventory) {
    return getHolder(inventory) == holder; // そうです、参照等価です。
  }

  /**
   * このインベントリに登録されているguiがあるかどうかをチェックします。
   *
   * @param inventory もしかしたら登録されるかもしれない在庫
   * @return 与えられたインベントリに対して {@link GuiInventoryHolder} が登録されている場合、true を返します。
   */
  public boolean isGuiRegistered(Inventory inventory) {
    return getHolder(inventory) != null;
  }

  // unregisterGuiメソッドを作ると、InventoryからGuiInventoryHolderを外す必要があるため、絶対に作れません。
  // なので、bukkitのapiにそのようなメソッドが追加されるまでは、nms/reflectionのハックをしないと不可能です。

  // ===== event stuff =====

  @SuppressWarnings("rawtypes")
  private void onGuiInventoryEvent(InventoryEvent event, Consumer<GuiInventoryHolder> action) {
    GuiInventoryHolder<?> guiHolder = getHolder(event.getInventory());

    if (guiHolder != null && guiHolder.getPlugin().isEnabled()) {
      action.accept(guiHolder);
    }
  }

  /**
   * トップインベントリがGuiによって保持され、イベントがキャンセルされない場合、InventoryOpenEventを{@link GuiInventoryHolder}に委譲します。
   *
   * @param event the InventoryOpenEvent
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryOpen(InventoryOpenEvent event) {
    onGuiInventoryEvent(event, gui -> gui.onOpen(event));
  }

  /**
   * トップインベントリがGuiによって保持され、イベントがキャンセルされない場合、InventoryClickEventを{@link GuiInventoryHolder}に委譲します。
   * InventoryClickEventは、Guiに渡される前にキャンセルされます。
   *
   * @param event the InventoryClickEvent
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    onGuiInventoryEvent(event, gui -> {
      event.setCancelled(true);
      gui.onClick(event);
    });
  }

  /**
   * トップインベントリがGuiによって保持され、イベントがキャンセルされない場合、InventoryDragEventを{@link GuiInventoryHolder}に委譲します。
   * InventoryDragEventは、Guiに渡される前にキャンセルされます。
   *
   * @param event the InventoryDragEvent
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    onGuiInventoryEvent(event, gui -> {
      event.setCancelled(true);
      gui.onDrag(event);
    });
  }

  /**
   * トップインベントリがGuiによって保持されている場合、InventoryCloseEventを{@link GuiInventoryHolder}に委譲します。
   *
   * @param event InventoryCloseEvent
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onInventoryClose(InventoryCloseEvent event) {
    onGuiInventoryEvent(event, gui -> gui.onClose(event));
  }

}
