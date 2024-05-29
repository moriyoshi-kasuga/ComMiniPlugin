package github.moriyoshi.comminiplugin.util;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.reflections.Reflections;

public class ReflectionUtil {

  private ReflectionUtil() {
  }

  public static <T> void forEachAllClass(Reflections reflections, Class<? extends T> clazz,
      Consumer<Class<? extends T>> consumer) {
    for (Class<? extends T> item : reflections.getSubTypesOf(clazz)) {
      if (Modifier.isAbstract(item.getModifiers())) {
        forEachAllClass(reflections, item, consumer);
        return;
      }
      consumer.accept(item);
    }
  }

  public static <T> void forEachClass(Reflections reflections, Class<T> clazz,
      Consumer<Class<? extends T>> consumer) {
    for (Class<? extends T> item : reflections.getSubTypesOf(clazz)) {
      if (Modifier.isAbstract(item.getModifiers())) {
        return;
      }
      consumer.accept(item);
    }
  }

}
