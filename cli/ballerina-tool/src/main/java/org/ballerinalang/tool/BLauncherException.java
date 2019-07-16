/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown to indicate that an exception occurred in Ballerina program launcher.
 *
 * @since 0.8.0
 */
public class BLauncherException extends RuntimeException {
    private List<String> detailedMessages = new ArrayList<>();

    public List<String> getDetailedMessages() {
        return detailedMessages;
    }

    void addMessage(String message) {
        detailedMessages.add(message);
    }

    public List<String> getMessages() {
        return detailedMessages;
    }
}
