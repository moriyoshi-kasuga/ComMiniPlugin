package github.moriyoshi.comminiplugin.dependencies.ui.menu;

import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ListMenu<P extends Plugin, T, Impl extends ListMenu<P, T, Impl>> extends PageMenu<P> {
  protected final ItemStack goToFirstPage =
      new ItemBuilder(Material.ENDER_PEARL).name("<green>最初のページにもどる").build();
  protected final String title;
  protected final List<T> rewards;
  protected final int rewardStartIndex, rewardEndIndex;
  protected Function<T, MenuButton<MenuHolder<P>>> function;

  public ListMenu(
      P plugin,
      String title,
      int pageSize,
      List<T> rewards,
      Function<T, MenuButton<MenuHolder<P>>> function) {
    this(plugin, title, pageSize, rewards, 0, Math.min(rewards.size(), pageSize), function);
  }

  public ListMenu(P plugin, String title, int pageSize, List<T> rewards) {
    this(plugin, title, pageSize, rewards, 0, Math.min(rewards.size(), pageSize));
  }

  public ListMenu(
      P plugin,
      String title,
      int pageSize,
      List<T> rewards,
      int rewardStartIndex,
      int rewardEndIndex,
      Function<T, MenuButton<MenuHolder<P>>> function) {
    super(plugin, new MenuHolder<>(plugin, pageSize), title, null, null);
    this.title = title;
    this.rewards = rewards;
    this.rewardStartIndex = rewardStartIndex;
    this.rewardEndIndex = rewardEndIndex;
    this.function = function;
  }

  public ListMenu(
      P plugin,
      String title,
      int pageSize,
      List<T> rewards,
      int rewardStartIndex,
      int rewardEndIndex) {
    super(plugin, new MenuHolder<>(plugin, pageSize), title, null, null);
    this.title = title;
    this.rewards = rewards;
    this.rewardStartIndex = rewardStartIndex;
    this.rewardEndIndex = rewardEndIndex;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MenuHolder<P> getPage() {
    // we know the GuiInventoryHolder of the page is always a MenuHolder since we
    // always create it ourselves
    return (MenuHolder<P>) super.getPage();
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    // setup rewards
    for (int slot = 0; slot < getPageSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
      getPage().setButton(slot, function.apply(rewards.get(rewardStartIndex + slot)));
    }
    getDefaultMenu()
        .ifPresent(
            menu -> {
              setButton(
                  getPageSize(),
                  new RedirectItemButton<>(goToFirstPage, () -> menu.get().getInventory()));
              getMenuBySearch()
                  .ifPresent(
                      func ->
                          setButton(
                              getPageSize() + 8,
                              new ItemButton<>(
                                  new ItemBuilder(Material.BOOK).name("<aqua>クリックで文字検索").build()) {
                                @Override
                                public void onClick(
                                    @NotNull MenuHolder<?> holder,
                                    @NotNull InventoryClickEvent event) {
                                  AnvilInputs.postClose(
                                          AnvilInputs.getString(
                                              getPlugin(),
                                              "<aqua>文字で検索",
                                              (s, completion) ->
                                                  List.of(
                                                      ResponseAction.openInventory(
                                                          getNewRewardsMenu(func.apply(s))
                                                              .getInventory()))),
                                          getPlugin(),
                                          state -> state.getPlayer().openInventory(getInventory()))
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

  public Optional<Function<String, List<T>>> getMenuBySearch() {
    return Optional.empty();
  }

  public Impl getNewRewardsMenu(List<T> list) {
    return getNewMenu(list, 0, Math.min(list.size(), getPageSize()));
  }

  @NotNull
  public Optional<Supplier<Impl>> getDefaultMenu() {
    return Optional.empty();
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    getPage().clearButtons(); // help gc

    // required
    super.onClose(event);
  }

  @SuppressWarnings("unchecked")
  public Impl getNewMenu(List<T> list, int rewardStartIndex, int rewardEndIndex) {
    try {
      val constructor =
          getClass()
              .getDeclaredConstructor(
                  getPlugin().getClass(),
                  String.class,
                  int.class,
                  List.class,
                  int.class,
                  int.class,
                  Function.class);
      constructor.setAccessible(true);
      final Impl newMenu =
          (Impl)
              constructor.newInstance(
                  getPlugin(),
                  title,
                  getPageSize(),
                  list,
                  rewardStartIndex,
                  rewardEndIndex,
                  function);
      newMenu.toNew((Impl) this);
      return newMenu;
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new RuntimeException(
          "Failed to create new " + getClass().getSimpleName() + " of index", e);
    }
  }

  @Override
  public Optional<Supplier<Impl>> getNextPageMenu() {
    // there is a next page if the current range upper bound is smaller than the end
    // of the list
    if (rewardEndIndex < rewards.size()) {
      return Optional.of(
          () ->
              getNewMenu(
                  rewards,
                  rewardEndIndex,
                  Math.min(rewards.size(), rewardEndIndex + getPageSize())));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Supplier<Impl>> getPreviousPageMenu() {
    // there is a previous page if we didn't start 0
    if (rewardStartIndex > 0) {
      return Optional.of(
          () ->
              getNewMenu(
                  rewards,
                  Math.max(0, rewardStartIndex - getPageSize()),
                  Math.min(rewardStartIndex, rewards.size())));
    } else {
      return Optional.empty();
    }
  }

  public void toNew(Impl old) {}
}
