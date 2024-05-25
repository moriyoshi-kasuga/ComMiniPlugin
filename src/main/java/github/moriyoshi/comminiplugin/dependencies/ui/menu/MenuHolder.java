package github.moriyoshi.comminiplugin.dependencies.ui.menu;

import github.moriyoshi.comminiplugin.dependencies.ui.GuiInventoryHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.GuiListener;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ChatButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ClaimButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.CloseButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.PermissionButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.TeleportButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ToggleButton;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * {@link org.bukkit.inventory.InventoryView}のトップインベントリのクリックにのみ反応するGuiInventoryHolderです。
 * <br>
 * クリックの機能は、登録された{@link MenuButton}の{@link MenuButton#onClick(MenuHolder,
 * InventoryClickEvent)}メソッドで渡されます。
 *
 * @param <P> your plugin
 * @see #setButton(int, MenuButton)
 * @see MenuButton
 * @see ItemButton
 * @see RedirectButton
 * @see ToggleButton
 * @see RedirectItemButton
 * @see CloseButton
 * @see PermissionButton
 * @see TeleportButton
 * @see ChatButton
 * @see ClaimButton
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MenuHolder<P extends Plugin> extends GuiInventoryHolder<P> implements
    Iterable<MenuButton<?>> {

  private final MenuButton<?>[] buttons;
  private final LinkedList<WeakReference<ButtonAddCallback>> addButtonCallbacks = new LinkedList<>();
  private final LinkedList<WeakReference<ButtonRemoveCallback>> removeButtonCallbacks = new LinkedList<>();
  private int buttonCount = 0;

  /**
   * 指定されたInventoryTypeとタイトルを持つMenuHolderを作成します。
   *
   * @param plugin your plugin
   * @param type   the inventory type
   * @param title  the title
   */
  public MenuHolder(P plugin, InventoryType type, String title) {
    this(GuiListener.getInstance(), plugin, type, title);
  }

  /**
   * 指定されたInventoryTypeとタイトルを持つMenuHolderを作成します。
   *
   * @param plugin      your plugin
   * @param type        the inventory type
   * @param title       the title
   * @param guiListener the gui listener that calls the onOpen, onClick and
   *                    onClose methods
   */
  public MenuHolder(GuiListener guiListener, P plugin, InventoryType type, String title) {
    super(guiListener, plugin, type, title);

    this.buttons = new MenuButton<?>[getInventory().getSize()];
  }

  /**
   * 指定されたサイズとタイトルを持つMenuHolderを作成します。
   *
   * @param plugin your plugin
   * @param size   the chest size (should be a multiple of 9 and between 9 - 54
   *               (inclusive)
   * @param title  the title
   */
  public MenuHolder(P plugin, int size, String title) {
    this(GuiListener.getInstance(), plugin, size, title);
  }

  /**
   * 指定されたサイズとタイトルを持つMenuHolderを作成します。
   *
   * @param plugin      your plugin
   * @param size        the chest size (should be a multiple of 9 and between 9 -
   *                    54 (inclusive)
   * @param title       the title
   * @param guiListener the gui listener that calls the onOpen, onClick and
   *                    onClose methods
   */
  public MenuHolder(GuiListener guiListener, P plugin, int size, String title) {
    super(guiListener, plugin, size, title);

    this.buttons = new MenuButton<?>[getInventory().getSize()];
  }

  /**
   * 指定されたInventoryTypeを持つMenuHolderを作成します。
   *
   * @param plugin your plugin
   * @param type   the inventory type
   */
  public MenuHolder(P plugin, InventoryType type) {
    this(GuiListener.getInstance(), plugin, type);
  }

  /**
   * 指定されたInventoryTypeを持つMenuHolderを作成します。
   *
   * @param plugin      your plugin
   * @param type        the inventory type
   * @param guiListener the gui listener that calls the onOpen, onClick and
   *                    onClose methods
   */
  public MenuHolder(GuiListener guiListener, P plugin, InventoryType type) {
    super(guiListener, plugin, type);

    this.buttons = new MenuButton<?>[getInventory().getSize()];
  }

  /**
   * 指定されたサイズのMenuHolderを作成します。
   *
   * @param plugin your plugin
   * @param size   the chest size (should be a multiple of 9 and between 9 - 54
   *               (inclusive)
   */
  public MenuHolder(P plugin, int size) {
    this(GuiListener.getInstance(), plugin, size);
  }

  /**
   * 指定されたサイズのMenuHolderを作成します。
   *
   * @param plugin      your plugin
   * @param size        the chest size (should be a multiple of 9 and between 9 -
   *                    54 (inclusive)
   * @param guiListener the gui listener that calls the onOpen, onClick and
   *                    onClose methods
   */
  public MenuHolder(GuiListener guiListener, P plugin, int size) {
    super(guiListener, plugin, size);

    this.buttons = new MenuButton<?>[getInventory().getSize()];
  }

  /**
   * 指定されたインベントリを持つMenuHolderを作成します。
   *
   * @param plugin    your Plugin
   * @param inventory the Inventory
   * @see GuiInventoryHolder#GuiInventoryHolder(Plugin, Inventory)
   */
  public MenuHolder(P plugin, Inventory inventory) {
    this(GuiListener.getInstance(), plugin, inventory);
  }

  /**
   * 指定されたインベントリを持つMenuHolderを作成します。
   *
   * @param plugin      your Plugin
   * @param inventory   the Inventory
   * @param guiListener the gui listener that calls the onOpen, onClick and
   *                    onClose methods
   * @see GuiInventoryHolder#GuiInventoryHolder(GuiListener, Plugin, Inventory)
   */
  public MenuHolder(GuiListener guiListener, P plugin, Inventory inventory) {
    super(guiListener, plugin, inventory);

    this.buttons = new MenuButton<?>[getInventory().getSize()];
  }

  /**
   * フレームワークから呼び出される。スロットに登録されたボタンがあれば、そのボタンにイベントを委譲する。
   * <p>
   * このメソッドをオーバーライドするサブクラスは、常に {@code super.onClick(event);} を呼び出す必要があります。
   *
   * @param event the inventory click event
   */
  @Override
  public void onClick(InventoryClickEvent event) {
    // only use the buttons when the top inventory was clicked.
    Inventory clickedInventory = getClickedInventory(event);
    if (clickedInventory == null) {
      return;
    }

    getButtonOptionally(event.getRawSlot()).ifPresent(
        (MenuButton button) -> button.onClick(this, event));
  }

  /**
   * スロットにボタンを設定する。
   * このメソッドをオーバーライドするサブクラスは、{@link MenuButton#onAdd(MenuHolder, int)}を呼び出すか、super.setButton(slot,
   * button)を呼び出す必要がある。
   *
   * @param slot   the slot number
   * @param button the button
   * @return ボタンを追加できる場合はtrue、そうでない場合はfalseを指定する。
   */
  public boolean setButton(int slot, MenuButton<?> button) {
    if (!unsetButton(slot)) {
      return false;
    }
    if (button == null) {
      return true;
    }

    var iterator = addButtonCallbacks.iterator();
    while (iterator.hasNext()) {
      var nextReference = iterator.next();
      var nextCallback = nextReference.get();
      if (nextCallback == null) {
        iterator.remove(); // コールバックがガベージコレクションされた場合、リストから削除する。
      } else {
        if (!nextCallback.onAdd(slot, button)) {
          return false;
        }
      }
    }

    if (((MenuButton) button).onAdd(this, slot)) {
      getInventory().setItem(slot, button.getIcon());
      this.buttons[slot] = button;
      buttonCount += 1;
      return true;
    } else {
      return false;
    }
  }

  /**
   * 指定されたスロットにあるボタンを取得します。
   *
   * @param slot the slot index
   * @return 与えられたスロットにボタンがある場合はボタン、そうでない場合はnull
   */
  public MenuButton<?> getButton(int slot) {
    if (slot < 0 || slot >= buttons.length) {
      return null;
    }

    return this.buttons[slot];
  }

  /**
   * 指定されたスロットにあるボタンを取得します。
   *
   * @param slot the slot index
   * @return 与えられたスロットにボタンがある場合はそのボタンを含むオプショナル、そうでない場合は空のオプショナル。
   */
  public Optional<MenuButton<?>> getButtonOptionally(int slot) {
    return Optional.ofNullable(getButton(slot));
  }

  /**
   * 登録されているすべてのボタンのスナップショットを取得します。ボタンが登録されていない場合は、空のマップが返されます。
   *
   * @return a new SortedMap containing the buttons
   */
  public SortedMap<Integer, MenuButton<?>> getButtons() {
    var map = new TreeMap<Integer, MenuButton<?>>();
    for (int i = 0; i < this.buttons.length; i++) {
      MenuButton button = this.buttons[i];
      if (button != null) {
        map.put(i, button);
      }
    }
    return map;
  }

  /**
   * スロットからボタンを取り外す。
   * このメソッドをオーバーライドするサブクラスは、ボタンが削除されるときに{@link MenuButton#onRemove(MenuHolder, int)}
   * を呼び出すか、super.unsetButton(slot)を呼び出す必要がある。
   *
   * @param slot the slot number
   * @return ボタンがスロットから正常に取り出されたかどうか
   */
  public boolean unsetButton(int slot) {
    MenuButton menuButton = this.buttons[slot];
    if (menuButton == null) {
      return true;
    }

    var iterator = removeButtonCallbacks.iterator();
    while (iterator.hasNext()) {
      var nextReference = iterator.next();
      var nextCallback = nextReference.get();
      if (nextCallback == null) {
        iterator.remove(); // コールバックがガベージコレクションされた場合、リストから削除する。
      } else {
        if (!nextCallback.onRemove(slot, menuButton)) {
          return false;
        }
      }
    }

    if (menuButton.onRemove(this, slot)) {
      this.buttons[slot] = null;
      getInventory().setItem(slot, null);
      buttonCount -= 1;
      return true;
    } else {
      return false;
    }
  }

  /**
   * メニューからすべてのボタンを削除します。
   * このメソッドをオーバーライドするサブクラスは、ボタンが削除されるときに{@link MenuButton#onRemove(MenuHolder, int)}
   * を呼び出すか、super.unsetButton(slot)を呼び出す必要がある。
   */
  public void clearButtons() {
    for (int i = 0; i < this.buttons.length; i++) {
      unsetButton(i);
    }
  }

  /**
   * すべてのボタンを反復処理するイテレータを取得します。
   *
   * @return a new Iterator
   * @apiNote ボタンのないスロットは、このイテレータの対象外です。
   */
  @Override
  public @NotNull ListIterator<MenuButton<?>> iterator() {
    return new ListIterator<>() {

      int cursor = -1;
      int lastFound = -1;
      int lastReturned = -1;
      int lastUpdated = -1;

      private void advanceTillNextButton() {
        if (cursor == -1) {
          cursor = 0;
        }

        while (lastFound < cursor && cursor < buttons.length) {
          if (buttons[cursor] != null) {
            lastFound = cursor;
            break;
          }
          cursor += 1;
        }
      }

      private void advanceTillPreviousButton() {
        if (cursor == buttons.length) {
          cursor -= 1;
        }

        while (cursor >= 0) {
          if (buttons[cursor] != null) {
            lastFound = cursor;
            break;
          }
          cursor -= 1;
        }
      }

      @Override
      public boolean hasNext() {
        advanceTillNextButton();
        return cursor < buttons.length;
      }

      @Override
      public MenuButton<?> next() {
        advanceTillNextButton(); // hasNextが先に呼び出された場合は無効です。

        if (cursor == buttons.length) {
          throw new NoSuchElementException(); // there are no elements to explore
        }
        if (lastFound == lastReturned) {
          throw new NoSuchElementException(); // if after advancing till the next button we still didn't find a new one
        }

        cursor += 1; // makes sure that advanceTillNextButton can actually advance again
        return buttons[lastReturned = lastFound];
      }

      @Override
      public boolean hasPrevious() {
        advanceTillPreviousButton();
        return cursor >= 0;
      }

      @Override
      public MenuButton<?> previous() {
        advanceTillPreviousButton(); // no-op if hasPrevious has been called before us

        if (cursor == -1) {
          throw new NoSuchElementException(); // there ae no more elements to explore
        }
        if (lastFound == lastReturned) {
          throw new NoSuchElementException(); // if after advancing till the previous button we still didn't find a new
          // one
        }

        cursor -= 1; // makes sure that advanceTillPreviousButton can actually advance again
        return buttons[lastReturned = lastFound];
      }

      @Override
      public int nextIndex() {
        advanceTillNextButton();
        return cursor;
      }

      @Override
      public int previousIndex() {
        advanceTillPreviousButton();
        return cursor;
      }

      @Override
      public void remove() {
        if (lastUpdated == lastReturned) {
          throw new IllegalStateException("Need to call next() or previous() first");
        }

        unsetButton(lastUpdated = lastReturned);
      }

      @Override
      public void set(MenuButton<?> menuButton) {
        if (lastUpdated == lastReturned) {
          throw new IllegalStateException("Need to call next() or previous() first");
        }

        setButton(lastUpdated = lastReturned, menuButton);
      }

      @Override
      public void add(MenuButton<?> menuButton) {
        throw new UnsupportedOperationException(
            "Adding is not supported by this ListIterator. Use MenuHolder#setButton(int, MenuButton) instead.");
      }
    };
  }

  /**
   * メニューのすべてのボタンをカバーするスプリッタを入手する。
   *
   * @return a new Spliterator that has the characteristics SIZED, SUB SIZED,
   *         NONNULL and ORDERED
   * @apiNote Slots without a button are not covered by this spliterator.
   */
  @Override
  public Spliterator<MenuButton<?>> spliterator() {
    return Spliterators.spliterator(iterator(), buttonCount,
        Spliterator.NONNULL | Spliterator.ORDERED);
  }

  /**
   * このメニューの各ボタンに対してアクションを実行します。
   * <p>
   * このアクションは、このメニューのボタンを設定または削除することができます。
   * というのは、このメソッドはその際にConcurrentModificationExceptionを投げないという意味です。
   *
   * @param action the action
   */
  public void forEach(BiConsumer<Integer, ? super MenuButton<?>> action) {
    for (int i = 0; i < buttons.length; i++) {
      MenuButton<?> button = buttons[i];
      if (button != null) {
        action.accept(i, button);
      }
    }
  }

  /**
   * ボタンが追加されたときに呼び出されるコールバックを追加します。
   *
   * @param buttonAddListener the callback
   * @see #removeButtonAddCallback(ButtonAddCallback)
   */
  public void addButtonAddCallback(ButtonAddCallback buttonAddListener) {
    if (buttonAddListener == null) {
      return;
    }
    addButtonCallbacks.add(new WeakReference<>(buttonAddListener));
  }

  /**
   * ボタンが削除されたときに呼び出されるコールバックを追加します。
   *
   * @param buttonRemoveListener the callback
   * @see #removeButtonRemoveCallback(ButtonRemoveCallback)
   */
  public void addButtonRemoveCallback(ButtonRemoveCallback buttonRemoveListener) {
    if (buttonRemoveListener == null) {
      return;
    }
    removeButtonCallbacks.add(new WeakReference<>(buttonRemoveListener));
  }

  /**
   * ボタンが追加されたときに呼び出されるコールバックを削除します（もうありません）。
   *
   * @param buttonAddListener the callback
   */
  public void removeButtonAddCallback(ButtonAddCallback buttonAddListener) {
    Objects.requireNonNull(buttonAddListener, "Button-Add callback cannot be null");
    // WeakReferenceがequalsをオーバーライドしないため、removeIfを使用する必要がある。
    // LinkedListのremove操作はO(n)なので、これは重要ではありません。
    addButtonCallbacks.removeIf(ref -> buttonAddListener.equals(ref.get()));
  }

  /**
   * ボタンがメニューから削除されたときに呼び出されるコールバックを削除する（もはや）。
   *
   * @param buttonRemoveListener the callback
   */
  public void removeButtonRemoveCallback(ButtonRemoveCallback buttonRemoveListener) {
    Objects.requireNonNull(buttonRemoveListener, "Button-Remove callback cannot be null");
    // WeakReferenceがequalsをオーバーライドしないため、removeIfを使用する必要がある。
    // LinkedListのremove操作はO(n)なので、これは重要ではありません。
    removeButtonCallbacks.removeIf(ref -> buttonRemoveListener.equals(ref.get()));
  }

  /**
   * 登録すると、メニューにボタンが追加されたときに呼び出されるコールバックです。
   *
   * @see #addButtonAddCallback(ButtonAddCallback)
   */
  @FunctionalInterface
  public interface ButtonAddCallback {

    boolean onAdd(int slot, MenuButton<?> button);

  }

  /**
   * 登録されている場合、メニューからボタンが削除されたときに呼び出されるコールバックです。
   *
   * @see #addButtonRemoveCallback(ButtonRemoveCallback)
   */
  @FunctionalInterface
  public interface ButtonRemoveCallback {

    boolean onRemove(int slot, MenuButton<?> button);

  }

}
