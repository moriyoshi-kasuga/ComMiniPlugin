package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AddSpecButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private AddSpecButton(ItemStack item) {
    super(item);
  }

  public static AddSpecButton of() {
    if (GameSystem.isStarted()) {
      return new AddSpecButton(
          new ItemBuilder(Material.NETHER_STAR).name(
              GameSystem.getNowGame().name + "<reset><gray>を観戦する").build());
    }
    return new AddSpecButton(
        new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開始されていません").build());
  }

  @Override
  public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
      @NotNull InventoryClickEvent event) {
    var p = (Player) event.getWhoClicked();
    if (!GameSystem.isStarted()) {
      return;
    }
    if (!GameSystem.getNowGame().addSpec(p)) {
      ComMiniPrefix.MAIN.send(p, "<red>このゲームに観戦できません");
    }
  }

}
