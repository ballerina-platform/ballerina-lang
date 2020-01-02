package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.llvm.LLVM.LLVMTargetMachineRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.BYTE;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMTargetMachineEmitToFile;

/**
 * Auto generated class.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmTargetMachineEmitToFile",
        args = {
                @Argument(name = "t", type = RECORD, structType = "LLVMTargetMachineRef"),
                @Argument(name = "m", type = RECORD, structType = "LLVMModuleRef"),
                @Argument(name = "filename", type = ARRAY, elementType = BYTE),
                @Argument(name = "codegen", type = INT),
                @Argument(name = "errorMessage", type = RECORD, structType = "PointerPointer"),
        },
        returnType = {
                @ReturnType(type = INT, structPackage = "ballerina/llvm"),
        }
)
public class LLVMTargetMachineEmitToFile {

    public static long llvmTargetMachineEmitToFile(Strand strand, MapValue<String, Object> arg0, MapValue<String,
            Object> arg1, ArrayValue arg2, long arg3, ArrayValue arg4) {

        LLVMTargetMachineRef t = (LLVMTargetMachineRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVMModuleRef m = (LLVMModuleRef) FFIUtil.getRecodeArgumentNative(arg1);

        return LLVMTargetMachineEmitToFile(t, m, arg2.getBytes(), (int) arg3, arg4.getBytes());
    }
}
