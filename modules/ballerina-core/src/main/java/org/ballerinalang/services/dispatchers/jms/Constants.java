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

/**
 * Constants for jms.
 *
 * @since 0.8.0
 */
public class Constants {

    /**
     * JMSSource annotation name which is used to define a JMS server connector.
     */
    public static final String ANNOTATION_JMS_SOURCE = "JMSSource";

    /**
     * Connection property annotation name.
     */
    public static final String ANNOTATION_CONNECTION_PROPERTY = "ConnectionProperty";

    /**
     * Connection property key parameter name.
     */
    public static final String CONNECTION_PROPERTY_KEY = "key";

    /**
     * Connection property value parameter name.
     */
    public static final String CONNECTION_PROPERTY_VALUE = "value";

    // jms destination
    public static final String JMS_DESTINATION = "destination";
    // jms protocol name
    public static final String PROTOCOL_JMS = "jms";
    public static final String JMS_SERVICE_ID = "JMS_SERVICE_ID";
    public static final String JMS_PACKAGE = "ballerina.net.jms";

    /**
     * Connection property factoryInitial parameter name.
     */
    public static final String CONNECTION_PROPERTY_FACTORY_INITIAL = "factoryInitial";

    /**
     * Connection property providerUrl parameter name.
     */
    public static final String CONNECTION_PROPERTY_PROVIDE_URL = "providerUrl";
}
