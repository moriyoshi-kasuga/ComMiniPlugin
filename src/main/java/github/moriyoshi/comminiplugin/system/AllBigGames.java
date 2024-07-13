package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigGame;
import github.moriyoshi.comminiplugin.biggame.battleroyale.BRListener;
import github.moriyoshi.comminiplugin.biggame.survivalsniper.SSBigGame;
import github.moriyoshi.comminiplugin.biggame.survivalsniper.SSListener;
import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.PrefixUtil;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedException;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedSupplier;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public enum AllBigGames {
  SS(
      SSBigGame.class,
      Material.SPYGLASS,
      "survivalsniper",
      "<blue>鉄塊を集めてスナイパーで相手を殺しあいます",
      "<blue>サバイバルスナイパー",
      new PrefixUtil("<gray>[<blue>SurvivalSniper<gray>]"),
      SSListener::new),
  BR(
      BRBigGame.class,
      Material.GOLDEN_SWORD,
      "battleroyale",
      "<yellow>バトルロワイヤル",
      "<yellow>殺せ!殺せ!勝ち上がれ!",
      new PrefixUtil("<gray>[<yellow>BattleRoyale<gray>]"),
      BRListener::new);

  public final Class<? extends AbstractBigGame> clazz;
  public final Material icon;
  public final String id;
  public final String name;
  public final String description;
  public final PrefixUtil prefixUtil;
  public final Function<IdentifierKey, IGameListener<?>> listener;

  public final GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(
      final Player player) {
    return () -> {
      try {
        return clazz
            .getDeclaredConstructor(
                Material.class,
                String.class,
                String.class,
                String.class,
                Player.class,
                PrefixUtil.class,
                Function.class)
            .newInstance(icon, id, name, description, player, prefixUtil, listener);
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        throw new GameInitializeFailedException("reflection error");
      }
    };
  }
}
