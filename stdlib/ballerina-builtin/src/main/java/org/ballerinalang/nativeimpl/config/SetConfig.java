/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function ballerina.config:setConfig.
 *
 * @since 0.966.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "config",
        functionName = "setConfig",
        args = {@Argument(name = "key", type = TypeKind.STRING),
                @Argument(name = "value", type = TypeKind.UNION)},
        isPublic = true
)
public class SetConfig extends BlockingNativeCallableUnit {

    public static final ConfigRegistry CONFIG_REGISTRY = ConfigRegistry.getInstance();

    @Override
    public void execute(Context context) {
        String configKey = context.getStringArgument(0);
        BValue configValue = context.getRefArgument(0);

        BType type = configValue.getType();
        if (type == BTypes.typeString) {
            CONFIG_REGISTRY.addConfiguration(configKey, configValue.stringValue());
        } else if (type == BTypes.typeInt) {
            CONFIG_REGISTRY.addConfiguration(configKey, ((BInteger) configValue).intValue());
        } else if (type == BTypes.typeFloat) {
            CONFIG_REGISTRY.addConfiguration(configKey, ((BFloat) configValue).floatValue());
        } else if (type == BTypes.typeBoolean) {
            CONFIG_REGISTRY.addConfiguration(configKey, ((BBoolean) configValue).floatValue());
        }

        context.setReturnValues();
    }
}
