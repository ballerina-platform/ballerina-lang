/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.hub.Hub;

import static org.ballerinalang.net.websub.WebSubUtils.createError;

/**
 * Extern function to validate that the hub URL passed indicates the underlying Ballerina Hub (if started) and publish
 * against a topic in the default Ballerina Hub's underlying broker.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "validateAndPublishToInternalHub",
        args = {@Argument(name = "hubUrl", type = TypeKind.STRING),
                @Argument(name = "topic", type = TypeKind.STRING),
                @Argument(name = "content", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.OBJECT)}
)
public class ValidateAndPublishToInternalHub {

    public static Object validateAndPublishToInternalHub(Strand strand, String hubUrl, String topic,
                                                         MapValue<String, Object> content) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted() && hubInstance.getPublishUrl().equals(hubUrl)) {
            try {
                Hub.getInstance().publish(topic, content);
            } catch (BallerinaWebSubException e) {
                return createError(e.getMessage());
            }
            return null;
        }
        return createError("Internal Ballerina Hub not initialized or incorrectly referenced");
    }
}
