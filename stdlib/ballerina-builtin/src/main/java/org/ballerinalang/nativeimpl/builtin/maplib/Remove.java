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

package org.ballerinalang.nativeimpl.builtin.maplib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function to remove element from the map.
 * ballerina.model.map:remove(string)
 */
@BallerinaFunction(
        packageName = "ballerina.builtin",
        functionName = "map.remove",
        args = {@Argument(name = "m", type = TypeKind.MAP),
                @Argument(name = "key", type = TypeKind.STRING)},
        isPublic = true
)
public class Remove extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BMap map = (BMap) getRefArgument(ctx, 0);
        map.remove(getStringArgument(ctx, 0));
        return VOID_RETURN;
    }
}
