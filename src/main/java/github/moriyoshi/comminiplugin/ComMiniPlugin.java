package github.moriyoshi.comminiplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.AbstractCommandAPICommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import github.moriyoshi.comminiplugin.api.serializer.ItemStackAdapter;
import github.moriyoshi.comminiplugin.api.serializer.LocationAdapter;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.block.CustomBlockData;
import github.moriyoshi.comminiplugin.command.LocationsCommands;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.glowing.GlowingBlocks;
import github.moriyoshi.comminiplugin.dependencies.glowing.GlowingEntities;
import github.moriyoshi.comminiplugin.dependencies.ui.GuiListener;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.CustomListener;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.system.minigame.MiniGameSystem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public final class ComMiniPlugin extends JavaPlugin {

  /** {@link Location} and {@link ItemStack} を Serializer and Deserializer できる {@link Gson} */
  public static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
          .registerTypeAdapter(Location.class, new LocationAdapter())
          .create();

  @Getter private static ComMiniPlugin plugin;

  @Getter private static GuiListener guiListener;

  @Getter private static GlowingEntities glowingEntities;
  @Getter private static GlowingBlocks glowingBlocks;

  private static Set<String> commands = new HashSet<>();

  public void loadWorlds() {
    new WorldCreator("lobby")
        .environment(Environment.NORMAL)
        .type(WorldType.FLAT)
        .generateStructures(false)
        .createWorld();
    new WorldCreator("game")
        .environment(Environment.NORMAL)
        .type(WorldType.FLAT)
        .generateStructures(false)
        .createWorld();
  }

  @Override
  public void onEnable() {
    ComMiniPlugin.plugin = this;

    loadWorlds();

    CommandAPI.onEnable();
    glowingEntities = new GlowingEntities(this);
    glowingBlocks = new GlowingBlocks(this);
    registerEvent(guiListener = GuiListener.getInstance());
    registerEvent(GameListener.getInstance());
    registerEvent(CustomListener.getInstance());
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .forPackage("github.moriyoshi.comminiplugin")
                .filterInputsBy(
                    new FilterBuilder()
                        .excludePackage("github.moriyoshi.comminiplugin.dependencies")
                        .excludePackage("github.moriyoshi.comminiplugin.system")));
    CustomItem.registers(reflections);
    CustomBlock.registers(reflections);
    registerCommand(new Reflections("github.moriyoshi.comminiplugin.command"));
    CustomBlockData.getInstance();
    ComMiniPlayer.gameInitialize();
    GameSystem.load();
    MiniGameSystem.load();
    ComMiniPrefix.SYSTEM.cast("<red>プラグインをロードしました。");
  }

  @Override
  public void onDisable() {
    ComMiniPlayer.save();
    GameSystem.finalGame();
    MiniGameSystem.clear();

    CustomBlockData.getInstance().saveFile();
    LocationsCommands.getManager().saveFile();

    commands.forEach(
        command -> {
          ComMiniPrefix.SYSTEM.cast("<gray>UNREGISTER COMMAND " + command);
          CommandAPI.unregister(command);
        });
    CommandAPI.onDisable();

    BukkitUtil.clear();
    glowingEntities.disable();
    glowingBlocks.disable();
    ComMiniPrefix.SYSTEM.cast("<red>プラグインをアンロードしました。");
  }

  public void registerCommand(final Reflections reflection) {
    ReflectionUtil.forEachAllClass(
        reflection,
        AbstractCommandAPICommand.class,
        command -> {
          if (command.equals(CommandAPICommand.class) || command.equals(CommandTree.class)) {
            return;
          }
          try {
            val constructor = command.getDeclaredConstructor();
            constructor.setAccessible(true);
            val instance = constructor.newInstance();
            instance.register();
            ComMiniPrefix.SYSTEM.logDebug("<yellow>REGISTER COMMAND " + instance.getName());
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

  /**
   * {@link Listener} を登録するメゾット
   *
   * @param listener instance
   */
  public void registerEvent(final Listener listener) {
    this.getServer().getPluginManager().registerEvents(listener, this);
  }

  @Override
  public void onLoad() {
    CommandAPI.onLoad(
        new CommandAPIBukkitConfig(this).initializeNBTAPI(NBTContainer.class, NBTContainer::new));
  }
}
