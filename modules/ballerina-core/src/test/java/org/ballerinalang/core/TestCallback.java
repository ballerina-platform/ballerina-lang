/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.core;

import org.ballerinalang.runtime.BalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Callback for testing service/resource invocations.
 */
public class TestCallback implements BalCallback {

    CarbonMessage response;

    public void done(CarbonMessage carbonMessage) {
        response = carbonMessage;
    }

    public CarbonMessage getResponse() {
        return response;
    }
}
