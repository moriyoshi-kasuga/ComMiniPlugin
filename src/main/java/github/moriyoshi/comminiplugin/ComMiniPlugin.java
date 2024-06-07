package github.moriyoshi.comminiplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.changeme.nbtapi.NBTContainer;
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
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadBlock;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.CustomListener;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

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

    new WorldCreator("SkiResort").environment(Environment.NORMAL).createWorld();
    new WorldCreator("Fantacy").environment(Environment.NORMAL).createWorld();
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
    val reflectionsObject = new Reflections("github.moriyoshi.comminiplugin.object");
    val reflectionsGame = new Reflections("github.moriyoshi.comminiplugin.game");
    CustomItem.registers(reflectionsObject);
    CustomItem.registers(reflectionsGame);
    CustomBlock.registers(reflectionsObject);
    CustomBlock.registers(reflectionsGame);
    val commands = new Reflections("github.moriyoshi.comminiplugin.command");
    ReflectionUtil.forEachAllClass(
        commands,
        CommandAPICommand.class,
        command -> {
          try {
            val constructor = command.getDeclaredConstructor();
            constructor.setAccessible(true);
            registerCommand(constructor.newInstance());
          } catch (InstantiationException
              | IllegalAccessException
              | IllegalArgumentException
              | InvocationTargetException
              | NoSuchMethodException
              | SecurityException e) {
            e.printStackTrace();
          }
        });
    ReflectionUtil.forEachAllClass(
        commands,
        CommandTree.class,
        command -> {
          try {
            val constructor = command.getDeclaredConstructor();
            constructor.setAccessible(true);
            registerCommand(constructor.newInstance());
          } catch (InstantiationException
              | IllegalAccessException
              | IllegalArgumentException
              | InvocationTargetException
              | NoSuchMethodException
              | SecurityException e) {
            e.printStackTrace();
          }
        });

    CustomBlockData.getInstance();
    ComMiniPlayer.gameInitialize();
  }

  @Override
  public void onDisable() {
    ComMiniPlayer.save();
    GameSystem.finalGame();
    CustomBlockData.getInstance().saveFile();
    CommandAPI.onDisable();
    LocationsCommands.getManager().saveFile();

    HandlerList.unregisterAll(guiListener);
    HandlerList.unregisterAll(GameListener.getInstance());
    HandlerList.unregisterAll(CustomListener.getInstance());

    JumpPadBlock.clear();

    glowingEntities.disable();
    glowingBlocks.disable();
  }

  /**
   * {@link CommandAPICommand} を登録するメゾット
   *
   * @param commandAPICommand instance
   */
  public void registerCommand(final CommandAPICommand commandAPICommand) {
    commandAPICommand.register();
    ComMiniPrefix.SYSTEM.logDebug("<yellow>REGISTER COMMAND " + commandAPICommand.getName());
  }

  /**
   * {@link CommandTree} を登録するメゾット
   *
   * @param commandTree instance
   */
  public void registerCommand(final CommandTree commandTree) {
    commandTree.register();
    ComMiniPrefix.SYSTEM.logDebug("<yellow>REGISTER COMMAND " + commandTree.getName());
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
