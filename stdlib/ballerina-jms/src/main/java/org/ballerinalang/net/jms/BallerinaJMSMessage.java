/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.net.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;

/**
 * Wrapper class to hold the {@link Message} flows through Ballerina.
 *
 * @since 0.95.5
 */
public class BallerinaJMSMessage {
    private static final Logger log = LoggerFactory.getLogger(BallerinaJMSMessage.class);

    private Message jmsMessage;

    private String replyDestinationName;

    /**
     * Constructor, must have the respective {@link Message} needs to be wrapped.
     * @param jmsMessage {@link Message} needs to be wrapped.
     */
    public BallerinaJMSMessage(Message jmsMessage) {
        this.jmsMessage = jmsMessage;
    }

    /**
     * @return Get the wrapped {@link Message}.
     */
    public Message getJmsMessage() {
        return jmsMessage;
    }

    /**
     * Get the name of the reply destination.
     *
     * @return name of the reply destination
     */
    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    /**
     * Reply destination is saved here while message flowing through Ballerina.
     * When the message is going to send to an endpoint, this name is taken, destination is created and it will get
     * added to the message headers as ReplyTo
     *
     * @param replyDestinationName name of the reply destination
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }
}
