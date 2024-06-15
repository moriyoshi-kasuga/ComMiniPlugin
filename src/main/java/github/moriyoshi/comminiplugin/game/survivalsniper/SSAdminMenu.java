package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame.Mode;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.InfiniteIterator;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.system.game.IGetGame;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class SSAdminMenu extends MenuHolder<ComMiniPlugin>
    implements IGetGame<SSGame>, OnlyBeforeStartGameMenu {

  private final BukkitRunnable task = createAutoCloseTask();

  public SSAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:サバイバルスナイパー");
    setButton(
        13,
        new ItemButton<>(
            new ItemBuilder(Material.BLACK_CONCRETE)
                .name("<yellow>FFA mode")
                .lore("<gray>default")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            getGame().setMode(Mode.FFA);
          }
        });
    setButton(
        14,
        new ItemButton<>(
            new ItemBuilder(Material.END_CRYSTAL)
                .name("<rainbow>Team mode")
                .lore("<gray>既にTeam Mode 中はランダムに振り分けます")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            val game = getGame();
            if (game.getMode() != Mode.TEAM) {
              game.setMode(Mode.TEAM);
              return;
            }
            AnvilInputs.getInteger(
                    ComMiniPlugin.getPlugin(),
                    "<gray>1~4チームまで振り分けます",
                    i -> 4 >= i && i >= 1,
                    (t, u) -> {
                      game.runPlayers(
                          p -> game.prefix.send(p, "<yellow>=====Random Team<yellow>====="));
                      val random = new Random();
                      val list_colors =
                          Stream.of(
                                  ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.BLUE)
                              .limit(t)
                              .toList();
                      Collections.shuffle(list_colors, random);
                      val colors = new InfiniteIterator<>(list_colors);
                      val list =
                          game.players.entrySet().stream()
                              .filter(entry -> entry.getValue().getSecond() != null)
                              .map(entry -> entry.getKey())
                              .toList();
                      Collections.shuffle(list, random);
                      list.forEach(
                          uuid -> {
                            val color = colors.next();
                            game.players.put(uuid, Pair.of(game.AIR_LIMIT, color));
                            val message =
                                BukkitUtil.mm(
                                    Bukkit.getPlayer(uuid)
                                        + " は <#"
                                        + BukkitUtil.chatColorToHex(color)
                                        + ">"
                                        + color.name()
                                        + "<gray> になりました");
                            game.runPlayers(p -> game.prefix.send(p, message));
                          });
                      return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                .open((Player) event.getWhoClicked());
          }
        });
    setButton(16, GameStartButton.of());
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    this.task.cancel();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (isClosed()) {
      return;
    }
    super.onClick(event);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    this.task.runTaskTimer(getPlugin(), 1, 1);
  }
}
