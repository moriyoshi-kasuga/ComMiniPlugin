package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.minigame.lobby_ffa.LFFAMiniGame;
import github.moriyoshi.comminiplugin.util.IdentifierKey;
import java.util.ArrayList;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniGameSystem {
  static final HashMap<IdentifierKey, AbstractMiniGame> minigames = new HashMap<>();

  public static void load() {
    new LFFAMiniGame();
  }

  public static void clear() {
    new ArrayList<>(minigames.values()).forEach(AbstractMiniGame::finishGame);
  }

  public static void register(IdentifierKey key, AbstractMiniGame miniGame) {
    minigames.put(key, miniGame);
  }

  public static void unregister(IdentifierKey key) {
    minigames.remove(key);
  }

  @Nullable
  public static AbstractMiniGame getMiniGame(IdentifierKey key) {
    return minigames.get(key);
  }

  @NotNull
  public static <T extends AbstractMiniGame> T getMiniGame(IdentifierKey key, Class<T> t) {
    return t.cast(minigames.get(key));
  }

  @NotNull
  public static <T extends AbstractMiniGame> T getUniqueMiniGame(String id, Class<T> t) {
    return t.cast(
        minigames.entrySet().stream()
            .filter(x -> x.getKey().getIdentifier().equals("minigame-" + id))
            .findFirst()
            .orElseThrow()
            .getValue());
  }
}
