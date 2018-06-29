/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * This provides the util functions to observe related functions.
 */
public class Utils {

    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            Set bIterator = map.keySet();
            for (Object aKey : bIterator) {
                returnMap.put(aKey.toString(), map.get(aKey).stringValue());
            }
        }
        return returnMap;
    }

    public static BMap<String, BValue> createErrorStruct(Context context, String message) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(errorStructInfo, message);
    }
}
