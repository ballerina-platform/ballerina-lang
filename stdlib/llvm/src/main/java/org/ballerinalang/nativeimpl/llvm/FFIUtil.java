package org.ballerinalang.nativeimpl.llvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * sss.
 */
public class FFIUtil {
    public static final String LLVM_PKG_PATH = BALLERINA_PACKAGE_PREFIX + "llvm";

    public static BMap<String, BValue> newRecord(Context context, String type) {
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(LLVM_PKG_PATH);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("package - " + LLVM_PKG_PATH + " does not exist");
        }
        StructureTypeInfo structureInfo = packageInfo.getStructInfo(type);
        BStructureType structType = structureInfo.getType();
        return new BMap<>(structType);
    }

    public static <T> T getRecodeArgumentNative(Context context, int index) {
        BMap<String, BValue> moduleRecode = (BMap<String, BValue>) context.getRefArgument(index);
        return (T) moduleRecode.getNativeData("native");
    }


    public static void addNativeToRecode(Object module, BMap<String, BValue> bStruct) {
        bStruct.addNativeData("native", module);
    }
}
