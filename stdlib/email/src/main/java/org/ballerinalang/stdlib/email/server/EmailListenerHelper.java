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
import org.ballerinalang.jvm.values.api.BString;
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
     * Initialize a new EmailConnector for the listener.
     * @param emailListener Listener that places emails in Ballerina runtime
     * @param serviceEndpointConfig Email server endpoint configuration
     * @throws EmailConnectorException If the given protocol is invalid
     */
    public static void init(ObjectValue emailListener, MapValue<BString, Object> serviceEndpointConfig)
            throws EmailConnectorException {
        final EmailListener listener = new EmailListener(BRuntime.getCurrentRuntime());
        Map<String, Object> paramMap = getServerConnectorParamMap(serviceEndpointConfig);
        EmailConnector emailConnector = EmailConnectorFactory.createServerConnector(paramMap, listener);
        emailListener.addNativeData(EmailConstants.EMAIL_SERVER_CONNECTOR, emailConnector);
    }

    /**
     * Register a new service for the listener.
     * @param emailListener Ballerina email listener
     * @param service Ballerina service to be listened
     */
    public static void register(ObjectValue emailListener, ObjectValue service) {
        EmailConnector emailConnector = (EmailConnector) emailListener.getNativeData(
                EmailConstants.EMAIL_SERVER_CONNECTOR);
        EmailListener listener = emailConnector.getEmailListener();
        listener.addService(service);
    }

    private static Map<String, Object> getServerConnectorParamMap(MapValue<BString, Object> serviceEndpointConfig) {
        Map<String, Object> params = new HashMap<>(7);
        MapValue<BString, BString> secureSocket = (MapValue<BString, BString>) serviceEndpointConfig.getMapValue(
                EmailConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            final MapValue<BString, BString> privateKey = (MapValue<BString, BString>) secureSocket.getMapValue(
                    EmailConstants.ENDPOINT_CONFIG_PRIVATE_KEY);
            if (privateKey != null) {
                final String privateKeyPath = privateKey.getStringValue(EmailConstants.ENDPOINT_CONFIG_PATH).getValue();
                final String privateKeyPassword =
                        privateKey.getStringValue(EmailConstants.ENDPOINT_CONFIG_PASS_KEY).getValue();
                if (privateKeyPath != null && !privateKeyPath.isEmpty() && privateKeyPassword != null
                        && !privateKeyPassword.isEmpty()) {
                    params.put(EmailConstants.IDENTITY, privateKeyPath);
                    params.put(EmailConstants.IDENTITY_PASS_PHRASE, privateKeyPassword);
                }
            }
        }
        MapValue<BString, Object> protocolConfig = (MapValue<BString, Object>) serviceEndpointConfig.getMapValue(
                EmailConstants.PROTOCOL_CONFIG);
        if (protocolConfig != null) {
            params.put(EmailConstants.PROTOCOL_CONFIG.getValue(), protocolConfig);
        }
        params.put(EmailConstants.PROPS_HOST.getValue(),
                   serviceEndpointConfig.getStringValue(EmailConstants.PROPS_HOST).getValue());
        params.put(EmailConstants.PROPS_USERNAME.getValue(),
                   serviceEndpointConfig.getStringValue(EmailConstants.PROPS_USERNAME).getValue());
        params.put(EmailConstants.PROPS_PASSWORD.getValue(),
                   serviceEndpointConfig.getStringValue(EmailConstants.PROPS_PASSWORD).getValue());
        params.put(EmailConstants.PROPS_PROTOCOL.getValue(),
                   serviceEndpointConfig.getStringValue(EmailConstants.PROPS_PROTOCOL).getValue());
        return params;
    }

    /**
     * Polls emails from the email server endpoint.
     * @param emailListener Ballerina listener for connecting to the email server endpoint
     * @throws Exception If an error occurs during the polling operations
     */
    public static void poll(ObjectValue emailListener) throws Exception {
        EmailConnector connector = (EmailConnector) emailListener.getNativeData(EmailConstants.EMAIL_SERVER_CONNECTOR);
        try {
            connector.poll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());

        }
    }
}
