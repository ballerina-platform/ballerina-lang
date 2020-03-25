package org.ballerinalang.langlib.compilertest;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.compilertest", functionName = "assertValueEqual",
        args = {@Argument(name = "expected", type = TypeKind.ANYDATA), @Argument(name = "actual", type = TypeKind.ANYDATA)},
        isPublic = true
)
public class AssertValueEqual {
    public static void assertValueEqual(Strand strand, Object expected, Object actual) {
        if (TypeChecker.isEqual(expected, actual)) {
        } else {
            throw BallerinaErrors.createError("Not Equal");
        }
    }
}