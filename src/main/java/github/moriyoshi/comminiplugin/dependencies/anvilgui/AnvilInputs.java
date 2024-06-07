package github.moriyoshi.comminiplugin.dependencies.anvilgui;

import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.StateSnapshot;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AnvilInputs {

  private AnvilInputs() {}

  public static AnvilGUI.Builder getString(
      @NotNull Plugin plugin,
      @NotNull Object title,
      @NotNull BiFunction<String, StateSnapshot, List<AnvilGUI.ResponseAction>> func) {
    return getInput(
        plugin, title, (s, state) -> Optional.of(s), (s, state) -> Collections.emptyList(), func);
  }

  public static <T> AnvilGUI.Builder getInput(
      @NotNull Plugin plugin,
      @NotNull Object title,
      @NotNull BiFunction<String, StateSnapshot, Optional<T>> toClazz,
      @NotNull BiFunction<T, StateSnapshot, List<AnvilGUI.ResponseAction>> func) {
    return getInput(
        plugin,
        title, toClazz,
        (s, state) -> Collections.emptyList(),
        func);
  }

  public static <T> AnvilGUI.Builder getInput(
      @NotNull Plugin plugin,
      @NotNull Object title,
      @NotNull BiFunction<String, StateSnapshot, Optional<T>> toClazz,
      @NotNull BiFunction<String, StateSnapshot, List<AnvilGUI.ResponseAction>> non,
      @NotNull BiFunction<T, StateSnapshot, List<AnvilGUI.ResponseAction>> func) {
    return new AnvilGUI.Builder()
        .plugin(plugin)
        .jsonTitle(JSONComponentSerializer.json().serialize(Util.mm(title)))
        .itemLeft(new ItemBuilder(Material.PAPER).name("").build())
        .onClick(
            (slot, state) -> {
              if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
              }

              String input = state.getText();
              Optional<T> apply = toClazz.apply(input, state);
              if (apply.isEmpty()) {
                return non.apply(input, state);
              }
              return func.apply(apply.get(), state);
            });
  }

  public static AnvilGUI.Builder postClose(
      @NotNull AnvilGUI.Builder builder, Plugin plugin, Consumer<StateSnapshot> consumer) {
    builder.onClose(
        state ->
            plugin
                .getServer()
                .getScheduler()
                .runTask(
                    plugin,
                    () -> {
                      InventoryType type = state.getPlayer().getOpenInventory().getType();
                      if (type == InventoryType.CRAFTING || type == InventoryType.CREATIVE) {
                        consumer.accept(state);
                      }
                    }));
    return builder;
  }
}
