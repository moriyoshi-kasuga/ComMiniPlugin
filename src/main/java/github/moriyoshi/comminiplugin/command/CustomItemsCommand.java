package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.val;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItemsCommand extends CommandAPICommand {

  public CustomItemsCommand() {
    super("customitems");
    withPermission(CommandPermission.OP);
    executesPlayer(
        (sender, args) -> {
          new CustomItemMenu().openInv(sender);
        });
  }


  private static final class CustomItemMenu extends ListMenu<String, CustomItemMenu> {
    public CustomItemMenu() {
      super(
          "<green>CustomItems",
          45,
          new ArrayList<>(CustomItem.canShowingRegisters.keySet()),
          (key) -> {
            final ItemStack item = CustomItem.getNewCustomItem(key).getItem();
            return new ItemButton<>(new ItemBuilder(item).addLore("").addLore(key).build()) {
              @Override
              public void onClick(
                  @NotNull final MenuHolder<ComMiniPlugin> holder,
                  @NotNull final InventoryClickEvent event) {
                event
                    .getWhoClicked()
                    .getInventory()
                    .addItem(CustomItem.getNewCustomItem(key).getItem());
              }
            };
          });
    }

    public CustomItemMenu(
        final ComMiniPlugin plugin,
        final String title,
        final int pageSize,
        final List<String> rewards,
        final int rewardStartIndex,
        final int rewardEndIndex,
        final Function<String, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public @NotNull Optional<Supplier<CustomItemMenu>> getDefaultMenu() {
      return Optional.of(CustomItemMenu::new);
    }

    @Override
    public Optional<Function<String, List<String>>> getMenuBySearch() {
      return Optional.of(
          str -> {
            val lower = str.toLowerCase();
            return CustomItem.canShowingRegisters.keySet().stream()
                .filter(key -> key.toLowerCase().contains(lower))
                .toList();
          });
    }
  }
}
