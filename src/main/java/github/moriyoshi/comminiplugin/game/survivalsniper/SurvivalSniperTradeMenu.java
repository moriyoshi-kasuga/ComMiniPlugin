package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class SurvivalSniperTradeMenu extends MenuHolder<ComMiniPlugin> {
  private static final int EscapeDeepLevel = 4;
  private static final int Beef = 1;

  public SurvivalSniperTradeMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<green>Trade");
    for (int i = 0; i < 27; i++) {
      setButton(i, empty);
    }
    setButton(10, new ItemButton<>(
        new ItemBuilder(new EscapeDeep().getItem()).lore("", "<green>" + EscapeDeepLevel + "レベルでトレード").build()) {

      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        var p = (Player) event.getWhoClicked();
        int level = p.getLevel();
        if (EscapeDeepLevel > level) {
          p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
          return;
        }
        p.setLevel(level - EscapeDeepLevel);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        p.getInventory().addItem(new EscapeDeep().getItem());
      }

    });
    setButton(11, new ItemButton<>(
        new ItemBuilder(new ItemStack(Material.COOKED_BEEF)).lore("", "<green>" + Beef + "レベルでトレード").build()) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        var p = (Player) event.getWhoClicked();
        int level = p.getLevel();
        if (Beef > level) {
          p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
          return;
        }
        p.setLevel(level - Beef);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF));
      }

    });
  }

}
