package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DefinitionContainer<E extends Definition> {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile E definition;

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
            return definition.getSemType(env);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public DefinitionUpdateResult<E> setDefinition(Supplier<E> supplier) {
        try {
            rwLock.writeLock().lock();
            boolean updated;
            E newDefinition;
            if (this.definition != null) {
                updated = false;
                newDefinition = this.definition;
            } else {
                updated = true;
                newDefinition = supplier.get();
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
            this.definition = null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public record DefinitionUpdateResult<E>(E definition, boolean updated) {

    }
}
