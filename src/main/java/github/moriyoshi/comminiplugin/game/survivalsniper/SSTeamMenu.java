package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame.Mode;
import github.moriyoshi.comminiplugin.system.IGetGame;
import github.moriyoshi.comminiplugin.system.buttons.HotbarSlotButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class SSTeamMenu extends MenuHolder<ComMiniPlugin>
    implements IGetGame<SSGame>, OnlyBeforeStartGameMenu {

  private final ItemStack SPEC = new ItemBuilder(Material.GRAY_CONCRETE).name("<gray>観戦する").build();
  private final ItemStack LEAVE = new ItemBuilder(Material.RED_CONCRETE).name("<red>抜ける").build();

  private final BukkitRunnable task = createAutoCloseTask();

  private ItemButton<?> createTeamButton(ChatColor color) {
    val list =
        getGame().players.entrySet().stream()
            .filter(
                entry -> {
                  return entry.getValue().getThird() == color;
                })
            .map(entry -> "<gray> " + Bukkit.getOfflinePlayer(entry.getKey()).getName())
            .collect(Collectors.toCollection(ArrayList::new));
    if (list.isEmpty()) {
      list.add("<gray>empty");
    }
    return new ItemButton<>(
        new ItemBuilder(Material.valueOf(color.name() + "_GLAZED_TERRACOTTA"))
            .name(Util.colorToComponent(color, color.name()).append(Util.mm(" <white>チームに参加する")))
            .lore("<gray>参加者リスト：")
            .addLore(list)
            .build()) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        getGame().joinPlayer((Player) event.getWhoClicked(), true, color);
      }
    };
  }

  public SSTeamMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>サバイバルスナイパー");
    setButton(
        2,
        new ItemButton<>(SPEC) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().joinPlayer(((Player) event.getWhoClicked()), false, null);
          }
        });
    setButton(
        6,
        new ItemButton<>(LEAVE) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().leavePlayer((Player) event.getWhoClicked());
          }
        });
    setButton(4, new HotbarSlotButton(SSPlayer.class));
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

  @Override
  public Component additional() {
    if (getGame().getMode() == Mode.TEAM) {
      setButton(20, createTeamButton(ChatColor.RED));
      setButton(21, createTeamButton(ChatColor.GREEN));
      setButton(23, createTeamButton(ChatColor.YELLOW));
      setButton(24, createTeamButton(ChatColor.BLUE));
      return null;
    }
    return Util.mm("<red>モードが変わりました、もう一度メニューを開いてください");
  }
}
