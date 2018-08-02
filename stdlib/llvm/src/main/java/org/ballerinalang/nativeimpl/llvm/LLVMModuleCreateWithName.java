package org.ballerinalang.nativeimpl.llvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.bytedeco.javacpp.LLVM.LLVMModuleRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;
import static org.bytedeco.javacpp.LLVM.LLVMModuleCreateWithName;

/**
 * Create llvm module.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMModuleCreateWithName",
        args = {
                @Argument(name = "moduleName", type = STRING),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMModuleRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMModuleCreateWithName extends BlockingNativeCallableUnit {
    public static final String LLVM_PKG_PATH = BALLERINA_PACKAGE_PREFIX + "llvm";

    @Override
    public void execute(Context context) {
        String moduleName = context.getStringArgument(0);
        LLVMModuleRef module = LLVMModuleCreateWithName(moduleName);

        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(LLVM_PKG_PATH);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("package - " + LLVM_PKG_PATH + " does not exist");
        }

        StructureTypeInfo structureInfo = packageInfo.getStructInfo("LLVMModuleRef");
        BStructureType structType = structureInfo.getType();
        BMap<String, BValue> bStruct = new BMap<>(structType);
        bStruct.addNativeData("native", module);
        context.setReturnValues(bStruct);
    }
}
