package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.Optional;

/**
 * Any {@code Type} that contains selectively immutable types must implement this interface. It represents the type
 * against which {@code isLikeType} operation is performed.
 */
public interface TypeWithAcceptedType {

    Optional<SemType> acceptedTypeOf(Context cx);
}
