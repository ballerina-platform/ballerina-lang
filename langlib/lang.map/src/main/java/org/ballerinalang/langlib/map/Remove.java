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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaException;

import static org.ballerinalang.jvm.MapUtils.checkIsMapOnlyOperation;

/**
 * Extern function to remove element from the map.
 * ballerina.model.map:remove(string)
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.map", functionName = "remove",
        args = {@Argument(name = "m", type = TypeKind.MAP), @Argument(name = "k", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ANY)},
        isPublic = true
)
public class Remove extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        BMap<String, BValue> map = (BMap<String, BValue>) ctx.getRefArgument(0);
        try {
            boolean isRemoved = map.remove(ctx.getStringArgument(0));
            ctx.setReturnValues(new BBoolean(isRemoved));
        } catch (BLangFreezeException e) {
            throw new BallerinaException(e.getMessage(), "Failed to remove element from map: " + e.getDetail());
        }
    }

    public static Object remove(Strand strand, MapValue<?, ?> m, String k) {
        checkIsMapOnlyOperation(m.getType(), "remove()");
        if (m.containsKey(k)) {
            try {
                return m.remove(k);
            } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
                throw BallerinaErrors.createError(e.getMessage(),
                                                  "Failed to remove element from map: " + e.getDetail());
            }
        }

        throw BallerinaErrors.createError(BallerinaErrorReasons.KEY_NOT_FOUND_ERROR, "cannot find key '" + k + "'");
    }
}
