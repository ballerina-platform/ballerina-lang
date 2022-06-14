package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;

public class SetClosureMap {

    public static void setClosureMap(BObject obj, BMap<?, ?> value) {
        BObject objectValue = (BObject) obj;
        objectValue.setClosureMap(value);
    }
}
