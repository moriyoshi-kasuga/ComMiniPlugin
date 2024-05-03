package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.PageMenu;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class CustomItemsCommand extends CommandAPICommand {

  public CustomItemsCommand() {
    super("customitems");
    withPermission(CommandPermission.OP);
    executesPlayer((sender, args) -> {
      open(sender);
    });
  }

  private static void open(Player sender) {
    var map = CustomItem.registers.entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> CustomItem.getNewCustomItem(e.getKey()),
            (v1, v2) -> v1,
            HashMap::new
        ));

    sender.openInventory(new ClaimItemsMenu(ComMiniPlugin.getPlugin(), 45, map).getInventory());
  }

  private static final class ClaimItemsMenu extends PageMenu<ComMiniPlugin> {

    public ClaimItemsMenu(ComMiniPlugin plugin, int pageSize, HashMap<String, CustomItem> rewards) {
      this(plugin, pageSize, rewards, 0, Math.min(rewards.size(), pageSize));
    }

    private ClaimItemsMenu(ComMiniPlugin plugin, int pageSize, HashMap<String, CustomItem> rewards,
        int rewardStartIndex, int rewardEndIndex) {
      super(plugin, new ItemPage(plugin, pageSize, rewards, rewardStartIndex, rewardEndIndex),
          "<green>CustomItems", null, null
      );
    }

    @Override
    public void onOpen(InventoryOpenEvent openEvent) {
      super.onOpen(openEvent);
      setButton(getPageSize(), new ItemButton<>(
          new ItemBuilder(Material.ENDER_PEARL).name("<green>最初のページにもどる").build()) {
        @Override
        public void onClick(MenuHolder<?> holder, InventoryClickEvent event) {
          open(((Player) event.getWhoClicked()));
        }
      });
      setButton(getPageSize() + 8,
          new ItemButton<>(
              new ItemBuilder(Material.BOOK).name("<aqua>クリックで文字検索").build()) {
            @Override
            public void onClick(MenuHolder<?> holder, InventoryClickEvent event) {
              AnvilInputs
                  .postClose(AnvilInputs.getInput(getPlugin(), "<aqua>文字で検索", (t, u) -> t,
                          (s, completion) -> List.of(
                              ResponseAction.openInventory(
                                  new ClaimItemsMenu(getPlugin(), 45,
                                      CustomItem.registers.entrySet().stream()
                                          .filter(entry -> entry.getKey().toLowerCase()
                                              .contains(s.toLowerCase()))
                                          .collect(Collectors.toMap(Map.Entry::getKey,
                                              e -> CustomItem.getNewCustomItem(e.getKey()),
                                              (v1, v2) -> v1, HashMap::new
                                          ))
                                  )
                                      .getInventory()))
                      ),
                      getPlugin(), player -> player.openInventory(getInventory())
                  )
                  .open((Player) event.getWhoClicked());
            }
          }
      );
    }

    @Override
    public ItemPage getPage() {
      return (ItemPage) super.getPage();
    }

    @Override
    protected boolean needsRedirects() {
      return false;
    }

    private static final class ItemPage extends MenuHolder<ComMiniPlugin> {

      private final int rewardStartIndex, rewardEndIndex;
      private final HashMap<String, CustomItem> rewards;
      private final List<String> list;

      private ItemPage(ComMiniPlugin plugin, int pageSize, HashMap<String, CustomItem> rewards,
          int rewardStartIndex, int rewardEndIndex) {
        super(plugin, pageSize);
        this.rewardStartIndex = rewardStartIndex;
        this.rewardEndIndex = rewardEndIndex;
        this.rewards = rewards;
        this.list = rewards.keySet().stream().toList();
      }

      @Override
      public void onOpen(InventoryOpenEvent event) {
        for (int slot = 0;
            slot < getInventory().getSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
          String key = list.get(rewardStartIndex + slot);
          ItemStack item = rewards.get(key).getItem();
          setButton(slot,
              new ItemButton<>(new ItemBuilder(item).addLore("").addLore(key).build()) {
                @Override
                public void onClick(MenuHolder<?> holder, InventoryClickEvent event) {
                  event.getWhoClicked().getInventory()
                      .addItem(CustomItem.getNewCustomItem(key).getItem());
                }
              }
          );
        }
      }

      @Override
      public void onClose(InventoryCloseEvent event) {
        clearButtons();
      }
    }
  }
}
