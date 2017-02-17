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


package org.ballerinalang.nativeimpl.functions.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Test Class for Testing Ballerina Service.
 */
@BallerinaFunction(
        packageName = "ballerina.test.echo",
        functionName = "echoString",
        args = {@Argument(name = "echoString", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class EchoStringNativeFunction extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        // Accessing First Parameter Value.
        String param = getArgument(ctx, 0).stringValue();
        // Return values.
        return getBValues(new BString(param));
    }

}
