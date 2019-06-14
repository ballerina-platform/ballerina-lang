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

package org.ballerinalang.nativeimpl.builtin.jsonlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;

/**
 * Extern function ballerina.model.json:getKeys. Returns an array of keys contained in the specified JSON.
 * If the JSON is not an object type element, then this method will return an empty array.
 * 
 * @since 0.90
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "json.getKeys",
        args = {@Argument(name = "j", type = TypeKind.JSON)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetKeys extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValueArray keys = null;
        try {
            // Accessing Parameters.
            BValue json = ctx.getNullableRefArgument(0);
            if (json == null) {
                keys = new BValueArray(BTypes.typeString);
            } else {
                keys = JSONUtils.getKeys(json);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException(BallerinaErrorReasons.JSON_OPERATION_ERROR, "get keys from json", e);
        }

        ctx.setReturnValues(keys);
    }

    public static ArrayValue getKeys(Object json) {
        try {
            if (json == null) {
                return new ArrayValue(new BArrayType(org.ballerinalang.jvm.types.BTypes.typeString));
            }

            return org.ballerinalang.jvm.JSONUtils.getKeys(json);
        } catch (Throwable e) {
            BLangExceptionHelper.handleJsonException(BallerinaErrorReasons.JSON_OPERATION_ERROR, "get keys from json",
                    e);
        }
        return null;
    }
}
