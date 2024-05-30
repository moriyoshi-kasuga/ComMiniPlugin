package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.BlockInputsAPI;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.PlayerChatInput;
import lombok.Getter;
import lombok.val;

public class TreasureLocation extends BlockInputsAPI<List<Integer>> {

  public TreasureLocation(String name) {
    super(ComMiniPlugin.getPlugin(), "br/treasure", name);
  }

  @Getter
  private int level = 1;

  public final void setLevel(int level) {
    this.level = level;
    val game = GameSystem.getGame(BRGame.class);
    getPlayers().forEach(p -> {
      game.prefix.send(p, "<red>change Treasure level to <yellow>" + level);
    });
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
    if (player.isConversing()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>先にほかのlevel設定を終わらせてください");
      return;
    }
    new PlayerChatInput(ComMiniPlugin.getPlugin(), "Treasure", true, new Prompt() {

      @Override
      public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return "1 ~ 4の数字を入力してください [(1),(1,2,3,4),(1..4)]";
      }

      @Override
      public boolean blocksForInput(@NotNull ConversationContext context) {
        return false;
      }

      @Override
      public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
        if (StringUtils.isEmpty(input)) {
          finalAddLocation(location, List.of(1));
          return null;
        }
        if (input.contains(",")) {
          finalAddLocation(location, List.of(input.split(",")).stream().map(s -> Integer.valueOf(s)).toList());
          return null;
        }
        val list = List.of(input.split("..")).stream().map(s -> Integer.valueOf(s)).toList();
        finalAddLocation(location, IntStream.range(list.get(0), list.get(1) + 1).boxed().toList());
        return null;
      }

    }, 100).build(player);
  }

}
