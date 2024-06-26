package github.moriyoshi.comminiplugin.object.jumppad;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.command.AllSoundCommand;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.SwitchButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ItemInputMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.ListMenu;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.JumpState;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public final class JumpPadSettingsMenu extends MenuHolder<ComMiniPlugin> {

  private final JumpPadBlock jumpPadBlock;
  private boolean isIncludeLinked = true;

  public JumpPadSettingsMenu(JumpPadBlock jumpPadBlock) {
    super(ComMiniPlugin.getPlugin(), 27, "<red>Settings Jump Pad");
    setButton(
        26,
        new SwitchButton<>(
            new ItemBuilder(Material.NETHER_STAR)
                .name("<green>繋がっているジャンプパッド全てに適用中")
                .lore("<gray>クリックで切り替え")
                .build(),
            new ItemBuilder(Material.NETHER_STAR)
                .name("<red>個別に適用中")
                .lore("<gray>クリックで切り替え")
                .build(),
            isIncludeLinked) {
          @Override
          public void afterChange(MenuHolder<?> holder, InventoryClickEvent event) {
            isIncludeLinked = !isIncludeLinked;
          }
        });

    setButton(
        11,
        new RedirectItemButton<>(
            new ItemBuilder(jumpPadBlock.getMaterial())
                .name("<green>material: " + jumpPadBlock.getMaterial().name())
                .build(),
            (holder, event) ->
                new ItemInputMenu<>(
                        holder.getPlugin(),
                        "<red>please input block material",
                        (item, e) -> {
                          if (item == null || item.isEmpty()) {
                            ComMiniPlugin.SYSTEM.send(
                                e.getWhoClicked(),
                                "<red>please input block material (ignore air,structor void if you"
                                    + " want to use it.)");
                            return;
                          }
                          val material = item.getType();
                          if (material.isEmpty() || !material.isBlock()) {
                            ComMiniPlugin.SYSTEM.send(
                                e.getWhoClicked(),
                                "<red>please input block material (ignore air,structor void if you"
                                    + " want to use it.)");
                            return;
                          }
                          setConsumer(block -> block.setMaterial(material), isIncludeLinked);
                        },
                        (item, e) -> JumpPadSettingsMenu.this.openInv(e.getWhoClicked()))
                    .getInventory()));

    setButton(
        12,
        new ItemButton<>(
            new ItemBuilder(Material.TNT).name("<red>power: " + jumpPadBlock.getPower()).build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(
                    AnvilInputs.getFloat(
                        ComMiniPlugin.getPlugin(),
                        "<red>200 >= power >= 10",
                        f -> 200 >= f && f >= 10,
                        (value, state) -> {
                          setConsumer(block -> block.setPower(value), isIncludeLinked);
                          return List.of(
                              AnvilGUI.ResponseAction.openInventory(
                                  new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                        }),
                    getPlugin(),
                    state ->
                        state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(
        13,
        new ItemButton<>(
            new ItemBuilder(Material.ENDER_EYE)
                .name("<yellow>angle: " + jumpPadBlock.getAngel())
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(
                    AnvilInputs.getFloat(
                        ComMiniPlugin.getPlugin(),
                        "<yellow>90 >= angle >= -90",
                        f -> 90 >= f && f >= -90,
                        (value, state) -> {
                          setConsumer(block -> block.setAngel(value), isIncludeLinked);
                          return List.of(
                              AnvilGUI.ResponseAction.openInventory(
                                  new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                        }),
                    getPlugin(),
                    state ->
                        state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(
        14,
        new RedirectItemButton<>(
            new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("<blue>sound: " + jumpPadBlock.getSound().name())
                .build(),
            (holder, event) -> new SelectSound(jumpPadBlock, isIncludeLinked).getInventory()));

    setButton(
        15,
        new ItemButton<>(
            new ItemBuilder(Material.BELL)
                .name("<aqua>particle: " + jumpPadBlock.getParticle())
                .lore("<gray>空白でパーティクルを消します")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(
                    AnvilInputs.getInput(
                        ComMiniPlugin.getPlugin(),
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
                        },
                        (value, state) -> {
                          setConsumer(
                              block -> block.setParticle(value == Particle.BLOCK ? null : value),
                              isIncludeLinked);
                          return List.of(
                              AnvilGUI.ResponseAction.openInventory(
                                  new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                        }),
                    getPlugin(),
                    state ->
                        state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(
        16,
        new ItemButton<>(
            new ItemBuilder(Material.ENDER_EYE)
                .name("<dark_purple>direction: " + jumpPadBlock.getDirection())
                .lore(
                    "<gray>-1でプレイヤーの方向に飛びます",
                    "<gray>0 or 360 represents the +z",
                    "<gray>180 represents the -z",
                    "<gray>90 represents the -x",
                    "<gray>270 represents the +x")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(
                    AnvilInputs.getFloat(
                        ComMiniPlugin.getPlugin(),
                        "<dark_purple>360 >= direction >= -1",
                        f -> 360 >= f && f >= -1,
                        (value, state) -> {
                          setConsumer(block -> block.setDirection(value), isIncludeLinked);
                          return List.of(
                              AnvilGUI.ResponseAction.openInventory(
                                  new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                        }),
                    getPlugin(),
                    state ->
                        state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    setButton(
        9,
        new ItemButton<>(
            new ItemBuilder(Material.FIREWORK_ROCKET)
                .name("<white>state: " + jumpPadBlock.getState().name())
                .lore("<gray>FREE: 自由に動けます", "<gray>DOWN: 下方向の時から動けます", "<gray>FIXED: 完全に動けません")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            AnvilInputs.postClose(
                    AnvilInputs.getString(
                        ComMiniPlugin.getPlugin(),
                        "<white>FREE or DOWN or FIXED",
                        (value, state) -> {
                          try {
                            setConsumer(
                                block -> block.setState(JumpState.valueOf(value.toUpperCase())),
                                isIncludeLinked);
                            return List.of(
                                AnvilGUI.ResponseAction.openInventory(
                                    new JumpPadSettingsMenu(jumpPadBlock).getInventory()));
                          } catch (IllegalArgumentException e) {
                            return Collections.emptyList();
                          }
                        }),
                    getPlugin(),
                    state ->
                        state.getPlayer().openInventory(JumpPadSettingsMenu.this.getInventory()))
                .open((Player) event.getWhoClicked());
          }
        });

    this.jumpPadBlock = jumpPadBlock;
  }

  public static void linkedConsumer(
      Location loc, Consumer<JumpPadBlock> jumppadConsumer, Set<Location> set) {
    IntStream.range(0, 4)
        .boxed()
        .map(
            i ->
                Pair.of(
                    Math.round(Math.cos(i * Math.PI / 2)), Math.round(Math.sin(i * Math.PI / 2))))
        .forEach(
            pair -> {
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

  public static void setConsumer(
      JumpPadBlock jumpPadBlock, Consumer<JumpPadBlock> jumppadConsumer, boolean isIncludeLinked) {
    jumppadConsumer.accept(jumpPadBlock);
    if (isIncludeLinked) {
      linkedConsumer(jumpPadBlock.getBlock().getLocation(), jumppadConsumer, new HashSet<>());
    }
  }

  public void setConsumer(Consumer<JumpPadBlock> jumppadConsumer, boolean isIncludeLinked) {
    setConsumer(jumpPadBlock, jumppadConsumer, isIncludeLinked);
  }

  public static final class SelectSound extends ListMenu<ComMiniPlugin, Sound, SelectSound> {

    private JumpPadBlock jumpPadBlock;
    private boolean isIncludeLinked;

    public SelectSound(JumpPadBlock jumpPadBlock, boolean isIncludeLinked) {
      super(
          ComMiniPlugin.getPlugin(),
          "<red>select sound",
          45,
          Stream.of(Sound.values()).toList(),
          sound -> {
            val m = AllSoundCommand.getMaterial(sound);
            return new ItemButton<>(
                new ItemBuilder(m == null || m.isEmpty() || !m.isItem() ? Material.BEDROCK : m)
                    .addLore("")
                    .addLore(sound.name())
                    .build()) {
              @Override
              public void onClick(
                  @NotNull final MenuHolder<ComMiniPlugin> holder,
                  @NotNull final InventoryClickEvent event) {
                setConsumer(jumpPadBlock, jump -> jump.setSound(sound), isIncludeLinked);
                new JumpPadSettingsMenu(jumpPadBlock).openInv(event.getWhoClicked());
              }
            };
          });
    }

    public SelectSound(
        ComMiniPlugin plugin,
        String title,
        int pageSize,
        List<Sound> rewards,
        int rewardStartIndex,
        int rewardEndIndex,
        Function<Sound, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
      super(plugin, title, pageSize, rewards, rewardStartIndex, rewardEndIndex, function);
    }

    @Override
    public @NotNull Optional<Supplier<SelectSound>> getDefaultMenu() {
      return Optional.of(() -> new SelectSound(jumpPadBlock, isIncludeLinked));
    }

    @Override
    public Optional<Function<String, List<Sound>>> getMenuBySearch() {
      return Optional.of(
          str -> {
            val upper = str.toUpperCase();
            return Stream.of(Sound.values()).filter(key -> key.name().contains(upper)).toList();
          });
    }
  }
}
