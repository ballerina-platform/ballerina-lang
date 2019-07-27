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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.hub.Hub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function to stop the default Ballerina WebSub Hub, if started.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "stopHubService",
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class StopHubService extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(StopHubService.class);

    @Override
    public void execute(Context context) {
    }

    public static boolean stopHubService(Strand strand, String hubUrl) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted()) {
            try {
                hubInstance.stopHubService();
                return true;
            } catch (BallerinaWebSubException e) {
                return false;
            }
        }
        return false;
    }
}
