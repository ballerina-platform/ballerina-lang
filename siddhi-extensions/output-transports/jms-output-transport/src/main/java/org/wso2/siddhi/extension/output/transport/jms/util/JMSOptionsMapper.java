/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.siddhi.extension.output.transport.jms.util;

import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to do the custom mapping from the siddhi extension to JMS Carbon Transport.
 */
public class JMSOptionsMapper {
    public static final String DESTINATION = "destination";
    public static final String CONNECTION_FACTORY_JNDI_NAME = "connection.factory.jndi.name";
    public static final String FACTORY_INITIAL = "factory.initial";
    public static final String PROVIDER_URL = "provider.url";
    public static final String CONNECTION_FACTORY_TYPE = "connection.factory.type";

    /**
     * Returns the custom property map mapping the siddhi extension key name to JMS transport key.
     *
     * @return map of custom mapping
     */
    public static Map<String, String> getCustomPropertyMapping() {
        Map<String, String> customPropertyMapping = new HashMap<>();
        customPropertyMapping.put(DESTINATION, JMSConstants.DESTINATION_PARAM_NAME);
        customPropertyMapping.put(CONNECTION_FACTORY_JNDI_NAME, JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME);
        customPropertyMapping.put(FACTORY_INITIAL, JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME);
        customPropertyMapping.put(PROVIDER_URL, JMSConstants.PROVIDER_URL_PARAM_NAME);
        customPropertyMapping.put(CONNECTION_FACTORY_TYPE, JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME);
        return customPropertyMapping;
    }

    /**
     * Returns the required options for the JMS input transport.
     *
     * @return list of required options.
     */
    public static List<String> getRequiredOptions() {
        return Arrays.asList(DESTINATION, CONNECTION_FACTORY_JNDI_NAME, FACTORY_INITIAL, FACTORY_INITIAL, PROVIDER_URL,
                CONNECTION_FACTORY_TYPE);
    }
}
