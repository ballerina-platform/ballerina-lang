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
package org.ballerinalang.test.service.websub;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;

import java.io.IOException;

/**
 * Utils class for WebSub Tests.
 */
class WebSubTestUtils {

    static void updateSubscribed(String port) throws BallerinaTestException {
        try {
            HttpClientRequest.doGet("http://localhost:" + port + "/helper/subscribed");
        } catch (IOException e) {
            throw new BallerinaTestException("Error updating subscription change");
        }
    }

    static void updateNotified(String port) throws BallerinaTestException {
        try {
            HttpClientRequest.doGet("http://localhost:" + port + "/helper/delivered");
        } catch (IOException e) {
            throw new BallerinaTestException("Error updating content delivery");
        }
    }
}
