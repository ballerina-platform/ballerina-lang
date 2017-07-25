/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.nativeimpl.lang.jsons;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Remove the element(s) that matches the given key.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "remove",
        args = {@Argument(name = "j", type = TypeEnum.JSON),
                @Argument(name = "key", type = TypeEnum.STRING)},
        isPublic = true
)
public class Remove extends AbstractNativeFunction {

    private static final String OPERATION = "remove element from json";

    @Override
    public BValue[] execute(Context ctx) {
        String fieldName = null;
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getRefArgument(ctx, 0);
            fieldName = getStringArgument(ctx, 0);

            // Removing the element
            JSONUtils.remove(json, fieldName);
        } catch (Throwable e) {
            ErrorHandler.handleJsonException(OPERATION, e);
        }

        return VOID_RETURN;
    }
}
