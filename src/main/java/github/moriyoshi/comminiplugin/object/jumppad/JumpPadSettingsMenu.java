package github.moriyoshi.comminiplugin.object.jumppad;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.command.AllSoundCommand;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ToggleButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ItemInputMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.tuple.Pair;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI;

public final class JumpPadSettingsMenu extends MenuHolder<ComMiniPlugin> {

  private boolean isIncludeLinked = true;

  private final JumpPadBlock jumpPadBlock;

  public JumpPadSettingsMenu(JumpPadBlock jumpPadBlock) {
    super(ComMiniPlugin.getPlugin(), 27, "<red>Settings Jump Pad");
    setButton(26, new ToggleButton<>(
        new ItemBuilder(Material.NETHER_STAR).name("<green>繋がっているジャンプパッド全てに適用中")
            .lore("<gray>クリックで切り替え").build(),
        isIncludeLinked) {

      @Override
      protected ItemStack enable(ItemStack stack) {
        return new ItemBuilder(getIcon()).name("<green>繋がっているジャンプパッド全てに適用中")
            .build();
      }

      @Override
      protected ItemStack disable(ItemStack stack) {
        return new ItemBuilder(getIcon()).name("<red>個別に適用中").build();
      }

      @Override
      public void afterToggle(MenuHolder<?> menuHolder, InventoryClickEvent event) {
        isIncludeLinked = !isIncludeLinked;
      }
    });

    setButton(11, new RedirectItemButton<>(
        new ItemBuilder(jumpPadBlock.getMaterial()).name("<green>material: " + jumpPadBlock.getMaterial().name())
            .build(),
        (holder, event) -> new ItemInputMenu<>(holder.getPlugin(), "<red>please input block material", (item, e) -> {
          if (item == null || item.isEmpty()) {
            ComMiniPrefix.SYSTEM.send(e.getWhoClicked(), "<red>please input block material (ignore air)");
            return;
          }
          val material = item.getType();
          if (material.isEmpty() || !material.isBlock()) {
            ComMiniPrefix.SYSTEM.send(e.getWhoClicked(), "<red>please input block material (ignore air)");
            return;
          }
          setConsumer(block -> {
            block.setMaterial(material);
            block.getBlock().setType(material);
          }, isIncludeLinked);
        }, (item, e) -> e.getWhoClicked().openInventory(JumpPadSettingsMenu.this.getInventory())).getInventory()
    ));

    setButton(12,
        new ItemButton<>(new ItemBuilder(Material.TNT).name("<red>power: " + jumpPadBlock.getPower()).build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(AnvilInputs
                .getInput(ComMiniPlugin.getPlugin(), "<red>60 >= power >= 10",
                    (str, state) -> {
                      try {
                        return Optional.of(Float.parseFloat(str)).filter(f -> 60 >= f && f >= 10);
                      } catch (NumberFormatException e) {
                        return Optional.empty();
                      }
                    }, (str, state) -> Collections.emptyList(), (value, state) -> {
                      setConsumer(block -> block.setPower(value), isIncludeLinked);
                      return List.of(
                          AnvilGUI.ResponseAction.openInventory(new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                    }),
                getPlugin(), state -> state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory())
                )
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(13,
        new ItemButton<>(
            new ItemBuilder(Material.ENDER_EYE).name("<yellow>angle: " + jumpPadBlock.getAngel()).build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(AnvilInputs.getInput(ComMiniPlugin.getPlugin(),
                "<yellow>90 >= angle >= -90",
                (str, state) -> {
                  try {
                    return Optional.of(Float.parseFloat(str)).filter(f -> 90 >= f && f >= -90);
                  } catch (NumberFormatException e) {
                    return Optional.empty();
                  }
                }, (str, state) -> Collections.emptyList(), (value, state) -> {
                  setConsumer(block -> block.setAngel(value), isIncludeLinked);
                  return List.of(
                      AnvilGUI.ResponseAction.openInventory(new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                }), getPlugin(), state -> state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(14,
        new RedirectItemButton<>(
            new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("<blue>sound: " + jumpPadBlock.getSound().name()).build(),
            (holder, event) -> new SelectSound(jumpPadBlock, isIncludeLinked).getInventory()));

    setButton(15,
        new ItemButton<>(
            new ItemBuilder(Material.BELL).name("<aqua>particle: " + jumpPadBlock.getParticle()).build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(AnvilInputs.getInput(ComMiniPlugin.getPlugin(),
                "<aqua>ignore block",
                (str, state) -> {
                  if (str.isEmpty()) {
                    return Optional.of(Particle.BLOCK);
                  }
                  try {
                    return Optional.of(Particle.valueOf(str));
                  } catch (IllegalArgumentException e) {
                    return Optional.empty();
                  }
                }, (str, state) -> Collections.emptyList(), (value, state) -> {
                  setConsumer(block -> block.setParticle(value == Particle.BLOCK ? null : value), isIncludeLinked);
                  return List.of(
                      AnvilGUI.ResponseAction.openInventory(new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                }), getPlugin(), state -> state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });
    this.jumpPadBlock = jumpPadBlock;
  }

  public static void linkedConsumer(Location loc, Consumer<JumpPadBlock> jumppadConsumer,
      Set<Location> set) {
    IntStream.range(0, 4).boxed()
        .map(i -> Pair.of(Math.round(Math.cos(i * Math.PI / 2)),
            Math.round(Math.sin(i * Math.PI / 2))))
        .forEach(pair -> {
          val b = loc.clone().add(pair.getFirst(), 0, pair.getSecond());
          if (set.contains(b)) {
            return;
          }
          if (CustomBlock.isCustomBlock(b, JumpPadBlock.class)) {
            jumppadConsumer.accept(CustomBlock.getCustomBlock(b, JumpPadBlock.class));
            set.add(b);
            linkedConsumer(b, jumppadConsumer, set);
          }
        });
  }

  public static void setConsumer(JumpPadBlock jumpPadBlock, Consumer<JumpPadBlock> jumppadConsumer,
      boolean isIncludeLinked) {
    if (!isIncludeLinked) {
      jumppadConsumer.accept(jumpPadBlock);
      return;
    }
    linkedConsumer(jumpPadBlock.getBlock().getLocation(), jumppadConsumer, new HashSet<>());
  }

  public void setConsumer(Consumer<JumpPadBlock> jumppadConsumer, boolean isIncludeLinked) {
    setConsumer(jumpPadBlock, jumppadConsumer, isIncludeLinked);
  }

  public static final class SelectSound extends ListMenu<Sound> {

    private JumpPadBlock jumpPadBlock;
    private boolean isIncludeLinked;

    public SelectSound(JumpPadBlock jumpPadBlock, boolean isIncludeLinked) {
      super("<red>select sound", 45,
          Stream.of(Sound.values()).toList(),
          sound -> {
            val m = AllSoundCommand.getMaterial(sound);
            return new ItemButton<>(
                new ItemBuilder(m == null || m.isEmpty() || !m.isItem() ? Material.BEDROCK : m).addLore("")
                    .addLore(sound.name()).build()) {
              @Override
              public void onClick(@NotNull final MenuHolder<ComMiniPlugin> holder,
                  @NotNull final InventoryClickEvent event) {
                setConsumer(jumpPadBlock, jump -> jump.setSound(sound), isIncludeLinked);
                new JumpPadSettingsMenu(jumpPadBlock).openInv(event.getWhoClicked());
              }
            };
          });
    }

    public SelectSound(ComMiniPlugin plugin, String title, int pageSize,
        List<Sound> rewards,
        int rewardStartIndex, int rewardEndIndex,
        Function<Sound, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public @NotNull Optional<Supplier<ListMenu<Sound>>> getDefaultMenu() {
      return Optional.of(() -> new SelectSound(jumpPadBlock, isIncludeLinked));
    }

    @Override
    public Optional<BiPredicate<String, Sound>> getSerachMethod() {
      return Optional.of((key, sound) -> sound.name().toLowerCase().contains(key.toLowerCase()));
    }

  }
}