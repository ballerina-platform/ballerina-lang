package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.types.TypeWithAcceptedType;
import io.ballerina.runtime.internal.types.TypeWithShape;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.util.Optional;

/**
 * Utility class for performing shape related operations.
 *
 * @since 2201.11.0
 */
public class ShapeAnalyzer {

    private ShapeAnalyzer() {
    }

    public static SemType acceptedTypeOf(Context cx, Type typeDesc) {
        if (typeDesc instanceof TypeWithAcceptedType typeWithAcceptedType) {
            return typeWithAcceptedType.acceptedTypeOf(cx);
        }
        return SemType.tryInto(cx, typeDesc);
    }

    public static Optional<SemType> shapeOf(Context cx, Object object) {
        switch (object) {
            case null -> {
                return Optional.of(Builder.getNilType());
            }
            case DecimalValue decimalValue -> {
                return Optional.of(Builder.getDecimalConst(decimalValue.value()));
            }
            case Double doubleValue -> {
                return Optional.of(Builder.getFloatConst(doubleValue));
            }
            case Number intValue -> {
                long value =
                        intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
                return Optional.of(Builder.getIntConst(value));
            }
            case Boolean booleanValue -> {
                return Optional.of(Builder.getBooleanConst(booleanValue));
            }
            case BString stringValue -> {
                return Optional.of(Builder.getStringConst(stringValue.getValue()));
            }
            case BValue bValue -> {
                Type type = bValue.getType();
                if (type instanceof TypeWithShape typeWithShape) {
                    return typeWithShape.shapeOf(cx, ShapeAnalyzer::shapeOf, object);
                } else {
                    return Optional.empty();
                }
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public static Optional<SemType> inherentTypeOf(Context cx, Object object) {
        switch (object) {
            case BValue bValue -> {
                return bValue.inherentTypeOf(cx);
            }
            case null -> {
                return Optional.of(Builder.getNilType());
            }
            case Double doubleValue -> {
                return Optional.of(Builder.getFloatConst(doubleValue));
            }
            case Number intValue -> {
                long value =
                        intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
                return Optional.of(Builder.getIntConst(value));
            }
            case Boolean booleanValue -> {
                return Optional.of(Builder.getBooleanConst(booleanValue));
            }
            default -> {
                return Optional.empty();
            }
        }
    }
}
