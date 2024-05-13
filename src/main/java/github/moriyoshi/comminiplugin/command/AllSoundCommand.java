package github.moriyoshi.comminiplugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

/**
 * AllSoundCommand
 */
public class AllSoundCommand extends CommandAPICommand {

  public AllSoundCommand() {
    super("allsound");
    executesPlayer((p, args) -> {
      new AllSoundMenu().openInv(p);
    });
  }

  private static final List<Sound> sounds = new ArrayList<>(Arrays.asList(Sound.values()));
  private static final List<Material> materials = new ArrayList<>(Arrays.asList(Material.values()));

  private static final class AllSoundMenu extends ListMenu<Sound> {
    public AllSoundMenu() {
      super("<green>Sounds", 45, sounds, (sound) -> {
        var m = getMaterial(sound);
        return new ItemButton<>(
            new ItemBuilder(m == null || m.isAir() || !m.isItem() ? Material.BEDROCK : m).addLore(
                "")
                .addLore(sound.name())
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
            var p = ((Player) event.getWhoClicked());
            p.playSound(event.getWhoClicked().getLocation(), sound, 1, 1);
          }
        };
      });
    }

    private static Material getMaterial(Sound sound) {
      String name = sound.name();
      var split = new ArrayList<>(List.of(name.split("_")));
      var finalname = split.get(1).toLowerCase();
      return switch (split.remove(0)) {
        case "AMBIENT" -> Material.STONE;
        case "BLOCK" -> {
          while (!split.isEmpty()) {
            try {
              yield Material.valueOf(String.join("_", split));
            } catch (IllegalArgumentException e) {
              split.remove(split.size() - 1);
            }
          }
          yield materials.stream().filter(s -> s.name().toLowerCase().contains(finalname))
              .findFirst().orElse(Material.BEDROCK);
        }
        case "ENTITY" -> {
          while (!split.isEmpty()) {
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
          yield materials.stream().filter(s -> s.name().toLowerCase().contains(finalname))
              .findFirst().orElse(Material.BEDROCK);
        }
        default -> Material.BEDROCK;
      };
    }

    public AllSoundMenu(ComMiniPlugin plugin, String title, int pageSize, List<Sound> rewards, int rewardStartIndex,
        int rewardEndIndex, Function<Sound, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public Optional<ListMenu<Sound>> getDefaultMenu() {
      return Optional.of(new AllSoundMenu());
    }

    @Override
    public Optional<Function<List<Sound>, ListMenu<Sound>>> getNewRewadsMenu() {
      return Optional
          .of((r -> new AllSoundMenu(getPlugin(), title, getPageSize(), r, 0, Math.min(r.size(), getPageSize()),
              function)));
    }

    @Override
    public Optional<BiPredicate<String, Sound>> getSerachMethod() {
      return Optional.of((key, sound) -> sound.name().toLowerCase().contains(key.toLowerCase()));
    }

  }

}
