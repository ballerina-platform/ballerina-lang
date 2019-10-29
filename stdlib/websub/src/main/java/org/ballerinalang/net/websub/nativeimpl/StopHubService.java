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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.WebSubUtils;
import org.ballerinalang.net.websub.hub.Hub;

/**
 * Extern function to stop the default Ballerina WebSub Hub, if started.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "stopHubService",
        returnType = {@ReturnType(type = TypeKind.ERROR), @ReturnType(type = TypeKind.NIL)},
        isPublic = true
)
public class StopHubService {

    public static Object stopHubService(Strand strand, Object hub) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted()) {
            try {
                if (hubInstance.getHubObject() != hub) {
                    return WebSubUtils.createError("error stopping the hub service: hub object does not match the " +
                                                           "started hub");
                }
                hubInstance.stopHubService();
                return null;
            } catch (BallerinaWebSubException e) {
                return WebSubUtils.createError(e.getMessage());
            }
        }
        return WebSubUtils.createError("error stopping the hub service: hub service not started");
    }
}
