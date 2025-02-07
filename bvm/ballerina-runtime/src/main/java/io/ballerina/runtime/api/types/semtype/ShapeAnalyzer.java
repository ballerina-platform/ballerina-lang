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

    public static Optional<SemType> acceptedTypeOf(Context cx, Type typeDesc) {
        if (typeDesc instanceof TypeWithAcceptedType typeWithAcceptedType) {
            return typeWithAcceptedType.acceptedTypeOf(cx);
        }
        return Optional.of(SemType.tryInto(cx, typeDesc));
    }

    public static Optional<SemType> shapeOf(Context cx, Object object) {
        if (object == null) {
            return Optional.of(Builder.getNilType());
        } else if (object instanceof DecimalValue decimalValue) {
            return Optional.of(Builder.getDecimalConst(decimalValue.value()));
        } else if (object instanceof Double doubleValue) {
            return Optional.of(Builder.getFloatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(Builder.getIntConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(Builder.getBooleanConst(booleanValue));
        } else if (object instanceof BString stringValue) {
            return Optional.of(Builder.getStringConst(stringValue.getValue()));
        } else if (object instanceof BValue bValue) {
            Type type = bValue.getType();
            if (type instanceof TypeWithShape typeWithShape) {
                return typeWithShape.shapeOf(cx, ShapeAnalyzer::shapeOf, object);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<SemType> inherentTypeOf(Context cx, Object object) {
        if (object instanceof BValue bValue) {
            return bValue.inherentTypeOf(cx);
        }
        if (object == null) {
            return Optional.of(Builder.getNilType());
        } else if (object instanceof Double doubleValue) {
            return Optional.of(Builder.getFloatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(Builder.getIntConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(Builder.getBooleanConst(booleanValue));
        }
        return Optional.empty();
    }
}
