/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.nativeimpl.actions.jms.utils;

/**
 * JMS Constants required for ballerina JMS actions.
 */
public class Constants {

    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final String PROTOCOL_JMS = "jms";

    /*
     * Session acknowledgement mode of the particular message.
     */
    public static final String JMS_SESSION_ACKNOWLEDGEMENT_MODE = "JMS_SESSION_ACKNOWLEDGEMENT_MODE";

    /**
     * A MessageContext property or client Option indicating the JMS message type.
     */
    public static final String JMS_MESSAGE_TYPE = "JMS_MESSAGE_TYPE";

    /**
     * Generic message type to use in case it it not specified.
     */
    public static final String GENERIC_MESSAGE_TYPE = "Message";

    // Message types
    public static final String TEXT_MESSAGE_TYPE = "TextMessage";
    public static final String BYTES_MESSAGE_TYPE = "BytesMessage";
    public static final String OBJECT_MESSAGE_TYPE = "ObjectMessage";
    public static final String MAP_MESSAGE_TYPE = "MapMessage";

    // Delivery statuses
    public static final String JMS_MESSAGE_DELIVERY_ERROR = "ERROR";
    public static final String JMS_MESSAGE_DELIVERY_SUCCESS = "SUCCESS";

    /**
     * This property is set to specify the status of the message.
     */
    public static final String JMS_MESSAGE_DELIVERY_STATUS = "JMS_MESSAGE_DELIVERY_STATUS";

}
