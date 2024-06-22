package github.moriyoshi.comminiplugin.biggame.battleroyale;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.BlockInputsAPI;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.RandomCollection;
import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@SuppressWarnings("deprecation")
public class TreasureLocation extends BlockInputsAPI<List<Pair<Integer, Integer>>> {

  private final int level = 1;

  public TreasureLocation(String name) {
    super(ComMiniPlugin.getPlugin(), "br/treasure", name);
    getLocations().keySet().forEach(loc -> loc.getBlock().setType(Material.BEDROCK));
  }

  @Override
  public List<Pair<Integer, Integer>> loadLocData(JsonElement element) {
    return element.getAsJsonArray().asList().stream()
        .map(
            e ->
                PluginLib.gson.<Pair<Integer, Integer>>fromJson(
                    e, new TypeToken<Pair<Integer, Integer>>() {}.getType()))
        .toList();
  }

  @Override
  public JsonElement saveLocData(List<Pair<Integer, Integer>> data) {
    return PluginLib.gson.toJsonTree(data);
  }

  @Override
  protected void innerAddLocation(Location location, Player player) {
    AnvilInputs.getInput(
            ComMiniPlugin.getPlugin(),
            "<red>Treasure",
            (str, state) -> {
              if (StringUtils.isEmpty(str)) {
                return Optional.of(List.of(Pair.of(1, 1)));
              }
              if (str.contains(",")) {
                return checkData(
                    Stream.of(str.split(","))
                        .map(
                            s -> {
                              try {
                                return Pair.of(Integer.valueOf(s), 1);
                              } catch (IllegalArgumentException ignore) {
                                try {
                                  val p = s.split("%");
                                  return Pair.of(Integer.valueOf(p[1]), Integer.valueOf(p[0]));
                                } catch (Exception e) {
                                  return Pair.of(-1, 1);
                                }
                              }
                            })
                        .toList());
              }
              if (str.contains("~")) {
                val list = Stream.of(str.split("~")).map(Integer::valueOf).toList();
                return checkData(
                    IntStream.range(list.get(0), list.get(1) + 1)
                        .boxed()
                        .map(i -> Pair.of(i, 1))
                        .toList());
              }
              try {
                return checkData(List.of(Pair.of(Integer.parseInt(str), 1)));
              } catch (NumberFormatException e) {
                return Optional.empty();
              }
            },
            (str, state) -> Collections.emptyList(),
            (list, state) -> {
              finalAddLocation(location, list);
              location.getBlock().setType(Material.BEDROCK);
              return List.of(ResponseAction.close());
            })
        .open(player);
  }

  private Optional<List<Pair<Integer, Integer>>> checkData(List<Pair<Integer, Integer>> list) {
    val keys = list.stream().map(Pair::getFirst).toList();
    if (Collections.max(keys) > 5) {
      return Optional.empty();
    }
    if (0 > Collections.max(keys)) {
      return Optional.empty();
    }
    return Optional.of(list);
  }

  @Override
  public ChatColor getColor(Location location) {
    return switch (Collections.max(
        getLocations().get(location).stream().map(Pair::getFirst).toList())) {
      case 5 -> ChatColor.DARK_PURPLE;
      case 4 -> ChatColor.GREEN;
      case 3 -> ChatColor.AQUA;
      case 2 -> ChatColor.BLUE;
      case 1 -> ChatColor.WHITE;
      default -> ChatColor.BLACK;
    };
  }

  public void setTreasures() {
    val r = new Random();
    getLocations()
        .forEach(
            (loc, pairs) -> {
              val random = new RandomCollection<Integer>(r);
              pairs.forEach(pair -> random.add(pair.getSecond(), pair.getFirst()));
              val value = random.next();
              if (value == 0) {
                loc.getBlock().setType(Material.AIR);
                return;
              }
              new TreasureChest(
                  loc.getBlock(), BukkitUtil.convertYawToBlockFace(loc.getYaw()), value);
            });
  }

  public void clearTreasures() {
    getLocations()
        .forEach(
            (loc, list) -> {
              val block = loc.getBlock();
              if (CustomBlock.isCustomBlock(block, TreasureChest.class)) {
                CustomBlock.getCustomBlock(block).remove();
              }
            });
  }
}
