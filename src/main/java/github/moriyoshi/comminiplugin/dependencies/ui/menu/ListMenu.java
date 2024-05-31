package github.moriyoshi.comminiplugin.dependencies.ui.menu;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;

public class ListMenu<T> extends PageMenu<ComMiniPlugin> {
  protected final ItemStack goToFirstPage = new ItemBuilder(Material.ENDER_PEARL).name("<green>最初のページにもどる").build();
  protected final String title;
  protected final List<T> rewards;
  protected final int rewardStartIndex, rewardEndIndex;
  protected final Function<T, MenuButton<MenuHolder<ComMiniPlugin>>> function;

  public ListMenu(String title, int pageSize, List<T> rewards,
      Function<T, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
    this(ComMiniPlugin.getPlugin(), title, pageSize, rewards, 0, Math.min(rewards.size(), pageSize), function);
  }

  public ListMenu(ComMiniPlugin plugin, String title, int pageSize, List<T> rewards, int rewardStartIndex,
      int rewardEndIndex, Function<T, MenuButton<MenuHolder<ComMiniPlugin>>> function) {
    super(plugin, new MenuHolder<>(plugin, pageSize), title, null, null);
    this.title = title;
    this.rewards = rewards;
    this.rewardStartIndex = rewardStartIndex;
    this.rewardEndIndex = rewardEndIndex;
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MenuHolder<ComMiniPlugin> getPage() {
    // we know the GuiInventoryHolder of the page is always a MenuHolder since we
    // always create it ourselves
    return (MenuHolder<ComMiniPlugin>) super.getPage();
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    // setup rewards
    for (int slot = 0; slot < getPageSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
      getPage().setButton(slot, function.apply(rewards.get(rewardStartIndex + slot)));
    }
    getDefaultMenu().ifPresent(menu -> {
      setButton(getPageSize(), new RedirectItemButton<>(goToFirstPage, () -> menu.get().getInventory()));
      getSerachMethod().ifPresent(method -> setButton(getPageSize() + 8,
          new ItemButton<>(new ItemBuilder(Material.BOOK).name("<aqua>クリックで文字検索").build()) {
            @Override
            public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
              AnvilInputs.postClose(
                  AnvilInputs.getInput(getPlugin(), "<aqua>文字で検索", (t, u) -> t,
                      (s, completion) -> List.of(ResponseAction.openInventory(
                          getNewRewadsMenu(rewards.stream().filter(key -> method.test(s, key)).toList())
                              .getInventory()))),
                  ComMiniPlugin.getPlugin(), state -> state.getPlayer().openInventory(getInventory())
                  )
                  .open((Player) event.getWhoClicked());
            }
          }));
    });

    // required for the page to even work
    super.onOpen(event);
  }

  @Override
  public void onClick(InventoryClickEvent clickEvent) {
    super.onClick(clickEvent);
  }

  public Optional<BiPredicate<String, T>> getSerachMethod() {
    return Optional.empty();
  }

  public ListMenu<T> getNewRewadsMenu(List<T> list) {
    return getNewMenu(list, 0, Math.min(list.size(), getPageSize()));
  }

  @NotNull
  public Optional<Supplier<ListMenu<T>>> getDefaultMenu() {
    return Optional.empty();
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    getPage().clearButtons(); // help gc

    // required
    super.onClose(event);
  }

  @SuppressWarnings("unchecked")
  public ListMenu<T> getNewMenu(List<T> list, int rewardStartIndex, int rewardEndIndex) {
    try {
      return getClass()
          .getDeclaredConstructor(ComMiniPlugin.class, String.class, int.class, List.class, int.class, int.class,
              Function.class)
          .newInstance(getPlugin(), title, getPageSize(), list, rewardStartIndex, rewardEndIndex, function);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("Failed to create new " + getClass().getSimpleName() + " of index", e);
    }
  }

  @Override
  public Optional<Supplier<ListMenu<T>>> getNextPageMenu() {
    // there is a next page if the current range upper bound is smaller than the end
    // of the list
    if (rewardEndIndex < rewards.size()) {
      return Optional
          .of(() -> getNewMenu(rewards, rewardEndIndex, Math.min(rewards.size(), rewardEndIndex + getPageSize())));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Supplier<ListMenu<T>>> getPreviousPageMenu() {
    // there is a previous page if we didn't start 0
    if (rewardStartIndex > 0) {
      return Optional.of(() -> getNewMenu(rewards, Math.max(0, rewardStartIndex - getPageSize()),
          Math.min(rewardStartIndex, rewards.size())));
    } else {
      return Optional.empty();
    }
  }
}
