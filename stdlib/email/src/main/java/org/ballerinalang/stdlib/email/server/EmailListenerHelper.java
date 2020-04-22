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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.email.util.EmailConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for listener functions.
 *
 * @since 1.3.0
 */
public class EmailListenerHelper {

    private EmailListenerHelper() {
        // private constructor
    }

    /**
     * Register a new listener for an email server endpoint.
     * @param emailListener Listener that places emails in Ballerina runtime
     * @param serviceEndpointConfig Email server endpoint configuration
     * @param service Ballerina service to be listened
     * @return Registered new Email connector with listening capability
     */
    public static EmailConnector register(ObjectValue emailListener, MapValue<Object, Object> serviceEndpointConfig,
                                          ObjectValue service) {
        EmailConnectorFactory emailConnectorFactory = new EmailConnectorFactory();
        final EmailListener listener = new EmailListener(BRuntime.getCurrentRuntime(), service);
        Map<String, Object> paramMap = getServerConnectorParamMap(serviceEndpointConfig);
        EmailConnector emailConnector = emailConnectorFactory.createServerConnector(paramMap, listener);
        emailListener.addNativeData(EmailConstants.EMAIL_SERVER_CONNECTOR, emailConnector);
        serviceEndpointConfig.addNativeData(EmailConstants.EMAIL_SERVER_CONNECTOR, emailConnector);
        return emailConnector;
    }

    private static Map<String, Object> getServerConnectorParamMap(MapValue serviceEndpointConfig) {
        Map<String, Object> params = new HashMap<>(7);
        MapValue secureSocket = serviceEndpointConfig.getMapValue(EmailConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            final MapValue privateKey = secureSocket.getMapValue(EmailConstants.ENDPOINT_CONFIG_PRIVATE_KEY);
            if (privateKey != null) {
                final String privateKeyPath = privateKey.getStringValue(EmailConstants.ENDPOINT_CONFIG_PATH);
                if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
                    params.put(EmailConstants.IDENTITY, privateKeyPath);
                    final String privateKeyPassword =
                            privateKey.getStringValue(EmailConstants.ENDPOINT_CONFIG_PASS_KEY);
                    if (privateKeyPassword != null && !privateKeyPassword.isEmpty()) {
                        params.put(EmailConstants.IDENTITY_PASS_PHRASE, privateKeyPassword);
                    }
                }
            }
        }
        MapValue protocolConfig = serviceEndpointConfig.getMapValue(EmailConstants.PROTOCOL_CONFIG);
        if (protocolConfig != null) {
            params.put(EmailConstants.PROTOCOL_CONFIG, protocolConfig);
        }
        params.put(EmailConstants.PROPS_HOST, serviceEndpointConfig.getStringValue(EmailConstants.PROPS_HOST));
        params.put(EmailConstants.PROPS_USERNAME, serviceEndpointConfig.getStringValue(EmailConstants.PROPS_USERNAME));
        params.put(EmailConstants.PROPS_PASSWORD, serviceEndpointConfig.getStringValue(EmailConstants.PROPS_PASSWORD));
        params.put(EmailConstants.PROPS_PROTOCOL, serviceEndpointConfig.getStringValue(EmailConstants.PROPS_PROTOCOL));
        return params;
    }

    /**
     * Polls emails from the email server endpoint.
     * @param config Configuration for connecting to the email server endpoint
     */
    public static void poll(MapValue<Object, Object> config) throws Exception {
        EmailConnector connector = (EmailConnector) config.getNativeData(EmailConstants.EMAIL_SERVER_CONNECTOR);
        try {
            connector.poll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());

        }
    }
}
