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

package org.ballerinalang.nativeimpl.connectors.jms.utils;

/**
 * Common Constants used by JMS connector.
 */

public class JMSConstants {

    public static final String CONNECTION_FACTORY_JNDI_PARAM_NAME = "connectionFactoryJNDIName";
    public static final String CONNECTION_FACTORY_TYPE_PARAM_NAME = "connectionFactoryType";
    public static final String DESTINATION_PARAM_NAME = "destination";
    public static final String NAMING_FACTORY_INITIAL_PARAM_NAME = "factoryInitial";
    public static final String PROVIDER_URL_PARAM_NAME = "providerUrl";
    public static final String CACHE_LEVEL = "CacheLevel";

    public static final String CONNECTION_USERNAME = "connectionUsername";
    public static final String CONNECTION_PASSWORD = "connectionPassword";
    public static final String MAP_DATA = "MapData";

    public static final String JMS_MESSAGE_TYPE = "JMS_MESSAGE_TYPE";
    public static final String GENERIC_MESSAGE_TYPE = "Message";
    public static final String TEXT_MESSAGE_TYPE = "TextMessage";
    public static final String BYTES_MESSAGE_TYPE = "BytesMessage";
    public static final String OBJECT_MESSAGE_TYPE = "ObjectMessage";
    public static final String MAP_MESSAGE_TYPE = "MapMessage";

    /**
     * Do not cache any JMS resources between tasks (when sending) or JMS CF's
     * (when sending)
     */
    public static final int CACHE_NONE = 0;
    /**
     * Cache only the JMS connection between tasks (when receiving), or JMS CF's
     * (when sending)
     */
    public static final int CACHE_CONNECTION = 1;
    /**
     * Cache only the JMS connection and Session between tasks (receiving), or
     * JMS CF's (sending)
     */
    public static final int CACHE_SESSION = 2;
    /**
     * Cache the JMS connection, Session and Consumer between tasks when
     * receiving
     */
    public static final int CACHE_CONSUMER = 3;
    /**
     * Cache the JMS connection, Session and Producer within a
     * JMSConnectionFactory when sending
     */
    public static final int CACHE_PRODUCER = 4;

    /**
     * Acknowledgements to client
     */
    public static final String JMS_MESSAGE_DELIVERY_ERROR = "ERROR";
    public static final String JMS_MESSAGE_DELIVERY_SUCCESS = "SUCCESS";
    public static final String JMS_MESSAGE_DELIVERY_STATUS = "JMS_MESSAGE_DELIVERY_STATUS";
}
