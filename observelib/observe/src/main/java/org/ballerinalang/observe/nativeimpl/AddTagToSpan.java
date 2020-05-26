/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * This function adds tags to a span.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe", version = "0.8.0",
        functionName = "addTagToSpan",
        args = {
                @Argument(name = "tagKey", type = TypeKind.STRING),
                @Argument(name = "tagValue", type = TypeKind.STRING),
                @Argument(name = "spanId", type = TypeKind.INT)
        },
        returnType = @ReturnType(type = TypeKind.BOOLEAN),
        isPublic = true
)
public class AddTagToSpan {

    public static Object addTagToSpan(Strand strand, BString tagKey, BString tagValue, long spanId) {
        boolean tagAdded = OpenTracerBallerinaWrapper.getInstance().addTag(tagKey.getValue(), tagValue.getValue(),
                                                                           spanId, strand);

        if (tagAdded) {
            return null;
        }

        return BallerinaErrors.createError("Span already finished. Can not add tag {" + tagKey + ":" + tagValue + "}");
    }
}
