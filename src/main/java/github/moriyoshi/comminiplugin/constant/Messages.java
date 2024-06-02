package github.moriyoshi.comminiplugin.constant;

import github.moriyoshi.comminiplugin.util.Util;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public enum Messages {
  GAME_FINAL_OR_START("<red>ゲームが開始されたか閉幕しました!"),
  GAME_NOT_FOUND("<red>現在ゲームは開催されていません!"),
  GAME_NOT_START("<red>ゲームは開始されていません!"),
  GAME_ALREADY_START("<red>既にゲームは開始されています!");

  public final Component message;

  Messages(String message) {
    this.message = Util.mm(message);
  }

  public void send(CommandSender sender) {
    ComMiniPrefix.MAIN.send(sender, this.message);
  }

  public void send(Collection<CommandSender> senders) {
    senders.forEach(sender -> ComMiniPrefix.MAIN.send(sender, this.message));
  }

  public void send(CommandSender... senders) {
    this.send(List.of(senders));
  }
}
