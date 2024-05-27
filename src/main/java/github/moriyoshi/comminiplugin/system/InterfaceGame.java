package github.moriyoshi.comminiplugin.system;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.util.PrefixUtil;

public interface InterfaceGame {

  boolean isStarted();

  World getWorld();

  Location getLobby();

  boolean addSpec(Player player);

  boolean initializeGame(Player player);

  boolean isGamePlayer(Player player);

  void runPlayers(Consumer<Player> consumer);

  void teleportLobby(Player player);

  void hidePlayer();

  boolean startGame(Player player);

  void finishGame();

  void showPlayer();

  PrefixUtil getPrefix();
}
