/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.ballerinalang.nativeimpl.observe.metrics.gauge;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Set the gauge to the given value.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "observe",
        functionName = "setValue",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Gauge",
                structPackage = "ballerina.observe"),
        args = {@Argument(name = "gauge", type = TypeKind.STRUCT, structType = "Gauge",
                structPackage = "ballerina.observe"), @Argument(name = "value", type = TypeKind.FLOAT)},
        isPublic = true
)
public class SetGauge extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct gaugeStruct = (BStruct) context.getRefArgument(0);
        String name = gaugeStruct.getStringField(0);
        String description = gaugeStruct.getStringField(1);
        BMap tagsMap = (BMap) gaugeStruct.getRefField(0);
        float value = (float) context.getFloatArgument(0);

        if (tagsMap != null) {
            List<Tag> tags = new ArrayList<>();
            for (Object key : tagsMap.keySet()) {
                tags.add(new Tag(key.toString(), tagsMap.get(key).stringValue()));
            }
            Gauge.builder(name).description(description).tags(tags).register().set(value);
        } else {
            Gauge.builder(name).description(description).register().set(value);
        }
    }
}
