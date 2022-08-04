/*
 * Copyright (c) (2022), WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;

import java.util.HashMap;
import java.util.Map;

public class PrintClosureMap {

    public static void printClosureMaps(BObject obj) {
        System.out.println("______ INTERNAL BALLERINA DEBUG START -------");
        System.out.println("______ Printing closure maps");
        System.out.println("______ Printing block closure map");
        printBlockClosureMap(obj);
        System.out.println("______ Printing param closure maps");
        printParamClosureMaps(obj);
        System.out.println("______ INTERNAL BALLERINA DEBUG END -------\n");
    }

    private static void printBlockClosureMap(BObject obj) {
        BMap<?, ?> closureMap = (BMap<?, ?>) obj.getBlockClosureMap();
        if (closureMap != null) {
            printBMap(closureMap);
        }
    }

    private static void printParamClosureMaps(BObject obj) {
        HashMap<?, ?> closureMaps = obj.getParamClosureMaps();
        if (closureMaps == null) {
            return;
        }
        for (Map.Entry entry : closureMaps.entrySet()) {
            BMap<?, ?> closureMap = (BMap<?, ?>) entry.getValue();
            BString keyName = StringUtils.fromString(entry.getKey().toString());
            System.out.println("Print map at level : " + keyName);
            printBMap(closureMap);
        }
    }

    private static void printBMap(BMap<?, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            Type type = TypeChecker.getType(entry.getValue());
            BString keyName = StringUtils.fromString(entry.getKey().toString());
            System.out.println(keyName + " : " +  type + " : " + entry.getValue());
        }
    }
}
