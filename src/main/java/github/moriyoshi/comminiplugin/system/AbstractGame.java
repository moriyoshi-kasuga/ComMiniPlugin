package github.moriyoshi.comminiplugin.system;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

@RequiredArgsConstructor
public abstract class AbstractGame {

  public final String id;
  public final String name;
  public final String description;
  public final Material material;
  public final PrefixUtil prefix;
  final AbstractGameListener<?> listener;

  @Getter
  private boolean isStarted = false;

  @Getter
  protected World world;

  @Getter
  protected Location lobby;

  public abstract MenuHolder<ComMiniPlugin> adminMenu();

  // TODO: ここでoptional じゃなくて iscanopenmenu とかでやろう
  // まあ add join addspec やるならほぼ意味ない気がするけどconfigじゃあ
  // とりあえずメモろう、
  // でボタンにpredicate apply すればほぼ簡単になる
  public abstract Optional<MenuHolder<ComMiniPlugin>> gameMenu(Player player);

  /**
   * プレイヤーを観戦に追加します
   *
   * @param player 追加したらtrueです、追加できないならfalseを早期 return してください
   */
  public abstract boolean addSpec(Player player);
  // TODO: addSpec だけじゃなくて addJoin も追加しよう
  // menu に addspec だけじゃなくて addjoin もいれて config だけにする

  /**
   * このゲームの初期化をするメゾット
   *
   * @param player 初期化する運営
   */
  public abstract boolean initializeGame(Player player);

  public final boolean startGame(Player player) {
    if (!innerStartGame(player)) {
      return false;
    }
    isStarted = true;
    ComMiniPlugin.getPlugin().registerEvent(listener);
    // TODO: ここでGamePlayerのisingameとかのフラッグ作ってあとはGameSystemとかにちゃんとinGamePlayer() とか
    // inStartGamePlyaer () とかの関数を用意しよう
    // Buttonとかでいっぱいその関数使うと思うからここらで用意する
    return true;
  }

  public final void finishGame() {
    isStarted = false;
    HandlerList.unregisterAll(listener);
    runPlayers(GameSystem::initializePlayer);
    innerFinishGame();
  }

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

  public final void hidePlayer() {
    List<UUID> list = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !isGamePlayer(p))
        .map(Entity::getUniqueId).toList();
    ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

  public final void showPlayer() {
    var list = Bukkit.getOnlinePlayers().stream()
        .filter(p -> !isGamePlayer(p))
        .map(p -> ((CraftPlayer) p).getHandle()).toList();
    ClientboundPlayerInfoUpdatePacket packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(list);
    runPlayers(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
  }

  /**
   * 主にプレイヤーの準備ができたら呼び出す、ゲームを開始するメゾット
   *
   * @param player 呼び出す運営
   * @return 開始できたらtrue
   */
  protected abstract boolean innerStartGame(Player player);

  /**
   * このメゾットを呼び出す前に自前でプレイヤーたちに対する endGame メゾット作って このゲームの設定をクリアするメゾット
   */
  protected abstract void innerFinishGame();

}
