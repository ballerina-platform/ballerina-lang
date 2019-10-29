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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.hub.Hub;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_BALLERINA_HUB_STARTED_UP_ERROR;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_ID;

/**
 * Extern function to start up the default Ballerina WebSub Hub.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "startUpHubService",
        args = {@Argument(name = "topicRegistrationRequired", type = TypeKind.BOOLEAN),
                @Argument(name = "publicUrl", type = TypeKind.STRING),
                @Argument(name = "hubListener", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.OBJECT), @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class StartUpHubService {

    public static Object startUpHubService(Strand strand, String basePath, String subscriptionResourcePath,
                                           String publishResourcePath, boolean topicRegistrationRequired,
                                           String publicUrl, ObjectValue hubListener) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted()) {
            return getHubStartedUpError(hubInstance);
        }
        return hubInstance.startUpHubService(strand, basePath, subscriptionResourcePath, publishResourcePath,
                                             topicRegistrationRequired, publicUrl, hubListener);
    }

    private static MapValue<String, Object> getHubStartedUpError(Hub hubInstance) {
        MapValue<String, Object> hubStartedUpError = BallerinaValues.createRecordValue(WEBSUB_PACKAGE_ID,
                STRUCT_WEBSUB_BALLERINA_HUB_STARTED_UP_ERROR);
        return BallerinaValues.createRecord(hubStartedUpError, "Ballerina Hub already started up", null,
                                            hubInstance.getHubObject());
    }
}
