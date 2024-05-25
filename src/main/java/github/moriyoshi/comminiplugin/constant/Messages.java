package github.moriyoshi.comminiplugin.constant;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;

import github.moriyoshi.comminiplugin.util.Util;
import net.kyori.adventure.text.Component;

public enum Messages {
  GAME_NOT_FOUND("<red>現在ゲームは開催されていません!"),
  GAME_NOT_START("<red>ゲームは開始されていません!"),
  GAME_ALREADY_START("<red>既にゲームは開始されています!");

  public Component message;

  Messages(String message) {
    this.message = Util.mm(message);
  }

  public void send(CommandSender sender) {
    ComMiniPrefix.MAIN.send(sender, this.message);
  }

  public void send(Collection<CommandSender> senders) {
    senders.forEach(sender -> {
      ComMiniPrefix.MAIN.send(sender, this.message);
    });
  }

  public void send(CommandSender... senders) {
    this.send(List.of(senders));
  }
}
