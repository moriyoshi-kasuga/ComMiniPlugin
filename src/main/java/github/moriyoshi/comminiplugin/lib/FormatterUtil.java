package github.moriyoshi.comminiplugin.lib;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** {key}をその値によって文字列をフォーマットする */
public final class FormatterUtil {

  // {\w+} → \w+
  private static final Pattern pattern = Pattern.compile("\\{(\\w+)}");

  private FormatterUtil() {}

  /**
   * 文字列をマップ名によってフォーマットします。
   *
   * @param format フォーマットする文字列
   * @param map map
   * @return mapの値によってフォーマットされた文字列
   */
  public static String format(final String format, final Map<Object, Object> map) {
    final Matcher matcher = pattern.matcher(format);
    final StringBuilder sb = new StringBuilder();
    int lastEnd = 0;
    while (matcher.find()) {
      sb.append(format, lastEnd, matcher.start());
      lastEnd = matcher.end();
      final Object parameter = map.get(matcher.group(1));
      if (parameter == null) {
        continue;
      }
      sb.append(parameter);
    }
    sb.append(format.substring(lastEnd));
    return sb.toString();
  }

  /**
   * 文字列をプロパティ名によってフォーマットします。
   *
   * @param format フォーマットする文字列
   * @param props プロパティ
   * @return プロパティの値によってフォーマットされた文字列
   */
  public static String format(final String format, final Properties props) {
    final Matcher matcher = pattern.matcher(format);
    final StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      final String parameter = props.getProperty(matcher.group(1));
      if (parameter == null) {
        continue;
      }
      matcher.appendReplacement(sb, parameter);
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
