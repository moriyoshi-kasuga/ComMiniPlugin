package github.moriyoshi.comminiplugin.util;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ExactMatchConversationCanceller;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PlayerChatInput implements ConversationAbandonedListener {

  public static boolean isInputted(Player player) {
    return player.isConversing();
  }

  private final ConversationFactory factory;

  public PlayerChatInput(Plugin plugin, String prefix, boolean cancelable, Prompt prompt) {
    var factory = new ConversationFactory(plugin).withPrefix(context -> prefix)
        .withFirstPrompt(prompt).withLocalEcho(false);
    if (cancelable) {
      factory.withEscapeSequence("cancel");
    }
    factory.addConversationAbandonedListener(this)
        .thatExcludesNonPlayersWithMessage("プレイヤーしか入力できません");
    this.factory = factory;
  }

  public PlayerChatInput(Plugin plugin, String prefix, boolean cancelable, Prompt prompt,
      int timeout) {
    this(plugin, prefix, cancelable, prompt);
    this.factory.withTimeout(timeout);
  }

  public final void build(Player player) {
    if (isInputted(player)) {
      throw new RuntimeException(
          "チャット入力中なのにまた入力のメゾットを使用しています player = " + player.getName());
    } else {
      factory.buildConversation(player).begin();
    }
  }

  @Override
  public void conversationAbandoned(@NotNull ConversationAbandonedEvent abandonedEvent) {
    if (!abandonedEvent.gracefulExit()) {
      ConversationCanceller canceller = abandonedEvent.getCanceller();
      Conversable forWhom = abandonedEvent.getContext().getForWhom();
      if (canceller instanceof InactivityConversationCanceller) {
        forWhom.sendRawMessage(getTimeOutCancel());
      } else if (canceller instanceof ExactMatchConversationCanceller) {
        forWhom.sendRawMessage(getUserCancel());
      } else if (canceller instanceof ManuallyAbandonedConversationCanceller) {
        forWhom.sendRawMessage(getResetCancel());
      }
    }
  }

  public String getTimeOutCancel() {
    return "タイムアウトです";
  }

  public String getUserCancel() {
    return "入力をキャンセルしました";
  }

  public String getResetCancel() {
    return "入力をリセットしました";
  }

}
