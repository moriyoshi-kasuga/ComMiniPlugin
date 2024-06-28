package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigGame;
import github.moriyoshi.comminiplugin.system.IGame.GameInitializeFailedSupplier;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public enum AllBigGames {
  BR(Material.GOLDEN_SWORD, "battleroyale", "<yellow>バトルロワイヤル", "<yellow>殺せ!殺せ!勝ち上がれ!") {
    @Override
    public GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(Player player) {
      return () -> new BRBigGame(icon, name, description, player);
    }
  };

  public final Material icon;
  public final String id;
  public final String name;
  public final String description;

  public abstract GameInitializeFailedSupplier<? extends AbstractBigGame> getSupplier(
      Player player);
}
