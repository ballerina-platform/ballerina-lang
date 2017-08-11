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

package org.ballerinalang.services.dispatchers.jms;

import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants for jms.
 *
 * @since 0.8.0
 */
public class Constants {

    private Constants() {
    }

    /**
     * JMSSource annotation name which is used to define a JMS server connector.
     */
    public static final String ANNOTATION_JMS_CONFIGURATION = "configuration";

    // jms protocol name
    public static final String PROTOCOL_JMS = "jms";
    public static final String PROTOCOL_PACKAGE_JMS = "ballerina.net.jms";
    public static final String JMS_SERVICE_ID = "JMS_SERVICE_ID";
    public static final String JMS_PACKAGE = "ballerina.net.jms";

    public static final String CONFIG_FILE_PATH = "configFilePath";

    public static final String PROPERTIES_ARRAY = "properties";

    /**
     * Parameters from the user.
     */
    public static final String ALIAS_CONNECTION_FACTORY_NAME = "connectionFactoryName";
    /**
     * Type of the connection factory. Whether queue or topic connection factory.
     */
    public static final String ALIAS_CONNECTION_FACTORY_TYPE = "connectionFactoryType";
    /**
     * jms destination.
     */
    public static final String ALIAS_DESTINATION = "destination";
    /**
     * Connection property factoryInitial parameter name.
     */
    public static final String ALIAS_INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    /**
     * Connection property providerUrl parameter name.
     */
    public static final String ALIAS_PROVIDER_URL = "providerUrl";
    public static final String ALIAS_ACK_MODE = "acknowledgementMode";
    public static final String ALIAS_CLIENT_ID = "clientId";
    public static final String ALIAS_DURABLE_SUBSCRIBER_ID = "subscriptionId";

    /**
     * Alias for MB initial context factory name.
     */
    public static final String MB_ICF_ALIAS = "wso2mbInitialContextFactory";
    public static final String MB_ICF_NAME = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    public static final String MB_CF_NAME_PREFIX = "connectionfactory.";

    private static Map<String, String> mappingParameters;

    static {
        mappingParameters = new HashMap<>();
        mappingParameters.put(ALIAS_INITIAL_CONTEXT_FACTORY, JMSConstants.PARAM_NAMING_FACTORY_INITIAL);
        mappingParameters.put(ALIAS_CONNECTION_FACTORY_NAME, JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME);
        mappingParameters.put(ALIAS_CONNECTION_FACTORY_TYPE, JMSConstants.PARAM_CONNECTION_FACTORY_TYPE);
        mappingParameters.put(ALIAS_PROVIDER_URL, JMSConstants.PARAM_PROVIDER_URL);
        mappingParameters.put(ALIAS_DESTINATION, JMSConstants.PARAM_DESTINATION_NAME);
        mappingParameters.put(ALIAS_ACK_MODE, JMSConstants.PARAM_ACK_MODE);
        mappingParameters.put(ALIAS_CLIENT_ID, JMSConstants.PARAM_CLIENT_ID);
        mappingParameters.put(ALIAS_DURABLE_SUBSCRIBER_ID, JMSConstants.PARAM_DURABLE_SUB_ID);
    }

    public static final Map<String, String> MAPPING_PARAMETERS = Collections.unmodifiableMap(mappingParameters);
}
