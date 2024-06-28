package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigGame;
import github.moriyoshi.comminiplugin.biggame.survivalsniper.SSBigGame;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedSupplier;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public enum AllBigGames {
  SS(Material.SPYGLASS, "survivalsniper", "<blue>鉄塊を集めてスナイパーで相手を殺しあいます", "<blue>サバイバルスナイパー") {

    @Override
    public GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(Player player) {
      return () -> new SSBigGame(icon, id, name, description, player);
    }
  },
  BR(Material.GOLDEN_SWORD, "battleroyale", "<yellow>バトルロワイヤル", "<yellow>殺せ!殺せ!勝ち上がれ!") {
    @Override
    public GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(Player player) {
      return () -> new BRBigGame(icon, id, name, description, player);
    }
  };

  public final Material icon;
  public final String id;
  public final String name;
  public final String description;

  public abstract GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(
      Player player);
}
