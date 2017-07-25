/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Rename the key of the given element that is under the given key.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "rename",
        args = {@Argument(name = "j", type = TypeEnum.JSON),
                @Argument(name = "oldKey", type = TypeEnum.STRING),
                @Argument(name = "newKey", type = TypeEnum.STRING)},
        isPublic = true
)
public class Rename extends AbstractNativeFunction {
    
    private static final String OPERATION = "rename element in json";

    @Override
    public BValue[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getRefArgument(ctx, 0);
            String oldKey = getStringArgument(ctx, 0);
            String newKey = getStringArgument(ctx, 1);
            
            // Rename the element key
            JSONUtils.rename(json, oldKey, newKey);
        } catch (Throwable e) {
            ErrorHandler.handleJsonException(OPERATION, e);
        }

        return VOID_RETURN;
    }
}
