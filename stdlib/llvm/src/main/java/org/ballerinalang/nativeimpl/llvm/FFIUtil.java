package org.ballerinalang.nativeimpl.llvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.function.IntFunction;

import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * sss.
 */
public class FFIUtil {
    public static final String LLVM_PKG_PATH = BALLERINA_PACKAGE_PREFIX + "llvm";
    public static final String NATIVE_KEY = "native";

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
        return getaNative(context.getRefArgument(index));
    }

    public static <T> T[] getRecodeArrayArgumentNative(Context context, int index, IntFunction<T[]> generator) {
        BRefValueArray refArray = (BRefValueArray) context.getRefArgument(index);
        T[] nativeArray = generator.apply((int) refArray.size());
        BRefType<?>[] values = refArray.getValues();
        for (int i = 0; i < refArray.size(); i++) {
            BRefType<?> bRefType = values[i];
            nativeArray[i] = getaNative(bRefType);
        }
        return nativeArray;
    }

    private static <T> T getaNative(BValue ref) {
        return (T) ((BMap<String, BValue>) ref).getNativeData(NATIVE_KEY);
    }


    public static void addNativeToRecode(Object module, BMap<String, BValue> bStruct) {
        bStruct.addNativeData(NATIVE_KEY, module);
    }

}
