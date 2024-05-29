package github.moriyoshi.comminiplugin;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import github.moriyoshi.comminiplugin.api.serializer.ItemStackAdapter;
import github.moriyoshi.comminiplugin.api.serializer.LocationAdapter;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.command.LocationsCommands;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.GuiListener;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSCustomMenu;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.CustomListener;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.system.GameSystem;
import lombok.Getter;
import lombok.val;

public final class ComMiniPlugin extends JavaPlugin {

  /**
   * {@link Location} and {@link ItemStack} を Serializer and Deserializer できる
   * {@link Gson}
   */
  public static final Gson gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
      .registerTypeAdapter(Location.class, new LocationAdapter()).create();

  @Getter
  private static GuiListener guiListener;

  public static ComMiniPlugin getPlugin() {
    return getPlugin(ComMiniPlugin.class);
  }

  @Override
  public void onEnable() {
    CommandAPI.onEnable();
    registerEvent(guiListener = GuiListener.getInstance());
    registerEvent(GameListener.getInstance());
    registerEvent(CustomListener.getInstance());
    val commands = new Reflections("github.moriyoshi.comminiplugin.command");
    for (Class<? extends CommandAPICommand> command : commands.getSubTypesOf(CommandAPICommand.class)) {
      try {
        registerCommand(command.getDeclaredConstructor().newInstance());
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    }
    for (Class<? extends CommandTree> command : commands.getSubTypesOf(CommandTree.class)) {
      try {
        registerCommand(command.getDeclaredConstructor().newInstance());
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    }
    registerCommand(new CommandAPICommand("custommenu").withPermission(CommandPermission.OP)
        .executesPlayer((p, args) -> {
          new SSCustomMenu().openInv(p);
        }));
    registerCommand(new CommandAPICommand("gamedebug").executesPlayer((p, args) -> {
      final ComMiniPlayer player = ComMiniPlayer.getPlayer(p.getUniqueId());
      val flag = !player.isDebug();
      player.setDebug(flag);
      ComMiniPrefix.SYSTEM.send(p, flag ? "<red>Debug Enabled" : "<green>Debug Disable");
    }));
    ComMiniPlayer.gameInitialize();
    new WorldCreator("lobby").environment(Environment.NORMAL).type(WorldType.FLAT).generateStructures(false)
        .createWorld();
    new WorldCreator("game").environment(Environment.NORMAL).type(WorldType.FLAT).generateStructures(false)
        .createWorld();

    new WorldCreator("SkiResort").environment(Environment.NORMAL).createWorld();
  }

  @Override
  public void onDisable() {
    ComMiniPlayer.save();
    if (GameSystem.isIn()) {
      GameSystem.finalGame();
    }
    CommandAPI.onDisable();
    LocationsCommands.getManager().saveFile();
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
    CustomItem.registers("github.moriyoshi.comminiplugin");
    CustomBlock.registers("github.moriyoshi.comminiplugin");
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this).initializeNBTAPI(NBTContainer.class, NBTContainer::new));
  }
}
