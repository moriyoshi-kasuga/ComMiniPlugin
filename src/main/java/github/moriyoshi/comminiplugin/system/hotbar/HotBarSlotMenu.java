package github.moriyoshi.comminiplugin.system.hotbar;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import lombok.val;

public final class HotBarSlotMenu extends MenuHolder<ComMiniPlugin> {

  private final HotBarSlot slots;

  private int swapSlot = -1;

  public HotBarSlotMenu(final HotBarSlot slots) {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>インベントリーのカスタマイズ");
    this.slots = slots;
    set();
    for (int i = 9; i < 27; i++) {
      setButton(i, empty);
    }
    setButton(18, GameMenuButton.of());
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
    var i = 0;
    for (val item : slots.toItemStacks()) {
      setButton(i, new ItemButton<>(item) {

        @Override
        public void onClick(@NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
          if (holder.getInventory().equals(event.getClickedInventory())) {
            update(((Player) event.getWhoClicked()), event.getSlot());
          }
        }

      });
      i++;
    }
  }

}