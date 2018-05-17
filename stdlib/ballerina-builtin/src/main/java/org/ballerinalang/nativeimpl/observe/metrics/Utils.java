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
package org.ballerinalang.nativeimpl.observe.metrics;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.metrics.Tag;

import java.util.HashSet;
import java.util.Set;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;

/**
 * This provides the util functions to metrics related functions.
 */
public class Utils {

    private Utils() {
        // Do nothing
    }

    public static Set<Tag> toTags(BMap map) {
        Set<Tag> tags = new HashSet<>(map.size());
        for (Object key : map.keySet()) {
            tags.add(new Tag(key.toString(), map.get(key).stringValue()));
        }
        return tags;
    }

    public static BStruct createMetricsStruct(Context context, String name, Object... values) {
        PackageInfo observePackage = context.getProgramFile().getPackageInfo(Constants.OBSERVE_PACKAGE_PATH);
        StructInfo spanStructInfo = observePackage.getStructInfo(name);
        return BLangVMStructs.createBStruct(spanStructInfo, values);
    }

    public static BStruct createErrorStruct(Context context, String message) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(errorStructInfo, message != null ? message : "unknown error");
    }
}
