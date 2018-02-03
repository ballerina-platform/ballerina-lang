/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.config;

import org.ballerinalang.bre.Context;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.config:getInstanceValue.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.config",
        functionName = "getInstanceValue",
        args = {@Argument(name = "instance", type = TypeKind.STRING),
                @Argument(name = "property", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetInstanceValue extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String instanceId = this.getStringArgument(context, 0);
        String configKey = this.getStringArgument(context, 1);

        String instanceValue = ConfigRegistry.getInstance().getInstanceConfigValue(instanceId, configKey);
        return getBValues(new BString(instanceValue));
    }
}
