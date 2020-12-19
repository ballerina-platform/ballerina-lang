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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BString;

/**
 * This function adds tags to a span.
 */
public class AddTagToSpan {
    private static final OpenTracerBallerinaWrapper otWrapperInstance = OpenTracerBallerinaWrapper.getInstance();

    public static Object addTagToSpan(Environment env, BString tagKey, BString tagValue, long spanId) {
        return otWrapperInstance.addTag(env, tagKey.getValue(), tagValue.getValue(), spanId);
    }
}
