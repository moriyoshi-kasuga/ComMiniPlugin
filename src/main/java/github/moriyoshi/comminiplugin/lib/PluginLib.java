package github.moriyoshi.comminiplugin.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.ExecutableCommand;
import github.moriyoshi.comminiplugin.dependencies.glowing.GlowingBlocks;
import github.moriyoshi.comminiplugin.dependencies.glowing.GlowingEntities;
import github.moriyoshi.comminiplugin.dependencies.ui.GuiListener;
import github.moriyoshi.comminiplugin.lib.block.CustomBlockData;
import github.moriyoshi.comminiplugin.lib.serializer.ItemStackAdapter;
import github.moriyoshi.comminiplugin.lib.serializer.LocationAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

public final class PluginLib {
  /** {@link Location} and {@link ItemStack} を Serializer and Deserializer できる {@link Gson} */
  public static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
          .registerTypeAdapter(Location.class, new LocationAdapter())
          .create();

  private PluginLib() {}

  @Getter private static InterfaceAPIPlugin plugin;
  @Getter private static CustomBlockData customBlockData;
  @Getter private static GuiListener guiListener;
  @Getter private static GlowingEntities glowingEntities;
  @Getter private static GlowingBlocks glowingBlocks;

  private static final Set<String> commands = new HashSet<>();

  public static void onEnable() {
    plugin.registerEvent(guiListener = GuiListener.getInstance());
    plugin.registerEvent(CustomListener.getInstance());
    glowingEntities = new GlowingEntities(plugin);
    glowingBlocks = new GlowingBlocks(plugin);
    CommandAPI.onEnable();
  }

  public static <T extends JavaPlugin & InterfaceAPIPlugin> void onLoad(T plugin) {
    PluginLib.plugin = plugin;
    CommandAPI.onLoad(
        new CommandAPIBukkitConfig(plugin).initializeNBTAPI(NBTContainer.class, NBTContainer::new));
  }

  public static void loadCustomBlock() {
    PluginLib.customBlockData = new CustomBlockData(plugin);
  }

  public static void unLoad() {
    BukkitUtil.clear();
    PluginLib.customBlockData.saveFile();
    commands.forEach(
        command -> {
          plugin.getSystemPrefix().logDebug("<gray>UNREGISTER COMMAND " + command);
          CommandAPI.unregister(command);
        });
    CommandAPI.onDisable();
  }

  public static void registerCommand(final Reflections reflection) {
    reflection
        .getSubTypesOf(ExecutableCommand.class)
        .forEach(
            command -> {
              if (Modifier.isAbstract(command.getModifiers())
                  || command.equals(CommandAPICommand.class)
                  || command.equals(CommandTree.class)) {
                return;
              }
              try {
                val constructor = command.getDeclaredConstructor();
                constructor.setAccessible(true);
                val instance = constructor.newInstance();
                instance.register();
                plugin.getSystemPrefix().logDebug("<yellow>REGISTER COMMAND " + instance.getName());
                commands.add(instance.getName());
              } catch (InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException
                  | NoSuchMethodException
                  | SecurityException e) {
                e.printStackTrace();
              }
            });
  }
}
