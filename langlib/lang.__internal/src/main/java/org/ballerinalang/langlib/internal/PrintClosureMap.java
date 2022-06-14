package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;

import java.util.Map;

public class PrintClosureMap {

    public static void printClosureMap(BObject obj) {
        BMap<?, ?> closureMap = (BMap<?, ?>) obj.getClosureMap();

        for (Map.Entry entry : closureMap.entrySet()) {
            Type type = TypeChecker.getType(entry.getValue());
            BString keyName = StringUtils.fromString(entry.getKey().toString());
            System.out.println(keyName + " : " +  type + " : " + entry.getValue());
        }
    }
}
