/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.map;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.MapUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.MapUtils.checkIsMapOnlyOperation;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.ballerinalang.util.BLangCompilerConstants.MAP_VERSION;
import static org.wso2.ballerinalang.compiler.util.Constants.REMOVE;

/**
 * Extern function to remove element from the map.
 * ballerina.model.map:remove(string)
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.map", version = MAP_VERSION, functionName = "remove",
        args = {@Argument(name = "m", type = TypeKind.MAP), @Argument(name = "k", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ANY)},
        isPublic = true
)
public class Remove {

    public static Object remove(Strand strand, MapValue<?, ?> m, BString k) {
        BType type = m.getType();

        checkIsMapOnlyOperation(type, REMOVE);
        MapUtils.validateRequiredFieldForRecord(m, k.getValue());
        if (m.containsKey(k)) {
            try {
                return m.remove(k);
            } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
                throw BallerinaErrors.createError(e.getMessage(),
                                                  "Failed to remove element from map: " + e.getDetail());
            }
        }

        throw BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR, "cannot find key '" + k + "'");
    }
}
