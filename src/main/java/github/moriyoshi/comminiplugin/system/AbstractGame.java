package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public abstract class AbstractGame {

  public final String id;
  public final String name;
  public final String description;
  public final Material material;
  public final PrefixUtil prefix;
  public final AbstractGameListener<?> listener;

  @Getter
  boolean isRunning = false;

  @Getter
  boolean isStarted = false;

  @Getter
  protected World world;
  @Getter
  protected Location lobby;

  public abstract MenuHolder<ComMiniPlugin> adminMenu();

  public abstract Optional<MenuHolder<ComMiniPlugin>> gameMenu(Player player);

  /**
   * プレイヤーを観戦に追加します
   *
   * @param player 追加したらtrueです、追加できないならfalseを早期 return してください
   */
  public abstract boolean addSpec(Player player);

  /**
   * このゲームの初期化をするメゾット
   *
   * @param player 初期化する運営
   */
  public abstract boolean initializeGame(Player player);

  /**
   * 主にプレイヤーの準備ができたら呼び出す、ゲームを開始するメゾット
   *
   * @param player 呼び出す運営
   * @return 開始できたらtrue
   */
  public abstract boolean startGame(Player player);

  /**
   * このメゾットを呼び出す前に自前でプレイヤーたちに対する endGame メゾット作って このゲームの設定をクリアするメゾット
   */
  public abstract void finishGame();

  /**
   * このプレイヤーのこのゲームに参加しているか
   *
   * @param player 確認するプレイヤー
   * @return 参加していたら
   */
  public abstract boolean isGamePlayer(Player player);

  /**
   * このゲームに参加しているプレイヤーに対して処理をします
   *
   * @param consumer する処理
   */
  public final void runPlayers(Consumer<Player> consumer) {
    Bukkit.getOnlinePlayers().forEach(p -> {
      if (isGamePlayer(p)) {
        consumer.accept(p);
      }
    });
  }

  public final void teleportLobby(Player player) {
    player.teleport(this.lobby);
  }

}
