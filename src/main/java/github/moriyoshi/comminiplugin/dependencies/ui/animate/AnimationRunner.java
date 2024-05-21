package github.moriyoshi.comminiplugin.dependencies.ui.animate;

import github.moriyoshi.comminiplugin.dependencies.ui.button.MenuButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.dependencies.ui.util.IntBiConsumer;
import java.util.Objects;
import java.util.OptionalLong;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * {@link Animation}のプレイヤーです。
 *
 * @param <Item> このアニメーションプレーヤーが使用されるコンテナ内のアイテムの種類。
 */
public final class AnimationRunner<Item> {

  private final Plugin plugin;
  private final Animation animation;
  private final IntBiConsumer<Item> container;

  private AnimationState status = AnimationState.NOT_STARTED;
  private BukkitTask task = null;

  /**
   * AnimationRunnerを作成する。
   *
   * @param plugin    アニメーションタスクの実行に使用されるプラグイン
   * @param animation アニメ
   * @param container コンテナを設定します。これは通常
   *                  {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)} または
   *                  {@link MenuHolder#setButton(int, MenuButton)} です。
   */
  public AnimationRunner(Plugin plugin, Animation animation, IntBiConsumer<Item> container) {
    this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    this.animation = Objects.requireNonNull(animation, "animation cannot be null");
    this.container = Objects.requireNonNull(container, "container cannot be null");
  }

  /**
   * このAnimationRunnerのステータスを取得します。
   *
   * @return ステータス
   */
  public AnimationState getStatus() {
    if (animation.hasNextFrame()) {
      if (task != null && !task.isCancelled()) {
        status = AnimationState.RUNNING;
      } else {
        status = AnimationState.PAUSED;
      }
    } else {
      status = AnimationState.FINISHED;
    }

    return status;
  }

  /**
   * アニメーションフレームをスケジュールに従って再生する。
   *
   * @param schedule スケジュール
   * @return アニメーションが正常に開始された場合はtrue、そうでない場合はfalse
   * @throws IllegalStateException アニメーションがすでに実行されている場合.
   * @see #getStatus()
   */
  public boolean play(Schedule schedule) {
    // throw IllegalStateException if the AnimationRunner was already busy.
    if (getStatus() == AnimationState.RUNNING) {
      throw new IllegalStateException("Animation already running");
    }

    // update the status
    status = AnimationState.RUNNING;

    // run the schedule
    return runSchedule(schedule);
  }

  /**
   * アニメーションの再生を停止させる。 このメソッドは、アニメーションがすでに一時停止または終了している場合は何もしません。
   */
  public void stop() {
    if (status != AnimationState.FINISHED) {
      status = AnimationState.PAUSED;
    }
    cancelTask();
  }

  /**
   * アニメーションをリセットして、もう一度最初からやり直せる状態にします。
   */
  public void reset() {
    cancelTask();
    animation.reset();
    status = AnimationState.NOT_STARTED;
  }

  private boolean runSchedule(Schedule schedule) {
    // 一般的なスケジュール構造をショートカットしてみる
    CommonRunnable sr = tryComputeCommonRunnable(schedule);
    boolean specialCased = false;
    if (sr != null) {
      // 少なくともScheduleRunnableは正常に取得できました。
      // (失敗するかもしれません)
      specialCased = trySpecialCaseRun(sr, schedule);
    }

    if (!specialCased) {
      // fallback では、スケジュール用の CommonRunnable を特別に用意することができませんでした。
      tryFallbackRun(schedule);
    }

    return getStatus() == AnimationState.RUNNING;
  }

  private boolean trySpecialCaseRun(CommonRunnable sr, Schedule schedule) {
    if (schedule instanceof OneTimeSchedule s) {
      if (s.when == 0L) {
        sr.run();
        task = null;
      } else if (s.when == 1L) {
        task = sr.runTask(plugin);
      } else {
        task = sr.runTaskLater(plugin, ((OneTimeSchedule) schedule).when);
      }
      return true;
    } else if (schedule instanceof FixedRateSchedule) {
      task = sr.runTaskTimer(plugin, 0L, ((FixedRateSchedule) schedule).period());
      return true;
    } else if (schedule instanceof StepLimitedSchedule) {
      return trySpecialCaseRun(sr, ((StepLimitedSchedule) schedule).source);
    } else if (schedule instanceof TimeLimitedSchedule) {
      return trySpecialCaseRun(sr, ((TimeLimitedSchedule) schedule).source);
    } else if (schedule instanceof ConcatSchedule concatSchedule) {
      if (concatSchedule.one() instanceof OneTimeSchedule
          && concatSchedule.two() instanceof RunFixedRate) {
        task = sr.runTaskTimer(plugin, ((OneTimeSchedule) concatSchedule.one()).when,
            ((RunFixedRate) concatSchedule.two()).period
        );
        return true;
      }
    }

    return false;
  }

  private CommonRunnable tryComputeCommonRunnable(Schedule schedule) {
    if (schedule instanceof OneTimeSchedule s) {
      return new RunOnce(s.when, () -> {
        this.showFrame();
        task = null;
      });
    } else if (schedule instanceof FixedRateSchedule s) {
      return new RunFixedRate(s.period(), this::showFrame);
    } else if (schedule instanceof ConcatSchedule s) {
      CommonRunnable one = tryComputeCommonRunnable(s.one());
      CommonRunnable two = tryComputeCommonRunnable(s.two());
      if (one != null && two != null) {
        return new RunConcat(one, two);
      }
    } else if (schedule instanceof StepLimitedSchedule s) {
      CommonRunnable sr = tryComputeCommonRunnable(s.source);
      if (sr != null) {
        return new RunStepLimited(sr, s.stepLimit, s.stepsPassed);
      }
    } else if (schedule instanceof TimeLimitedSchedule s) {
      CommonRunnable sr = tryComputeCommonRunnable(s.source);
      if (sr != null) {
        return new RunTimeLimited(sr, s.timeLimit, s.timePassed);
      }
    }

    return null;
  }

  private void tryFallbackRun(Schedule schedule) {
    OptionalLong nextTick = schedule.next();

    if (nextTick.isEmpty()) {
      this.status = AnimationState.FINISHED;
      stop();
    } else {
      long delay = nextTick.getAsLong();
      if (delay == 0) {
        showFrame();
        tryFallbackRun(schedule);
      } else {
        task = getScheduler().runTaskLater(plugin, () -> {
          showFrame();
          tryFallbackRun(schedule);
        }, delay);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void showFrame() {
    if (!animation.hasNextFrame()) {
      status = AnimationState.FINISHED;
      cancelTask();
    } else {
      Frame frame = animation.nextFrame();
      frame.apply(container);
    }
  }

  private void cancelTask() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  private BukkitScheduler getScheduler() {
    return plugin.getServer().getScheduler();
  }

}
