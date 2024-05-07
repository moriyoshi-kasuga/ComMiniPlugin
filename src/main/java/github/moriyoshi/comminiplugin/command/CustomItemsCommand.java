package github.moriyoshi.comminiplugin.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class CustomItemsCommand extends CommandAPICommand {

  public CustomItemsCommand() {
    super("customitems");
    withPermission(CommandPermission.OP);
    executesPlayer((sender, args) -> {
      new CustomItemMenu().openInv(sender);
    });
  }

  private static final class CustomItemMenu extends ListMenu<String> {
    public CustomItemMenu() {
      super("<green>CustomItems", 45, new ArrayList<>(CustomItem.registers.keySet()), (key) -> {
        ItemStack item = CustomItem.getNewCustomItem(key).getItem();
        return new ItemButton<>(new ItemBuilder(item).addLore("").addLore(key).build()) {
          @Override
          public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
              @NotNull InventoryClickEvent event) {
            event.getWhoClicked().getInventory()
                .addItem(CustomItem.getNewCustomItem(key).getItem());
          }
        };
      });
    }

    public CustomItemMenu(ComMiniPlugin plugin, String title, int pageSize, List<String> rewards, int rewardStartIndex,
        int rewardEndIndex, Function<String, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public Optional<ListMenu<String>> getDefaultMenu() {
      return Optional.of(new CustomItemMenu());
    }

    @Override
    public Optional<Function<List<String>, ListMenu<String>>> getNewRewadsMenu() {
      return Optional.of((r -> new CustomItemMenu(getPlugin(), title, getPageSize(), r, 0,
          Math.min(r.size(), getPageSize()), function)));
    }

    @Override
    public Optional<BiPredicate<String, String>> getSerachMethod() {
      return Optional.of((search, key) -> key.toLowerCase().contains(search.toLowerCase()));
    }

  }
}
