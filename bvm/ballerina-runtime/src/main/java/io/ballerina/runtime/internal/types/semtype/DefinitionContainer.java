package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Container used to maintain concurrent safety when creating {@code Definitions}.
 *
 * @param <E> type of the definition
 * @since 2201.11.0
 */
public class DefinitionContainer<E extends Definition> {

    private volatile E definition;

    private final ReentrantLock lock = new ReentrantLock();

    public boolean isDefinitionReady() {
        try {
            lock.lock();
            return definition != null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the semtype of the definition. Must call {@code isDefinitionReady} before calling this method.
     *
     * @param env {@code Env} in which type is defined at
     * @return recursive semtype representing the type
     */
    public SemType getSemType(Env env) {
        return definition.getSemType(env);
    }

    public DefinitionUpdateResult<E> trySetDefinition(Supplier<E> supplier) {
        try {
            lock.lock();
            boolean updated;
            E newDefinition;
            if (this.definition != null) {
                updated = false;
                newDefinition = null;
            } else {
                updated = true;
                newDefinition = supplier.get();
                this.definition = newDefinition;
            }
            return new DefinitionUpdateResult<>(newDefinition, updated);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        try {
            lock.lock();
            this.definition = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Result of trying to update the definition.
     *
     * @param <E>        Type of the definition
     * @param updated    If update was successful. If this failed you must get the semtype using the {@code getSemType}
     *                   method of the container
     * @param definition If update was successful this will be the new definition. Otherwise, this will be null
     * @since 2201.11.0
     */
    public record DefinitionUpdateResult<E>(E definition, boolean updated) {

    }
}
