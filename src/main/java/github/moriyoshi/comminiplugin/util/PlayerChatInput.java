package github.moriyoshi.comminiplugin.util;

import lombok.val;
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

  private final ConversationFactory factory;

  public PlayerChatInput(
      final Plugin plugin, final String prefix, final boolean cancelable, final Prompt prompt) {
    val factory =
        new ConversationFactory(plugin)
            .withPrefix(context -> prefix)
            .withFirstPrompt(prompt)
            .withLocalEcho(false);
    if (cancelable) {
      factory.withEscapeSequence("cancel");
    }
    factory
        .addConversationAbandonedListener(this)
        .thatExcludesNonPlayersWithMessage("プレイヤーしか入力できません");
    this.factory = factory;
  }

  public PlayerChatInput(
      final Plugin plugin,
      final String prefix,
      final boolean cancelable,
      final Prompt prompt,
      final int timeout) {
    this(plugin, prefix, cancelable, prompt);
    this.factory.withTimeout(timeout);
  }

  public static boolean isInputted(final Player player) {
    return player.isConversing();
  }

  public final void build(final Player player) {
    if (isInputted(player)) {
      throw new RuntimeException("チャット入力中なのにまた入力のメゾットを使用しています player = " + player.getName());
    } else {
      factory.buildConversation(player).begin();
    }
  }

  @Override
  public void conversationAbandoned(@NotNull final ConversationAbandonedEvent abandonedEvent) {
    if (!abandonedEvent.gracefulExit()) {
      final ConversationCanceller canceller = abandonedEvent.getCanceller();
      final Conversable forWhom = abandonedEvent.getContext().getForWhom();
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
