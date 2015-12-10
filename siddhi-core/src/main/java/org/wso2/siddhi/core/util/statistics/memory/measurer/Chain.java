package org.wso2.siddhi.core.util.statistics.memory.measurer;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A chain of references, which starts at a root object and leads to a
 * particular value (either an object or a primitive).
 *
 * @author andreou
 */
public abstract class Chain {
  private final Object value;
  private final Chain parent;

  Chain(Chain parent, Object value) {
    this.parent = parent;
    this.value = value;
  }

  static Chain root(Object value) {
    return new Chain(null, Preconditions.checkNotNull(value)) {
      @Override
      public Class<?> getValueType() {
        return getValue().getClass();
      }
    };
  }

  FieldChain appendField(Field field, Object value) {
    return new FieldChain(this, Preconditions.checkNotNull(field), value);
  }

  ArrayIndexChain appendArrayIndex(int arrayIndex, Object value) {
    return new ArrayIndexChain(this, arrayIndex, value);
  }

  /**
   * Returns whether this chain has a parent. This returns false only when
   * this chain represents the root object itself.
   */
  public boolean hasParent() {
    return parent != null;
  }

  /**
   * Returns the parent chain, from which this chain was created.
   * @throws IllegalStateException if {@code !hasParent()}, then an
   */
  public /*@Nonnull*/ Chain getParent() {
    Preconditions.checkState(parent != null, "This is the root value, it has no parent");
    return parent;
  }

  /**
   * Returns the value that this chain leads to. If the value is a primitive,
   * a wrapper object is returned instead.
   */
  public /*@Nullable*/ Object getValue() {
    return value;
  }

  public abstract /*@Nonnull*/ Class<?> getValueType();

  /**
   * Returns whether the connection of the parent chain and this chain is
   * through a field (of the getParent().getValue().getClass() class).
   */
  public boolean isThroughField() {
    return false;
  }

  /**
   * Returns whether the connection of the parent chain and this chain is
   * through an array index, i.e. the parent leads to an array, and this
   * chain leads to an element of that array.
   */
  public boolean isThroughArrayIndex() {
    return false;
  }

  /**
   * Returns whether the value of this chain represents a primitive.
   */
  public boolean isPrimitive() {
    return getValueType().isPrimitive();
  }

  /**
   * Returns the root object of this chain.
   */
  public /*@Nonnull*/ Object getRoot() {
    Chain current = this;
    while (current.hasParent()) {
      current = current.getParent();
    }
    return current.getValue();
  }

  Deque<Chain> reverse() {
    Deque<Chain> reverseChain = new ArrayDeque<Chain>(8);
    Chain current = this;
    reverseChain.addFirst(current);
    while (current.hasParent()) {
      current = current.getParent();
      reverseChain.addFirst(current);
    }
    return reverseChain;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder(32);

    Iterator<Chain> it = reverse().iterator();
    sb.append(it.next().getValue());
    while (it.hasNext()) {
      sb.append("->");
      Chain current = it.next();
      if (current.isThroughField()) {
        sb.append(((FieldChain)current).getField().getName());
      } else if (current.isThroughArrayIndex()) {
        sb.append("[").append(((ArrayIndexChain)current).getArrayIndex()).append("]");
      }
    }
    return sb.toString();
  }

  static class FieldChain extends Chain {
    private final Field field;

    FieldChain(Chain parent, Field referringField, Object value) {
      super(parent, value);
      this.field = referringField;
    }

    @Override
    public boolean isThroughField() {
      return true;
    }

    @Override
    public boolean isThroughArrayIndex() {
      return false;
    }

    @Override
    public Class<?> getValueType() {
      return field.getType();
    }

    public Field getField() {
      return field;
    }
  }

  static class ArrayIndexChain extends Chain {
    private final int index;

    ArrayIndexChain(Chain parent, int index, Object value) {
      super(parent, value);
      this.index = index;
    }

    @Override
    public boolean isThroughField() {
      return false;
    }

    @Override
    public boolean isThroughArrayIndex() {
      return true;
    }

    @Override
    public Class<?> getValueType() {
      return getParent().getValue().getClass().getComponentType();
    }

    public int getArrayIndex() {
      return index;
    }
  }
}
