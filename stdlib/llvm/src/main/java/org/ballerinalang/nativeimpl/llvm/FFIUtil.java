package org.ballerinalang.nativeimpl.llvm;

import java.util.function.IntFunction;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * sss.
 */
public class FFIUtil {
    private static final String NATIVE_KEY = "native";

    public static MapValue<String, Object> newRecord() {
        return new MapValueImpl<String, Object>();
    }

    public static Object getRecodeArgumentNative(MapValue<String, Object> value) {
        return value.getStringValue(NATIVE_KEY);
    }

    public static Object[] getRecodeArrayArgumentNative(ArrayValue value) {
        Object[] objectArray = new Object[value.size()];
        for (int counter = 0; counter < value.size(); counter++) {
            MapValue<String, Object> partition = (MapValue<String, Object>) value.get(counter);
            Object object = partition.get(NATIVE_KEY);
            objectArray[counter] = object;
        }
        return objectArray;
    }

    public static void addNativeToRecode(Object module, MapValue<String, Object> mapValue) {
        mapValue.addNativeData(NATIVE_KEY, module);
    }

}
