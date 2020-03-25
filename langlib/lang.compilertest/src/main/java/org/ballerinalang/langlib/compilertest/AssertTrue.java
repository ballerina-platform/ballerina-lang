package org.ballerinalang.langlib.compilertest;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.compilertest", functionName = "assertTrue",
        args = {@Argument(name = "value", type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class AssertTrue {
    public static void assertTrue(Strand strand, boolean value) {
        if (!value) {
            throw BallerinaErrors.createError("Not True");
        }
    }
}