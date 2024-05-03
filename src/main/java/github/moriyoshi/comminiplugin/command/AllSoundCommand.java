package github.moriyoshi.comminiplugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.PageMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;

/**
 * AllSoundCommand
 */
public class AllSoundCommand extends CommandAPICommand {
  public AllSoundCommand() {
    super("allsound");
    executesPlayer((p, args) -> {
      open(p);
    });
  }

  private static final List<Sound> sounds = new ArrayList<>(Arrays.asList(Sound.values()));
  private static final List<Material> materials = new ArrayList<>(Arrays.asList(Material.values()));

  private static void open(Player sender) {
    new InnerMenu(ComMiniPlugin.getPlugin(), 45, sounds).openInv(sender);
  }

  private static class InnerMenu extends PageMenu<ComMiniPlugin> {
    public InnerMenu(ComMiniPlugin plugin, int pageSize, List<Sound> sounds) {
      this(plugin, pageSize, sounds, 0, Math.min(sounds.size(), pageSize));
    }

    private InnerMenu(ComMiniPlugin plugin, int pageSize, List<Sound> rewards, int rewardStartIndex,
        int rewardEndIndex) {
      super(plugin, new ItemPage(plugin, pageSize, rewards, rewardStartIndex, rewardEndIndex),
          "<green>Sounds", null, null);
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
                              new InnerMenu(getPlugin(), 45,
                                  sounds.stream().filter(sound -> sound.name().toLowerCase().contains(s.toLowerCase()))
                                      .toList())
                                  .getInventory()))),
                      getPlugin(), player -> player.openInventory(getInventory()))
                  .open((Player) event.getWhoClicked());
            }
          });
    }

    @Override
    public Optional<Supplier<InnerMenu>> getNextPageMenu() {
      // there is a next page if the current range upper bound is smaller than the end
      // of the list
      ItemPage itemPage = getPage();
      if (itemPage.rewardEndIndex < itemPage.rewards.size()) {
        return Optional.of(() -> new InnerMenu(
            getPlugin(),
            getPageSize(),
            itemPage.rewards,
            itemPage.rewardEndIndex,
            Math.min(itemPage.rewards.size(), itemPage.rewardEndIndex + getPageSize())));
      } else {
        return Optional.empty();
      }
    }

    @Override
    public Optional<Supplier<InnerMenu>> getPreviousPageMenu() {
      // there is a previous page if we didn't start at 0
      ItemPage itemPage = getPage();
      if (itemPage.rewardStartIndex > 0) {
        return Optional.of(() -> new InnerMenu(
            getPlugin(),
            getPageSize(),
            itemPage.rewards,
            Math.max(0, itemPage.rewardStartIndex - getPageSize()),
            Math.min(itemPage.rewardStartIndex, itemPage.rewards.size())));
      } else {
        return Optional.empty();
      }
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
      private final List<Sound> rewards;

      private ItemPage(ComMiniPlugin plugin, int pageSize, List<Sound> rewards, int rewardStartIndex,
          int rewardEndIndex) {
        super(plugin, pageSize);
        this.rewardStartIndex = rewardStartIndex;
        this.rewardEndIndex = rewardEndIndex;
        this.rewards = rewards;
      }

      @Override
      public void onOpen(InventoryOpenEvent event) {
        for (int slot = 0; slot < getInventory().getSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
          Sound key = rewards.get(rewardStartIndex + slot);
          var m = getMaterial(key);
          setButton(slot,
              new ItemButton<>(
                  new ItemBuilder(m == null || m.isAir() || !m.isItem() ? Material.BEDROCK : m).addLore("")
                      .addLore(key.name())
                      .build()) {
                @Override
                public void onClick(MenuHolder<?> holder, InventoryClickEvent event) {
                  ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), key, 1, 1);
                }
              });
        }
      }

      private Material getMaterial(Sound sound) {
        String name = sound.name();
        var split = new ArrayList<>(List.of(name.split("_")));
        var finalname = split.get(1).toLowerCase();
        return switch (split.remove(0)) {
          case "AMBIENT" -> Material.STONE;
          case "BLOCK" -> {
            while (split.size() > 0) {
              try {
                yield Material.valueOf(String.join("_", split));
              } catch (IllegalArgumentException e) {
                split.remove(split.size() - 1);
              }
            }
            var contain = materials.stream().filter(s -> s.name().toLowerCase().contains(finalname)).findFirst();
            if (contain.isPresent()) {
              yield contain.get();
            }
            yield Material.BEDROCK;
          }
          case "ENTITY" -> {
            while (split.size() > 0) {
              var str = String.join("_", split);
              try {
                yield Material.valueOf(str + "_SPAWNN_EGG");
              } catch (IllegalArgumentException e) {
                try {
                  yield Material.valueOf(str);
                } catch (IllegalArgumentException ignore) {
                }
                split.remove(split.size() - 1);
              }
            }
            var contain = materials.stream().filter(s -> s.name().toLowerCase().contains(finalname)).findFirst();
            if (contain.isPresent()) {
              yield contain.get();
            }
            yield Material.BEDROCK;
          }
          default -> Material.BEDROCK;
        };
      }

      @Override
      public void onClose(InventoryCloseEvent event) {
        clearButtons();
      }
    }
  }
}
