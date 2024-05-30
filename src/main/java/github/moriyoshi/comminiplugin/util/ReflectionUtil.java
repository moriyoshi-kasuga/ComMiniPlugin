package github.moriyoshi.comminiplugin.util;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.reflections.Reflections;

public class ReflectionUtil {

  public static <T> void forEachAllClass(final Reflections reflections, final Class<? extends T> clazz,
      final Consumer<Class<? extends T>> consumer) {
    for (final Class<? extends T> item : reflections.getSubTypesOf(clazz)) {
      if (Modifier.isAbstract(item.getModifiers())) {
        forEachAllClass(reflections, item, consumer);
        return;
      }
      consumer.accept(item);
    }
  }

  public static <T> void forEachOnlyClass(final Reflections reflections, final Class<T> clazz,
      final Consumer<Class<? extends T>> consumer) {
    for (final Class<? extends T> item : reflections.getSubTypesOf(clazz)) {
      if (Modifier.isAbstract(item.getModifiers())) {
        return;
      }
      consumer.accept(item);
    }
  }

  private ReflectionUtil() {
  }

}
