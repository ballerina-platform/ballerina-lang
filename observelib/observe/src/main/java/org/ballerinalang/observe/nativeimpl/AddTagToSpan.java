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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;

/**
 * This function adds tags to a span.
 */
public class AddTagToSpan {
    private static final OpenTracerBallerinaWrapper otWrapperInstance = OpenTracerBallerinaWrapper.getInstance();

    public static Object addTagToSpan(BString tagKey, BString tagValue, long spanId) {
        boolean tagAdded = otWrapperInstance.addTag(tagKey.getValue(), tagValue.getValue(),
                                                    spanId, Scheduler.getStrand());

        if (tagAdded) {
            return null;
        }

        return ErrorCreator.createError(
                StringUtils.fromString(("Span already finished. Can not add tag {" + tagKey + ":" + tagValue + "}")));
    }
}
