package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.BlockInputsAPI;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.dependencies.anvilgui.AnvilInputs;
import github.moriyoshi.comminiplugin.system.GameSystem;
import lombok.Getter;
import lombok.val;
import net.wesjd.anvilgui.AnvilGUI.ResponseAction;

@Getter
@SuppressWarnings("deprecation")
public class TreasureLocation extends BlockInputsAPI<List<Integer>> {

  public TreasureLocation(String name) {
    super(ComMiniPlugin.getPlugin(), "br/treasure", name);
  }

  private int level = 1;

  public final void setLevel(int level) {
    this.level = level;
    val game = GameSystem.getGame(BRGame.class);
    getPlayers().values().forEach(p -> game.prefix.send(p, "<red>change Treasure level to <yellow>" + level));
  }

  @Override
  public List<Integer> loadLocData(JsonElement element) {
    return element.getAsJsonArray().asList().stream().map(JsonElement::getAsInt).toList();
  }

  @Override
  public JsonElement saveLocData(List<Integer> data) {
    return ComMiniPlugin.gson.toJsonTree(data);
  }

  @Override
  protected void innerAddLocation(Location location, Player player) {
    AnvilInputs.getInput(ComMiniPlugin.getPlugin(), "<red>Treasure", (str, state) -> {
      if (StringUtils.isEmpty(str)) {
        return Optional.of(List.of(1));
      }
      if (str.contains(",")) {
        return checkData(Stream.of(str.split(",")).map(Integer::valueOf).toList());
      }
      if (str.contains("~")) {
        val list = Stream.of(str.split("~")).map(Integer::valueOf).toList();
        return checkData(IntStream.range(list.get(0), list.get(1) + 1).boxed().toList());
      }
      try {
        return checkData(List.of(Integer.parseInt(str)));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    }, (str, state) -> Collections.emptyList(), (list, state) -> {
      finalAddLocation(location, list);
      return List.of(ResponseAction.close());
    }).open(player);
  }

  private Optional<List<Integer>> checkData(List<Integer> list) {
    if (Collections.max(list) > 5) {
      return Optional.empty();
    }
    if (1 > Collections.max(list)) {
      return Optional.empty();
    }
    return Optional.of(list);
  }

  @Override
  public ChatColor getColor(Location location) {
    return switch (Collections.max(getLocations().get(location))) {
      case 5 -> ChatColor.DARK_PURPLE;
      case 4 -> ChatColor.AQUA;
      case 3 -> ChatColor.GREEN;
      case 2 -> ChatColor.BLUE;
      default -> ChatColor.WHITE;
    };

  }

  public void setTreasures() {
    val random = new Random();
    getLocations().forEach((loc, list) -> new TreasureChest(loc.getBlock(), list.get(random.nextInt(list.size()))));
  }

  public void clearTreasures() {
    getLocations().forEach((loc, list) -> {
      val block = loc.getBlock();
      if (CustomBlock.isCustomBlock(block, TreasureChest.class)) {
        CustomBlock.getCustomBlock(block).remove();
      }
    });
  }

}
