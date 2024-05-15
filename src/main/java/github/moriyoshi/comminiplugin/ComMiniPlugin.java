package github.moriyoshi.comminiplugin;

import javax.tools.DocumentationTool.Location;

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
import github.moriyoshi.comminiplugin.api.serializer.ItemStackSerializer;
import github.moriyoshi.comminiplugin.api.serializer.LocationSerializer;
import github.moriyoshi.comminiplugin.command.AdminGameMenuCommand;
import github.moriyoshi.comminiplugin.command.AllSoundCommand;
import github.moriyoshi.comminiplugin.command.CustomItemsCommand;
import github.moriyoshi.comminiplugin.command.FinalizeGameCommand;
import github.moriyoshi.comminiplugin.command.ItemEditCommand;
import github.moriyoshi.comminiplugin.command.MenuCommand;
import github.moriyoshi.comminiplugin.command.RandomTeleport;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.GuiListener;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSCustomMenu;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemListner;
import github.moriyoshi.comminiplugin.system.game.GameListener;
import github.moriyoshi.comminiplugin.system.game.GamePlayer;
import lombok.Getter;

public final class ComMiniPlugin extends JavaPlugin {

  /**
   * {@link Location} and {@link ItemStack} を Serializer and Deserializer できる
   * {@link Gson}
   */
  public static final Gson gson = new GsonBuilder().registerTypeAdapter(ItemStack.class,
      new ItemStackSerializer()).registerTypeAdapter(Location.class, new LocationSerializer())
      .create();

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
    registerEvent(CustomItemListner.getInstance());
    registerCommand(new MenuCommand());
    registerCommand(new AdminGameMenuCommand());
    registerCommand(new FinalizeGameCommand());
    registerCommand(new CustomItemsCommand());
    registerCommand(new ItemEditCommand());
    registerCommand(new AllSoundCommand());
    registerCommand(new RandomTeleport());
    registerCommand(new CommandAPICommand("custommenu")
        .withPermission(CommandPermission.OP)
        .executesPlayer((p, args) -> {
          new SSCustomMenu().openInv(p);
        }));
    registerCommand(new CommandAPICommand("gamedebug")
        .executesPlayer((p, args) -> {
          GamePlayer player = GamePlayer.getPlayer(p.getUniqueId());
          var flag = !player.isDebug();
          player.setDebug(flag);
          ComMiniPrefix.SYSTEM.send(p, flag ? "<red>Debug Enabled" : "<green>Debug Disable");
        }));
    GamePlayer.gameInitialize();
    new WorldCreator("lobby").environment(Environment.NORMAL).type(WorldType.FLAT).createWorld();
    new WorldCreator("game").environment(Environment.NORMAL).type(WorldType.FLAT).createWorld();
  }

  @Override
  public void onDisable() {
    GamePlayer.save();
    CommandAPI.onDisable();
  }

  /**
   * {@link CommandAPICommand} を登録するメゾット
   *
   * @param commandAPICommand instance
   */
  public void registerCommand(CommandAPICommand commandAPICommand) {
    commandAPICommand.register();
    ComMiniPrefix.SYSTEM.logDebug("<yellow>REGISTER COMMAND " + commandAPICommand.getName());
  }

  /**
   * {@link CommandTree} を登録するメゾット
   *
   * @param commandTree instance
   */
  public void registerCommand(CommandTree commandTree) {
    commandTree.register();
    ComMiniPrefix.SYSTEM.logDebug("<yellow>REGISTER COMMAND " + commandTree.getName());
  }

  /**
   * {@link Listener} を登録するメゾット
   *
   * @param listener instance
   */
  public void registerEvent(Listener listener) {
    this.getServer().getPluginManager().registerEvents(listener, this);
  }

  @Override
  public void onLoad() {
    Reflections reflections = new Reflections("github.moriyoshi.comminiplugin");
    reflections.getSubTypesOf(CustomItem.class).forEach(item -> {
      try {
        var id = item.getDeclaredConstructor().newInstance().getIdentifier();
        if (CustomItem.registers.containsKey(id)) {
          throw new IllegalArgumentException(
              id + "のカスタムアイテムがかぶっています、" + item.getName() + " >>==<< "
                  + CustomItem.registers.get(id).getName());
        }
        CustomItem.registers.put(id, item);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    CommandAPI.onLoad(
        new CommandAPIBukkitConfig(this).initializeNBTAPI(NBTContainer.class, NBTContainer::new));
  }
}
