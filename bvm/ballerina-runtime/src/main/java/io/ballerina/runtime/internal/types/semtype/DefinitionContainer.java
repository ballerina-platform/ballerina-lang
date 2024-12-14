package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

// FIXME: doc
/**
 * Container used to maintain concurrency invariants when creating a potentially recursive semtype.
 *
 * It maintains fallowing invariants.
 * 1. When the type is being defined only the thread that is defining the type may proceed
 * 2. After definition is completed any number of threads may proceed concurrently In order to achieve this container
 * has three phases (init, defining, defined). At init phase (that is no definition has been set) any number of threads
 * may proceed concurrently. When a thread sets a definition {@code setDefinition} container enters the defining phase.
 * In that phase only that thread may continue in {@code getSemType} method (this is to allow for recursive type
 * definitions). Container registers with the {@code Definition} using {@code registerContainer} method. When the
 * {@code Definition} has been defined (ie. {@code Env} has an atom corresponding to the definition) it must notify the
 * container using {@code definitionUpdated} method. At this point container moves to defined phase allowing concurrent
 * access to {@code getSemType}.
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
