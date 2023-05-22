package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

public class CommonUtils {
    public static Object sleep(BDecimal seconds) {
        try {
            Thread.sleep(seconds.intValue() * 1000);
        } catch (InterruptedException e) {
            return BLangExceptionHelper.getRuntimeException(
                    RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, "Invalid duration: " + e.getMessage());
        }
        return null;
    }

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
