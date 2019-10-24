package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMDisposeBuilder;

@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmDisposeBuilder",
        args = {
                @Argument(name = "builder", type = RECORD, structType = "LLVMBuilderRef"),
        })
public class LLVMDisposeBuilder {
    public static void llvmDisposeBuilder(Strand strand, MapValue<String, Object> fn) {
        LLVM.LLVMBuilderRef builder = (LLVM.LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(fn);
        LLVMDisposeBuilder(builder);
    }
}
