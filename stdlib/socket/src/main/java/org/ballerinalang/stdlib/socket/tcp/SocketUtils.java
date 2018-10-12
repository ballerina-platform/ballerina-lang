/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Represents the util functions of Socket operations.
 */
public class SocketUtils {

    /**
     * Returns the error struct for the corresponding message.
     *
     * @param context context of the extern function.
     * @param message error message.
     * @return error message struct.
     */
    public static BMap<String, BValue> createError(Context context, String message) {
        PackageInfo builtInPkg = context.getProgramFile().getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo error = builtInPkg.getStructInfo(BLangVMErrors.STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(error, message);
    }
}
