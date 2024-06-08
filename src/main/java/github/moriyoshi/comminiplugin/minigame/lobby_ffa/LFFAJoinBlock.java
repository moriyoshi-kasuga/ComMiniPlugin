package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import com.google.gson.JsonElement;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadBlock;
import github.moriyoshi.comminiplugin.system.minigame.MiniGameSystem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
    super.walk(e);
    val player = e.getPlayer();
    if (BukkitUtil.isFalling(player.getUniqueId())) {
      return;
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        if (!player.isOnGround()) {
          return;
        }
        MiniGameSystem.getUniqueMiniGame("LFFA", LFFAMiniGame.class).addPlayer(player);
        this.cancel();
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }
}
