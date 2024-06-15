package github.moriyoshi.comminiplugin.lib;

import java.util.Iterator;
import java.util.List;

public class InfiniteIterator<E> implements Iterator<E> {

  private final List<E> list;
  private int index = 0;

  public InfiniteIterator(List<E> elements) {
    this.list = elements;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public E next() {
    if (list.size() == index) {
      index = 0;
      return list.get(index++);
    }
    return list.get(index++);
  }
}
