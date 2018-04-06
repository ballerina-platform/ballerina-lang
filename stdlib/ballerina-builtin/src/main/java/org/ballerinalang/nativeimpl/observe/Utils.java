/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.nativeimpl.observe;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This provides the util functions to tracing related functions.
 */
public class Utils {

    private Utils() {
        //Do nothing
    }

    static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            Set bIterator = map.keySet();
            for (Object aKey : bIterator) {
                if (map.get(aKey) instanceof BStringArray) {
                    returnMap.put(aKey.toString(), (((BStringArray) map.get(aKey)).get(0)));
                } else {
                    returnMap.put(aKey.toString(), map.get(aKey).stringValue());
                }
            }
        }
        return returnMap;
    }

    public static BStruct createSpanStruct(Context context, String serviceName, String spanName, String spanId) {
        PackageInfo observePackage = context.getProgramFile().getPackageInfo("ballerina.observe");
        StructInfo spanStructInfo = observePackage.getStructInfo("Span");
        return BLangVMStructs.createBStruct(spanStructInfo, serviceName, spanName, spanId);
    }

    public static BStruct createSpanContextStruct(Context context, BMap spanContext) {
        PackageInfo observePackage = context.getProgramFile().getPackageInfo("ballerina.observe");
        StructInfo spanStructInfo = observePackage.getStructInfo("SpanContext");
        return BLangVMStructs.createBStruct(spanStructInfo, spanContext);
    }
}
