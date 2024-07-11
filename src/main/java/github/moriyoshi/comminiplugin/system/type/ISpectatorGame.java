package github.moriyoshi.comminiplugin.system.type;

import github.moriyoshi.comminiplugin.system.IGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

public interface ISpectatorGame extends IGame {
  /**
   * ゲームが始まってからの観戦で参加するかいなか
   *
   * @param player 観戦させたい人
   * @return true で参加させ、false で観戦不可能
   */
  @OverrideOnly
  boolean predicateSpec(Player player);

  /**
   * ゲームが始まってからの観戦で参加したさいの処理
   *
   * @param player 観戦させたい人
   */
  @OverrideOnly
  void innerAddSpec(Player player);
}
