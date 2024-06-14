package github.moriyoshi.comminiplugin.lib;

import github.moriyoshi.comminiplugin.lib.tuple.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.val;

public class RandomCollection<E> {
  private final List<Pair<Double, E>> list = new ArrayList<>();
  private final Random random;
  private double total = 0.0;

  public RandomCollection() {
    this(new Random());
  }

  public RandomCollection(Random random) {
    this.random = random;
  }

  public RandomCollection<E> add(double weight, E result) {
    if (weight <= 0) return this;
    list.add(Pair.of(weight, result));
    total += weight;
    return this;
  }

  public E next() {
    double value = random.nextDouble() * total;
    for (val pair : list) {
      value -= pair.getFirst();
      if (value < 0) return pair.getSecond();
    }
    return list.get(list.size() - 1).getSecond();
  }

  public boolean remove(E e) {
    val iter = list.iterator();
    while (iter.hasNext()) {
      val pair = iter.next();
      if (pair.getSecond().equals(e)) {
        total -= pair.getFirst();
        iter.remove();
        return true;
      }
    }
    return false;
  }
}
