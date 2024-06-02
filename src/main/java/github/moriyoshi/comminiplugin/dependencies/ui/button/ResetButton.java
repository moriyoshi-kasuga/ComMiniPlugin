package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * クリックされたメニューのボタンをリセットするボタンです。
 *
 * @param <MH> the menu holder type
 */
public class ResetButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

  private Supplier<IntStream> slots;
  private IntFunction<? extends MenuButton<? super MH>> mapper;

  /**
   * スロットプロバイダとスロットからボタンへのマッピングがないリセットボタンを作成します。 このメソッドを使用するサブクラスは、{@link #setMapping(Supplier,
   * IntFunction)} )} またはそのオーバーロードのいずれかを呼び出す必要があります。 または、{@link #getResetSlots()}と{@link
   * #getButtonFor(int)}をオーバーライドする必要があります。
   *
   * @param icon the button's icon
   */
  protected ResetButton(ItemStack icon) {
    super(icon);
  }

  /**
   * リセットボタンを作成します。
   *
   * @param icon the icon
   * @param newContents the slot-to-button mapping
   */
  public ResetButton(ItemStack icon, Map<Integer, ? extends MenuButton<? super MH>> newContents) {
    super(icon);
    setMapping(newContents);
  }

  /**
   * リセットボタンを作成します。
   *
   * @param icon the icon
   * @param newContents the slot-to-button mapping
   */
  public ResetButton(ItemStack icon, MenuButton<? super MH>[] newContents) {
    super(icon);
    setMapping(newContents);
  }

  /**
   * リセットボタンを作成する.
   *
   * @param icon the icon
   * @param newContents the slot-to-button mapping
   */
  public ResetButton(ItemStack icon, List<? extends MenuButton<? super MH>> newContents) {
    super(icon);
    setMapping(newContents);
  }

  /**
   * リセットボタンを作成します。
   *
   * @param icon the icon
   * @param slots このボタンがリセットを実行するスロットを示します。
   * @param mapper どのボタンがどのスロットでリセットされる必要があるかを計算するマッピング機能
   */
  public ResetButton(
      ItemStack icon,
      Supplier<IntStream> slots,
      IntFunction<? extends MenuButton<? super MH>> mapper) {
    super(icon);
    setMapping(slots, mapper);
  }

  /**
   * マッピングを設定します。
   *
   * @param newContents スロットとボタンの対応表
   */
  public void setMapping(Map<Integer, ? extends MenuButton<? super MH>> newContents) {
    Objects.requireNonNull(newContents, "NewContents cannot be null");
    this.slots = () -> newContents.keySet().stream().mapToInt(Integer::intValue);
    this.mapper = newContents::get;
  }

  /**
   * Set the mapping.
   *
   * @param newContents the slot-to-button mapping
   */
  public void setMapping(MenuButton<? super MH>[] newContents) {
    Objects.requireNonNull(newContents, "NewContents cannot be null");
    this.slots = () -> IntStream.range(0, newContents.length);
    this.mapper = i -> newContents[i];
  }

  /**
   * Set the mapping.
   *
   * @param newContents the slot-to-button mapping
   */
  public void setMapping(List<? extends MenuButton<? super MH>> newContents) {
    Objects.requireNonNull(newContents, "NewContents cannot be null");
    this.slots = () -> IntStream.range(0, newContents.size());
    this.mapper = newContents::get;
  }

  /**
   * Set the mapping.
   *
   * @param slots the slots for which to reset buttons
   * @param mapper the slot-to-button mapping
   */
  public void setMapping(
      Supplier<IntStream> slots, IntFunction<? extends MenuButton<? super MH>> mapper) {
    this.slots = Objects.requireNonNull(slots, "Slots supplier cannot be null");
    this.mapper = Objects.requireNonNull(mapper, "Slot-to-button mapper cannot be null");
  }

  /**
   * 新しいボタンの計算が必要なスロットを取得します。
   *
   * @return the stream of slots
   */
  protected IntStream getResetSlots() {
    return Objects.requireNonNull(
        slots.get(),
        "The supplier that supplies reset slots cannot return null. Just use an empty stream"
            + " already.");
  }

  /**
   * 指定されたスロットに設定する必要があるボタンを取得します。
   *
   * @param slot the slot
   * @return the new button, or null if there shouldn't be a button at the slot
   */
  protected MenuButton<? super MH> getButtonFor(int slot) {
    return mapper.apply(slot);
  }

  /**
   * Resets the buttons in the menu.
   *
   * @param menuHolder the menu holder
   * @param event the InventoryClickEvent
   */
  @Override
  public void onClick(@NotNull MH menuHolder, @NotNull InventoryClickEvent event) {
    getResetSlots().forEach(slot -> menuHolder.setButton(slot, getButtonFor(slot)));
  }
}
