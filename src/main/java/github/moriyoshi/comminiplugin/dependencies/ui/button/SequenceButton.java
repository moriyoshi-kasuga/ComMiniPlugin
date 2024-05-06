package github.moriyoshi.comminiplugin.dependencies.ui.button;

import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import java.util.Objects;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * 2つのボタンを順次合成していくボタンです。
 *
 * @param <P>   ボタン用プラグイン
 * @param <MHR> {@code MH1}と{@code MH2}の両方のスーパータイプである。
 * @param <MH1> 第1ボタンのメニューホルダタイプ
 * @param <MH2> 第2ボタンのメニューホルダーの種類
 */
public class SequenceButton<P extends Plugin, MHR extends MenuHolder<P>, MH1 extends MHR, MH2 extends MHR>
    implements MenuButton<MHR> {

  private final MenuButton<MH1> first;
  private final MenuButton<MH2> second;

  public SequenceButton(MenuButton<MH1> first, MenuButton<MH2> second) {
    this.first = Objects.requireNonNull(first, "First button cannot be null");
    this.second = Objects.requireNonNull(second, "Second button cannot be null");
  }

  /**
   * シーケンスの最初のボタンを取得します。
   *
   * @return the first button
   */
  protected MenuButton<MH1> getFirst() {
    return first;
  }

  /**
   * 順番に2つ目のボタンを取得します。
   *
   * @return the second button
   */
  protected MenuButton<MH2> getSecond() {
    return second;
  }

  /**
   * クリックイベントを第1ボタンに委譲し、次に第2ボタンに委譲します。 メニューホルダー{@link MHR}は{@link MH1}と{@link MH2}の両方にダウンキャストされます。
   *
   * @param holder the MenuHolder
   * @param event  the InventoryClickEvent
   */
  @SuppressWarnings("unchecked")
  @Override
  public void onClick(@NotNull MHR holder, @NotNull InventoryClickEvent event) {
    getFirst().onClick((MH1) holder, event);
    getSecond().onClick((MH2) holder, event);
  }

  /**
   * アイコンスタックを取得します。
   *
   * @return the icon of the first button
   */
  @Override
  public ItemStack getIcon() {
    return getFirst().getIcon();
  }

  /**
   * onAddコールバックを第1ボタンと第2ボタンに委譲する（順不同）。
   *
   * @param holder このボタンが追加されるホルダー
   * @param slot   メニューの中の位置
   * @return 両ボタンのonAddコールバックがtrueを返したかどうか
   * @see MenuHolder#setButton(int, MenuButton)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean onAdd(MHR holder, int slot) {
    return getFirst().onAdd((MH1) holder, slot)
        & getSecond().onAdd((MH2) holder, slot);
  }

  /**
   * onRemoveコールバックを第2ボタンと第1ボタンの順に委譲する。
   *
   * @param holder このボタンが消されるホルダー
   * @param slot   メニューの中の位置
   * @return 両ボタンのonRemoveコールバックがtrueを返したかどうか
   * @see MenuHolder#unsetButton(int)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean onRemove(MHR holder, int slot) {
    return getSecond().onRemove((MH2) holder, slot)
        & getFirst().onRemove((MH1) holder, slot);
  }

}
