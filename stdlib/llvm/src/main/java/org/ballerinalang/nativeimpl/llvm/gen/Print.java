package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "print",
        args = {@Argument(name = "values", type = TypeKind.INT)},
        isPublic = true
)
public class Print {
    public static void print(int value) {

        System.out.println(value);
    }
}
