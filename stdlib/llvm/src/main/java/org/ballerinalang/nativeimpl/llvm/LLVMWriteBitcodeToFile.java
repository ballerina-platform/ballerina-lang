package org.ballerinalang.nativeimpl.llvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.bytedeco.javacpp.LLVM.LLVMWriteBitcodeToFile;

/**
 * Create llvm module.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMWriteBitcodeToFile",
        args = {
                @Argument(name = "module", type = RECORD, structType = "LLVMModuleRef"),
                @Argument(name = "path", type = STRING),
        }
)
public class LLVMWriteBitcodeToFile extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> moduleRecode = (BMap<String, BValue>) context.getRefArgument(0);
        String path = context.getStringArgument(0);
        LLVM.LLVMModuleRef module = (LLVM.LLVMModuleRef) moduleRecode.getNativeData("native");
        LLVMWriteBitcodeToFile(module, path);
    }
}
