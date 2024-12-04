package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

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

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile E definition;

    private final ReentrantLock recTypeLock = new ReentrantLock();
    private volatile boolean isDefining = false;

    public boolean isDefinitionReady() {
        try {
            rwLock.readLock().lock();
            return definition != null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public SemType getSemType(Env env) {
        try {
            rwLock.readLock().lock();
            // We don't need this check to be synchronized since {@code trySetDefinition} will hold the write lock until
            // it completes, So isDefining should always be at a consistent state
            if (isDefining) {
                // This should prevent threads other than the defining thread to access the rec atom.
                recTypeLock.lock();
            }
            return definition.getSemType(env);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public DefinitionUpdateResult<E> trySetDefinition(Supplier<E> supplier) {
        try {
            rwLock.writeLock().lock();
            boolean updated;
            E newDefinition;
            if (this.definition != null) {
                updated = false;
                newDefinition = null;
            } else {
                updated = true;
                newDefinition = supplier.get();
                newDefinition.registerContainer(this);
                this.recTypeLock.lock();
                isDefining = true;
                this.definition = newDefinition;
            }
            return new DefinitionUpdateResult<>(newDefinition, updated);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void clear() {
        try {
            rwLock.writeLock().lock();
            // This shouldn't happen because defining thread should hold the lock.
            assert !isDefining;
            this.definition = null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void definitionUpdated() {
        recTypeLock.unlock();
        isDefining = false;
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
