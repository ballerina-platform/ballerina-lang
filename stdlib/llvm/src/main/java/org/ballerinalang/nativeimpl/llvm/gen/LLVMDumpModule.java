package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMDumpModule;

@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmDumpModule",
        args = {
                @Argument(name = "m", type = RECORD, structType = "LLVMModuleRef"),
        })
public class LLVMDumpModule {

    public static void llvmDumpModule(Strand strand, MapValue<String, Object> fn) {
        LLVM.LLVMModuleRef m = (LLVM.LLVMModuleRef) FFIUtil.getRecodeArgumentNative(fn);
        LLVMDumpModule(m);
    }
}
