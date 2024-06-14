package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.SwitchButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.val;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class AllSoundCommand extends CommandAPICommand {

  // TODO: もっとちゃんとアイテム適用するようにする
  public static Material getMaterial(final Sound sound) {
    final String name = sound.name();
    val materials = Arrays.asList(Material.values());
    val split = new ArrayList<>(Arrays.asList(name.split("_")));
    val finalName = split.getFirst().toLowerCase();
    return switch (split.removeFirst()) {
      case "AMBIENT" -> Material.STONE;
      case "BLOCK" -> {
        while (!split.isEmpty()) {
          try {
            yield Material.valueOf(String.join("_", split));
          } catch (final IllegalArgumentException e) {
            split.removeLast();
          }
        }
        yield materials.stream()
            .filter(s -> s.name().toLowerCase().contains(finalName))
            .findFirst()
            .orElse(Material.BEDROCK);
      }
      case "ENTITY" -> {
        while (!split.isEmpty()) {
          val str = String.join("_", split);
          try {
            yield Material.valueOf(str + "_SPAWN_EGG");
          } catch (final IllegalArgumentException e) {
            try {
              yield Material.valueOf(str);
            } catch (final IllegalArgumentException ignore) {
            }
            split.removeLast();
          }
        }
        yield materials.stream()
            .filter(s -> s.name().toLowerCase().contains(finalName))
            .findFirst()
            .orElse(Material.BEDROCK);
      }
      default -> Material.BEDROCK;
    };
  }

  public static final class AllSoundMenu extends ListMenu<ComMiniPlugin, Sound, AllSoundMenu> {

    private boolean isSelf = true;

    public AllSoundMenu() {
      super(ComMiniPlugin.getPlugin(), "<green>Sounds", 45, Arrays.asList(Sound.values()));
      this.function =
          (sound) -> {
            val m = getMaterial(sound);
            return new ItemButton<>(
                new ItemBuilder(m == null || m.isEmpty() || !m.isItem() ? Material.BEDROCK : m)
                    .addLore("")
                    .addLore(sound.name())
                    .build()) {
              @Override
              public void onClick(
                  @NotNull final MenuHolder<ComMiniPlugin> holder,
                  @NotNull final InventoryClickEvent event) {
                val p = ((Player) event.getWhoClicked());
                if (isSelf) {
                  p.playSound(event.getWhoClicked().getLocation(), sound, 1, 1);
                } else {
                  p.getWorld().playSound(event.getWhoClicked().getLocation(), sound, 1, 1);
                }
              }
            };
          };
    }

    public AllSoundMenu(
        final ComMiniPlugin plugin,
        final String title,
        final int pageSize,
        final List<Sound> rewards,
        final int rewardStartIndex,
        final int rewardEndIndex,
        final Function<Sound, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public void onOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
      setButton(
          48,
          new ItemButton<>(new ItemBuilder(Material.BUCKET).name("<gray>音を消す").build()) {
            @Override
            public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent e) {
              e.getWhoClicked().stopSound(SoundStop.all());
            }
          });
      setButton(
          50,
          new SwitchButton<>(
              new ItemBuilder(Material.NETHER_STAR)
                  .name("<red>自分のみに聞かせています")
                  .lore("<gray>クリックで切り替え")
                  .build(),
              new ItemBuilder(Material.NETHER_STAR)
                  .name("<green>近くのプレイヤーに聞かせています")
                  .lore("<gray>クリックで切り替え")
                  .build(),
              isSelf) {
            @Override
            public void afterChange(MenuHolder<?> holder, InventoryClickEvent event) {
              isSelf = !isSelf;
            }
          });
      super.onOpen(event);
    }

    @Override
    public @NotNull Optional<Supplier<AllSoundMenu>> getDefaultMenu() {
      return Optional.of(AllSoundMenu::new);
    }

    @Override
    public Optional<Function<String, List<Sound>>> getMenuBySearch() {
      return Optional.of(
          str -> {
            val upper = str.toUpperCase();
            return Arrays.stream(Sound.values())
                .filter(sound -> sound.name().contains(upper))
                .toList();
          });
    }

    @Override
    public void toNew(AllSoundMenu old) {
      this.isSelf = old.isSelf;
    }
  }

  public AllSoundCommand() {
    super("allsound");
    executesPlayer(
        (p, args) -> {
          new AllSoundMenu().openInv(p);
        });
  }
}
