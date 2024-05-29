package github.moriyoshi.comminiplugin.dependencies.fastboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.val;

public class FastBoardManager {

  private final HashMap<UUID, FastBoard> fastBoards = new HashMap<>();

  private final Consumer<FastBoard> defaultBoardUpdate;

  public FastBoardManager(Consumer<FastBoard> defaultBoardUpdate) {
    this.defaultBoardUpdate = defaultBoardUpdate;
  }

  public final void add(Player player) {
    val board = new FastBoard(player);
    defaultBoardUpdate.accept(board);
    this.fastBoards.put(player.getUniqueId(), board);
  }

  public final void addAll(Collection<Player> c) {
    c.forEach(player -> add(player));
  }

  public final void addAll(Player... players) {
    for (Player player : players) {
      add(player);
    }
  }

  public final void forEach(Consumer<FastBoard> consumer) {
    this.fastBoards.values().forEach(consumer);
  }

  public final void remove(UUID uuid) {
    val board = fastBoards.remove(uuid);
    if (board != null) {
      board.delete();
    }
  }

  public final void clear() {
    this.fastBoards.values().forEach(FastBoard::delete);
    this.fastBoards.clear();
  }

}
