package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMPassManagerBuilderPopulateModulePassManager;

@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmPassManagerBuilderPopulateModulePassManager",
        args = {
                @Argument(name = "pmb", type = RECORD, structType = "LLVMPassManagerBuilderRef"),
                @Argument(name = "pm", type = RECORD, structType = "LLVMPassManagerRef"),
        })
public class LLVMPassManagerBuilderPopulateModulePassManager {

    public static void llvmPassManagerBuilderPopulateModulePassManager(Strand strand, MapValue<String, Object> arg0,
                                                                       MapValue<String, Object> arg1) {

        LLVM.LLVMPassManagerBuilderRef pmb = (LLVM.LLVMPassManagerBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVM.LLVMPassManagerRef pm = (LLVM.LLVMPassManagerRef) FFIUtil.getRecodeArgumentNative(arg1);
        LLVMPassManagerBuilderPopulateModulePassManager(pmb, pm);
    }
}
