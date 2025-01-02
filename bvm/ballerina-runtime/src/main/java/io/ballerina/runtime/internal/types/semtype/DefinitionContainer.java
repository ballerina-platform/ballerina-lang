package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Container used to maintain concurrent safety when creating {@code Definitions}.
 *
 * @param <E> type of the definition
 * @since 2201.11.0
 */
public class DefinitionContainer<E extends Definition> {

    private final AtomicReference<E> definition = new AtomicReference<>();

    public boolean isDefinitionReady() {
        return definition.get() != null;
    }

    /**
     * Get the semtype of the definition. Must call {@code isDefinitionReady} before calling this method.
     *
     * @param env {@code Env} in which type is defined at
     * @return recursive semtype representing the type
     */
    public SemType getSemType(Env env) {
        return definition.get().getSemType(env);
    }

    /**
     * Try to set the definition. If the definition is already set, this will not update the definition.
     *
     * @param supplier supplier to get the definition. Calling this should not have side effects
     * @return result of the update
     */
    public DefinitionUpdateResult<E> trySetDefinition(Supplier<E> supplier) {
        if (isDefinitionReady()) {
            return new DefinitionUpdateResult<>(definition.get(), false);
        }
        boolean updated = definition.compareAndSet(null, supplier.get());
        return new DefinitionUpdateResult<>(definition.get(), updated);
    }

    public void clear() {
        this.definition.set(null);
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
