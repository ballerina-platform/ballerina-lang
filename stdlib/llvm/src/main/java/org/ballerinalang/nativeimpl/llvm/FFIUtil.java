package org.ballerinalang.nativeimpl.llvm;

import io.ballerina.runtime.BallerinaValues;
import io.ballerina.runtime.api.runtime.Module;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.bytedeco.javacpp.Pointer;

/**
 * sss.
 */
public class FFIUtil {
    private static final String NATIVE_KEY = "native";

    public static BMap<String, Object> newRecord() {
        return new BMap<String, Object>();
    }

    public static BMap<String, Object> newRecord(Type type) {
        return new BMap<String, Object>(type);
    }

    public static BMap<String, Object> newRecord(Module packageObj, String structName) {
        return BallerinaValues.createRecordValue(packageObj, structName);
    }

    public static Object getRecodeArgumentNative(BMap<String, Object> value) {
        return value.getNativeData(NATIVE_KEY);
    }

    public static Pointer[] getRecodeArrayArgumentNative(BArray value) {
        Pointer[] objectArray = new Pointer[value.size()];
        for (int counter = 0; counter < value.size(); counter++) {
            BMap<String, Object> partition = (BMap<String, Object>) value.getRefValue(counter);
            Pointer object = (Pointer) partition.getNativeData(NATIVE_KEY);
            objectArray[counter] = object;
        }
        return objectArray;
    }

    public static void addNativeToRecode(Object module, BMap<String, Object> BMap) {
        BMap.addNativeData(NATIVE_KEY, module);
    }

}
