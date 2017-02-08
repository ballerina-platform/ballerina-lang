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

package org.wso2.ballerina.core.nativeimpl.connectors.jms.utils;

/**
 * Common Constants used by JMS connector.
 */

public class JMSConstants {

    public static final String CONNECTION_FACTORY_JNDI_PARAM_NAME = "ConnectionFactoryJNDIName";
    public static final String CONNECTION_FACTORY_TYPE_PARAM_NAME = "ConnectionFactoryType";
    public static final String DESTINATION_PARAM_NAME = "Destination";
    public static final String NAMING_FACTORY_INITIAL_PARAM_NAME = "FactoryInitial";
    public static final String PROVIDER_URL_PARAM_NAME = "ProviderUrl";

    public static final String CONNECTION_USERNAME = "ConnectionUsername";
    public static final String CONNECTION_PASSWORD = "ConnectionPassword";

    public static final String JMS_MESSAGE_TYPE = "JMS_MESSAGE_TYPE";
    public static final String GENERIC_MESSAGE_TYPE = "Message";
    public static final String TEXT_MESSAGE_TYPE = "TextMessage";
    public static final String BYTES_MESSAGE_TYPE = "BytesMessage";
    public static final String OBJECT_MESSAGE_TYPE = "ObjectMessage";
}
