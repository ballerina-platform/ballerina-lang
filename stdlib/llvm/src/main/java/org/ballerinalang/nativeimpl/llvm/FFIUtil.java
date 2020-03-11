package org.ballerinalang.nativeimpl.llvm;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.bytedeco.javacpp.Pointer;

/**
 * sss.
 */
public class FFIUtil {
    private static final String NATIVE_KEY = "native";

    public static MapValue<String, Object> newRecord() {
        return new MapValueImpl<String, Object>();
    }

    public static MapValue<String, Object> newRecord(BType type) {
        return new MapValueImpl<String, Object>(type);
    }

    public static MapValue<String, Object> newRecord(BPackage packageObj, String structName) {
        return BallerinaValues.createRecordValue(packageObj, structName);
    }

    public static Object getRecodeArgumentNative(MapValue<String, Object> value) {
        return value.getNativeData(NATIVE_KEY);
    }

    public static Pointer[] getRecodeArrayArgumentNative(ArrayValue value) {
        Pointer[] objectArray = new Pointer[value.size()];
        for (int counter = 0; counter < value.size(); counter++) {
            MapValue<String, Object> partition = (MapValue<String, Object>) value.getRefValue(counter);
            Pointer object = (Pointer) partition.getNativeData(NATIVE_KEY);
            objectArray[counter] = object;
        }
        return objectArray;
    }

    public static void addNativeToRecode(Object module, MapValue<String, Object> mapValue) {
        mapValue.addNativeData(NATIVE_KEY, module);
    }

}
