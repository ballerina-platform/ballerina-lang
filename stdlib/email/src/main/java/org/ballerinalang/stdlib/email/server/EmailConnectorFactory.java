/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email.server;

import java.util.Map;

/**
 * Produces EmailConnector objects.
 *
 * @since 1.3.0
 */
public class EmailConnectorFactory {

    private EmailConnectorFactory() {
        // Singleton class
    }

    /**
     * Generates a new Email connector.
     * @param connectorConfig Configuration of Email connector
     * @param emailListener Listener that polls to email server
     * @return Generated new Email connector
     * @throws EmailConnectorException If the given protocol is invalid
     */
    public static EmailConnector createServerConnector(Map<String, Object> connectorConfig,
                                                EmailListener emailListener) throws EmailConnectorException {
        return new EmailConnector(connectorConfig, emailListener);
    }
}
