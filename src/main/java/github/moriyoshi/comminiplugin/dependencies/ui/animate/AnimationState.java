package github.moriyoshi.comminiplugin.dependencies.ui.animate;

/** {@link AnimationRunner}のステータスです。 */
public enum AnimationState {

  /** アニメーションランナーがまだアニメーションの再生を開始していないことを示す。 */
  NOT_STARTED,
  /** アニメーションランナーが、現在アニメーションの実行に追われていることを示す。 */
  RUNNING,
  /** アニメーションが{@link AnimationRunner#stop()}の呼び出しで中断されたことを示す。 */
  PAUSED,
  /** アニメーションの再生が終了したことを示す。これは、アニメーションのフレームが切れたときに起こることがある。 */
  FINISHED
}
