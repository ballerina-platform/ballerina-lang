package org.ballerinalang.langlib.compilertest;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.compilertest", functionName = "assertNotError",
        args = {@Argument(name = "value", type = TypeKind.UNION)},
        isPublic = true
)
public class AssertNotError {
    public static void assertNotError(Strand strand, Object value) {
        if (value instanceof ErrorValue) {
            throw BallerinaErrors.createError("Error");
        }
    }
}