
package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BFunctionPointer;


/**
 * Common utility functions for the Testerina module.
 *
 * @since 2201.7.0
 */
public class CommonUtils {
    public static BDecimal currentTimeInMillis() {
        long currentTime = System.currentTimeMillis();
        return BDecimal.valueOf(currentTime);
    }

    public static Object isFunctionParamConcurrencySafe(BFunctionPointer func) {
        FunctionType functionType = (FunctionType) func.getType();
        Parameter[] functionParameters = functionType.getParameters();
        for (Parameter functionParameter : functionParameters) {
            Type parameterType = functionParameter.type;
            if (isSubTypeOfReadOnlyOrIsolatedObjectUnion(parameterType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isSubTypeOfReadOnlyOrIsolatedObjectUnion(Type type) {
        if (type.isReadOnly()) {
            return true;
        }

        if (type instanceof ObjectType) {
            return ((ObjectType) type).isIsolated();
        }

        if (!(type instanceof UnionType)) {
            return false;
        }

        for (Type memberType : ((UnionType) type).getMemberTypes()) {
            if (!isSubTypeOfReadOnlyOrIsolatedObjectUnion(memberType)) {
                return false;
            }
        }
        return true;
    }
}