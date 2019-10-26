package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMCreatePassManager;

@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmCreatePassManager",
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMPassManagerRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMCreatePassManager {

    public static MapValue<String, Object> llvmCreatePassManager(Strand strand) {

        LLVM.LLVMPassManagerRef returnValue = LLVMCreatePassManager();
        MapValue<String, Object> rerunWrapperRecode = FFIUtil.newRecord(new BPackage("ballerina",
                "llvm"), "LLVMPassManagerRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        return rerunWrapperRecode;
    }
}
