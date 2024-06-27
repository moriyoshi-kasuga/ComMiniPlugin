package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import com.google.gson.JsonElement;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadBlock;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class LFFAJoinBlock extends JumpPadBlock {

  public LFFAJoinBlock(Block block, JsonElement dataElement) {
    super(block, dataElement);
  }

  public LFFAJoinBlock(Block block, Player player) {
    super(block, player);
  }

  public LFFAJoinBlock(Block block) {
    super(block);
  }

  @Override
  public void walk(PlayerMoveEvent e) {
    val player = e.getPlayer();
    if (BukkitUtil.isFalling(player.getUniqueId())) {
      return;
    }
    GameSystem.getUniqueGame("LFFA", LFFAMiniGame.class).addPlayer(player);
    super.walk(e);
  }
}
