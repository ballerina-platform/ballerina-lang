package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;

public class GetClosureMap {

    public static BMap<?, ?> getClosureMap(BObject obj) {
        BMap<?, ?> closureMap = (BMap<?, ?>) obj.getClosureMap();
        return closureMap;
    }
}
