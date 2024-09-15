package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.types.TypeWithShape;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.util.Optional;

public class ShapeAnalyzer {

    private ShapeAnalyzer() {
    }

    public static Optional<SemType> acceptedTypeOf(Context cx, Type typeDesc) {
        if (typeDesc instanceof TypeWithShape typeWithShape) {
            return typeWithShape.acceptedTypeOf(cx);
        }
        return Optional.of(SemType.tryInto(typeDesc));
    }

    public static Optional<SemType> shapeOf(Context cx, Object object) {
        if (object == null) {
            return Optional.of(Builder.nilType());
        } else if (object instanceof DecimalValue decimalValue) {
            return Optional.of(Builder.decimalConst(decimalValue.value()));
        } else if (object instanceof Double doubleValue) {
            return Optional.of(Builder.floatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(Builder.intConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(Builder.booleanConst(booleanValue));
        } else if (object instanceof BString stringValue) {
            return Optional.of(Builder.stringConst(stringValue.getValue()));
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
            return bValue.shapeOf(cx);
        }
        if (object == null) {
            return Optional.of(Builder.nilType());
        } else if (object instanceof Double doubleValue) {
            return Optional.of(Builder.floatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(Builder.intConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(Builder.booleanConst(booleanValue));
        }
        return Optional.empty();
    }
}
