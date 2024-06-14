package github.moriyoshi.comminiplugin.lib.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import github.moriyoshi.comminiplugin.lib.FileAPI.HashUUID;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

public abstract class CustomItem implements InterfaceItem {

  public static final BiMap<String, Class<? extends CustomItem>> canShowingRegisters =
      HashBiMap.create();
  private static final BiMap<String, Class<? extends CustomItem>> registers = HashBiMap.create();
  private static final Map<String, Constructor<? extends CustomItem>> newConstructors =
      HashBiMap.create();
  @Getter @NotNull private final ItemStack item;
  private UUID uuid;

  public CustomItem(@NotNull final ItemStack item) {
    NBT.modify(
        item,
        nbt -> {
          final var compound = nbt.getOrCreateCompound(nbtKey);
          if (!compound.hasTag("identifier")) {
            compound.setString("identifier", getIdentifier());
          }
          if (compound.hasTag("uuid")) {
            this.uuid = compound.getUUID("uuid");
          } else {
            val uuid = canStack() ? HashUUID.v5(getClass().getName()) : UUID.randomUUID();
            compound.setUUID("uuid", uuid);
            this.uuid = uuid;
          }
        });
    this.item = item;
  }

  public static void registers(final Reflections reflections) {
    reflections
        .getSubTypesOf(CustomItem.class)
        .forEach(
            item -> {
              if (Modifier.isAbstract(item.getModifiers())) {
                return;
              }
              String id;
              boolean canShowing;
              Constructor<? extends CustomItem> newConstructor;
              try {
                val instance = item.getDeclaredConstructor().newInstance();
                id = instance.getIdentifier();
                canShowing = instance.canShowing();
                newConstructor = item.getDeclaredConstructor(ItemStack.class);
              } catch (InstantiationException
                  | IllegalAccessException
                  | InvocationTargetException
                  | NoSuchMethodException e) {
                throw new RuntimeException(e);
              }
              if (CustomItem.registers.containsKey(id)) {
                throw new IllegalArgumentException(
                    id
                        + "のカスタムアイテムがかぶっています、"
                        + item.getName()
                        + " >>==<< "
                        + CustomItem.registers.get(id).getName());
              }
              PluginLib.getPlugin()
                  .getSystemPrefix()
                  .logDebug(
                      "<gray>REGISTER ITEM "
                          + item.getSimpleName()
                          + " (canShowing: "
                          + canShowing
                          + ")");
              CustomItem.registers.put(id, item);
              CustomItem.newConstructors.put(id, newConstructor);
              if (canShowing) {
                CustomItem.canShowingRegisters.putIfAbsent(id, item);
              }
            });
  }

  public static CustomItem getNewCustomItem(final String identifier) {
    if (registers.containsKey(identifier)) {
      try {
        return registers.get(identifier).getDeclaredConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(identifier + " のIDはカスタムアイテムに登録されていません");
  }

  public static Optional<CustomItem> getCustomItemOptional(final ItemStack item) {
    return Optional.ofNullable(getCustomItem(item));
  }

  @Nullable
  public static CustomItem getCustomItem(final ItemStack item) {
    final var ci = getIdentifier(item);
    if (ci.isPresent()) {
      try {
        return newConstructors.get(ci.get()).newInstance(item);
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | SecurityException e) {
        return null;
      }
    }
    return null;
  }

  public static boolean hasCustomItem(Inventory inv, Class<?> clazz) {
    for (val item : inv) {
      if (equalsItem(item, clazz)) {
        return true;
      }
    }
    return false;
  }

  public static CustomItem getInventoryCustomItem(Inventory inv, UUID uuid) {
    for (val item : inv) {
      val custom = getCustomItem(item);
      if (custom != null && uuid.equals(custom.getUniqueId())) {
        return custom;
      }
    }
    return null;
  }

  public static boolean equalsItem(final ItemStack itemStack, final Class<?> clazz) {
    if (registers.containsValue(clazz)) {
      return equalsIdentifier(registers.inverse().get(clazz), itemStack);
    }
    return false;
  }

  /**
   * アイテムがその識別子なのかを判別します
   *
   * @param identifier 識別子
   * @param item 判定するアイテム
   * @return 同じ識別子の場合trueを返します
   */
  public static boolean equalsIdentifier(final String identifier, final ItemStack item) {
    final var id = getIdentifier(item);
    return id.map(s -> s.equals(identifier)).orElse(false);
  }

  /**
   * どちらのアイテムも同じ識別子かどうか判別します
   *
   * @param itemStack item1
   * @param item item2
   * @return 同じだった場合trueを返します
   */
  public static boolean equalsIdentifier(final ItemStack itemStack, final ItemStack item) {
    final var id = getIdentifier(itemStack);
    return id.filter(s -> equalsIdentifier(s, item)).isPresent();
  }

  /**
   * そのアイテムがもつ識別子が登録されているかを判別します
   *
   * @param item 対象のアイテム
   * @return 登録されいたらtrueを返します
   */
  public static boolean isCustomItem(final ItemStack item) {
    if (item == null || item.isEmpty()) {
      return false;
    }
    return NBT.get(
        item,
        readableNBT -> {
          if (!readableNBT.hasTag(nbtKey)) {
            return false;
          }
          final ReadableNBT compound = readableNBT.getCompound(nbtKey);
          assert compound != null;
          return compound.hasTag("identifier");
        });
  }

  /**
   * そのアイテムから識別子を取得します
   *
   * @param item 対象のアイテム
   * @return アイテムの識別子を返します
   * @throws IllegalArgumentException 渡されたアイテムがCustomItemではない場合にthrowされます
   */
  @NotNull
  public static Optional<String> getIdentifier(final ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return Optional.empty();
    }
    return NBT.get(
        item,
        nbt -> {
          if (!nbt.hasTag(nbtKey)) {
            return Optional.empty();
          }
          final ReadableNBT compound = nbt.getCompound(nbtKey);
          assert compound != null;
          return Optional.of(compound.getString("identifier"));
        });
  }

  public @NotNull UUID getUniqueId() {
    return uuid;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final CustomItem objItem) {
      return objItem.getIdentifier().equals(getIdentifier())
          && Objects.equals(objItem.getUniqueId(), getUniqueId());
    }
    return false;
  }

  @Override
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }
}
