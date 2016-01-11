package org.wso2.siddhi.core.util.statistics.memory.measurer;

/**
 * A visitor that controls an object traversal. Implementations
 * of this interface are passed to {@link ObjectExplorer} exploration methods.
 *
 * @param <T> the type of the result that this visitor returns
 * (can be defined as {@code Void} to denote no result}.
 * @see ObjectExplorer
 */
public interface ObjectVisitor<T> {
  /**
   * Visits an explored value (the whole chain from the root object
   * leading to the value is provided), and decides whether to continue
   * the exploration of that value.
   *
   * <p>In case the explored value is either primitive or {@code null}
   * (e.g., if {@code chain.isPrimitive() || chain.getValue() == null}),
   * the return value is meaningless and is ignored.
   *
   * @param chain the chain that leads to the explored value.
   * @return {@link Traversal#EXPLORE} to denote that the visited object
   * should be further explored, or {@link Traversal#SKIP} to avoid
   * exploring it.
   */
  Traversal visit(Chain chain);

  /**
   * @return  an arbitrary value (presumably constructed during the object
   * graph traversal).
   */
  T result();

  /**
   * Constants that denote how the traversal of a given object (chain)
   * should continue.
   */
  enum Traversal {
    /**
     * The visited object should be further explored.
     */
    EXPLORE,

    /**
     * The visited object should not be explored.
     */
    SKIP
  }
}
