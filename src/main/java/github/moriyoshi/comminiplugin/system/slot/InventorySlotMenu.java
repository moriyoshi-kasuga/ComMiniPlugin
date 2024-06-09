package github.moriyoshi.comminiplugin.system.slot;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import java.util.stream.IntStream;
import lombok.val;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public final class InventorySlotMenu extends MenuHolder<ComMiniPlugin> {

  private final InventorySlot slots;

  private int swapSlot = -1;

  public InventorySlotMenu(final InventorySlot slots) {
    super(ComMiniPlugin.getPlugin(), 54, "<blue>インベントリーのカスタマイズ");
    this.slots = slots;
    set();
    IntStream.concat(IntStream.range(27, 36), IntStream.range(46, 54))
        .forEach(i -> setButton(i, empty));
    setButton(45, GameMenuButton.of());
  }

  private void update(final Player p, final int slot) {
    if (swapSlot == -1) {
      swapSlot = slot;
      p.playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1, 1);
      return;
    }
    p.playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1, 1);
    slots.swap(swapSlot, slot);
    swapSlot = -1;
    set();
  }

  private void set() {
    val iter = slots.toItemStacks().listIterator();
    while (iter.hasNext()) {
      val i = iter.nextIndex();
      val button =
          new ItemButton<>(iter.next()) {
            @Override
            public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
              if (holder.getInventory().equals(event.getClickedInventory())) {
                update(((Player) event.getWhoClicked()), i);
              }
            }
          };
      if (9 > i) {
        setButton(36 + i, button);
      } else {
        setButton(i - 9, button);
      }
    }
  }
}
