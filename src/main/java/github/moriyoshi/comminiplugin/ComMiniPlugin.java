package github.moriyoshi.comminiplugin;

import github.moriyoshi.comminiplugin.command.LocationsCommands;
import github.moriyoshi.comminiplugin.lib.InterfaceAPIPlugin;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.system.ResourcePackSystem;
import github.moriyoshi.comminiplugin.system.biggame.BigGameSystem;
import lombok.Getter;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public final class ComMiniPlugin extends JavaPlugin implements InterfaceAPIPlugin {

  public static final PrefixUtil MAIN = new PrefixUtil("<gold>[<aqua>ComMini<gold>] ");
  public static final PrefixUtil SYSTEM = new PrefixUtil("<gold>[<red>System<gold>] ");

  @Getter private static ComMiniPlugin plugin;

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

    registerEvent(GameListener.getInstance());
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .forPackage("github.moriyoshi.comminiplugin")
                .filterInputsBy(
                    new FilterBuilder()
                        .excludePackage("github.moriyoshi.comminiplugin.dependencies")
                        .excludePackage("github.moriyoshi.comminiplugin.system")));
    CustomItem.registers(reflections);
    PluginLib.onEnable();
    PluginLib.registerCommand(new Reflections("github.moriyoshi.comminiplugin.command"));
    // TODO: これを onLoad()　時にやってPluginLib.onEnable で普通に load CustomBlock すればいいんじゃね?
    CustomBlock.registers(reflections);
    PluginLib.loadCustomBlock();

    ComMiniPlayer.gameInitialize();
    BigGameSystem.load();
    ResourcePackSystem.load();

    SYSTEM.broadCast("<red>プラグインをロードしました。");
  }

  @Override
  public void onDisable() {
    ComMiniPlayer.save();
    BigGameSystem.finalGame();
    LocationsCommands.getManager().saveFile();

    PluginLib.unLoad();

    SYSTEM.broadCast("<red>プラグインをアンロードしました。");
  }

  @Override
  public void onLoad() {
    PluginLib.onLoad(this);
  }

  @Override
  public PrefixUtil getMainPrefix() {
    return MAIN;
  }

  @Override
  public PrefixUtil getSystemPrefix() {
    return SYSTEM;
  }
}
