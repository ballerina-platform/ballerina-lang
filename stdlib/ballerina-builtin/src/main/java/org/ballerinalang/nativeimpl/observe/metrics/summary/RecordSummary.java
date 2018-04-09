/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.observe.metrics.summary;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates the statistics kept by the summary with the specified amount.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "observe",
        functionName = "record",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Summary",
                structPackage = "ballerina.observe"),
        args = {@Argument(name = "summary", type = TypeKind.STRUCT, structType = "Summary",
                structPackage = "ballerina.observe"), @Argument(name = "amount", type = TypeKind.FLOAT)},
        isPublic = true
)
public class RecordSummary extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct summaryStruct = (BStruct) context.getRefArgument(0);
        String name = summaryStruct.getStringField(0);
        String description = summaryStruct.getStringField(1);
        float amount = (float) context.getFloatArgument(0);
        BMap tagsMap = (BMap) summaryStruct.getRefField(0);

        if (!tagsMap.isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for (Object key : tagsMap.keySet()) {
                tags.add(new Tag(key.toString(), tagsMap.get(key).stringValue()));
            }
            Summary.builder(name).description(description).tags(tags).register().record(amount);
        } else {
            Summary.builder(name).description(description).register().record(amount);
        }
    }
}
