package github.moriyoshi.comminiplugin.constant;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

/**
 * MenuItem
 */
public class MenuItem extends CustomItem {

  public MenuItem() {
    this(new ItemBuilder(Material.BOOK).name("<red>Menu").glow().build());
  }

  public MenuItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (open(e.getPlayer())) {
      return;
    }
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>あなたはmenuを開けません");
  }

  @Override
  public @NotNull String getIdentifier() {
    return "menu";
  }

  public static boolean open(Player p) {
    if (GameSystem.inGame() && GameSystem.isStarted() && GameSystem.nowGame().isGamePlayer(p)) {
      return false;
    }
    new InnerMenu().openInv(p);
    return true;
  }

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {
    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, new ItemButton<>(new ItemBuilder(Material.ENDER_PEARL).name("<blue>ロビーにテレポート").build()) {

        @Override
        public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
          var p = (Player) event.getWhoClicked();
          if (GameSystem.inGame() && GameSystem.nowGame().isGamePlayer(p)) {
            ComMiniPrefix.MAIN.send(p, "<red>あなたはロビーにテレポートできません");
            return;
          }
          p.teleport(ComMiniWorld.LOBBY);
        }

      });
      if (GameSystem.isStarted()) {
        setButton(14, new ItemButton<>(
            new ItemBuilder(Material.NETHER_STAR).name(GameSystem.nowGame().name + "<reset><gray>を観戦する").build()) {

          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            var p = (Player) event.getWhoClicked();
            if (!GameSystem.nowGame().addSpec(p)) {
              ComMiniPrefix.MAIN.send(p, "<red>このゲームに観戦できません");
            }
          }

        });
      }
      if (GameSystem.inGame()) {
        var game = GameSystem.nowGame();
        setButton(13,
            new ItemButton<>(new ItemBuilder(game.material).name(game.name).lore(game.description).build()) {
              @Override
              public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
                var p = (Player) event.getWhoClicked();
                GameSystem.nowGame().gameMenu(p).ifPresentOrElse(menu -> menu.openInv(p), () -> {
                  ComMiniPrefix.MAIN.send(p, "<red>Menuは開けません");
                });
              }
            });
      } else {
        setButton(13, new ItemButton<>(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build()));
        setButton(14, new ItemButton<>(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開始されていません").build()));
      }
    }
  }

  @Override
  public void dropItem(PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return false;
  }
}
