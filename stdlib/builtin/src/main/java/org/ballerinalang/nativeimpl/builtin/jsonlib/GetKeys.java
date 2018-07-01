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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.model.json:getKeys. Returns an array of keys contained in the specified JSON.
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

    private static final Logger log = LoggerFactory.getLogger(GetKeys.class);

    @Override
    public void execute(Context ctx) {
        BStringArray keys = null;
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) ctx.getNullableRefArgument(0);
            if (json == null) {
                keys = new BStringArray();
            } else {
                keys = JSONUtils.getKeys(json);
            }
            if (log.isDebugEnabled()) {
                log.debug("keys: " + keys);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("get keys from json", e);
        }

        ctx.setReturnValues(keys);
    }
}
