/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.internal;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.Utils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.internal:parseJson.
 *
 * @since 0.980.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "parseJson",
        args = {@Argument(name = "s", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.JSON)},
        isPublic = true)
public class ParseJson extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            String value = context.getStringArgument(0);
            JsonNode jsonNode = JsonParser.parse(value);
            context.setReturnValues(new BJSON(jsonNode));
        } catch (Throwable e) {
            BStruct error = Utils.createConversionError(context, "Failed to parse json string: " + e.getMessage());
            context.setReturnValues(error);
        }
    }
}
