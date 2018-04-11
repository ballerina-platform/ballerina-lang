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

package org.ballerinalang.queue.nativeImpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Set byte content of the JMS Byte Message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        functionName = "setBytesMessageContent",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Message",
                             structPackage = "ballerina.queue"),
        args = {@Argument(name = "content", type = TypeKind.BLOB)},
        isPublic = true
)
public class SetBytesMessageContent extends AbstractBlockinAction {

    private static final Logger log = LoggerFactory.getLogger(SetBytesMessageContent.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        BStruct messageStruct  = ((BStruct) context.getRefArgument(0));
        byte[] messageContent = context.getBlobArgument(0);

        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);

        try {
            if (jmsMessage instanceof BytesMessage) {
                ((BytesMessage) jmsMessage).writeBytes(messageContent);
            } else {
                log.error("JMSMessage is not a Bytes message. ");
            }
        } catch (JMSException e) {
            log.error("Error when setting JMS message content :" + e.getLocalizedMessage(), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Set content for the JMS message");
        }
    }
}
