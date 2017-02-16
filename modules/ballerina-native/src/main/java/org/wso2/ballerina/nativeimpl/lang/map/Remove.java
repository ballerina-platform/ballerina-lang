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

package org.wso2.ballerina.nativeimpl.lang.map;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Native function to remove element from the map.
 * ballerina.lang.map:remove(map, string)
 */
@BallerinaFunction(
        packageName = "ballerina.lang.map",
        functionName = "remove",
        args = {@Argument(name = "m", type = TypeEnum.MAP),
                @Argument(name = "key", type = TypeEnum.STRING)},
        isPublic = true
)
public class Remove extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BMap map = (BMap) getArgument(ctx, 0);
        BString key = (BString) getArgument(ctx, 1);
        map.remove(key);
        return VOID_RETURN;
    }
}
