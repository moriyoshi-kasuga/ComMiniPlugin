package github.moriyoshi.comminiplugin.util;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import org.reflections.Reflections;

public class ReflectionUtil {

  private ReflectionUtil() {}

  public static <T> void forEachAllClass(
      final Reflections reflections,
      final Class<? extends T> clazz,
      final Consumer<Class<? extends T>> consumer) {
    for (final Class<? extends T> item : reflections.getSubTypesOf(clazz)) {
      if (Modifier.isAbstract(item.getModifiers())) {
        continue;
      }
      consumer.accept(item);
    }
  }
}
