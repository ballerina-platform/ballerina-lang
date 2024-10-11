package io.ballerina.runtime.api.types.semtype;

import java.util.function.Supplier;

/**
 * A thread-safe single lazy supplier that initialize the value only once.
 *
 * @param <E> type of the value
 * @since 2201.11.0
 */
public class ConcurrentLazySupplier<E> implements Supplier<E> {

    private Supplier<E> initializer;
    private volatile E value = null;

    public ConcurrentLazySupplier(Supplier<E> initializer) {
        this.initializer = initializer;
    }

    @Override
    public E get() {
        E result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    result = initializer.get();
                    assert result != null;
                    value = result;
                    initializer = null;
                }
            }
        }
        return result;
    }
}
