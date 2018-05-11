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

import io.ballerina.messaging.broker.core.ContentChunk;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.persistency.ConnectionException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.queue.QueueConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ballerina.messaging.broker.core.Message;


//import javax.jms.JMSException;
//import javax.jms.TextMessage;

/**
 * Get text content of the JMS Message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        functionName = "getTextMessageContent",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Message",
                             structPackage = "ballerina.queue"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetTextMessageContent extends AbstractBlockinAction {

    private static final Logger log = LoggerFactory.getLogger(
            org.ballerinalang.net.jms.nativeimpl.message.GetTextMessageContent.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        BStruct messageStruct  = ((BStruct) context.getRefArgument(0));
        Object messageObject = messageStruct.getNativeData(QueueConstants.QUEUE_MESSAGE_CONTENT);
        if (messageObject == null || !(messageObject instanceof byte[])) {
            throw new ConnectionException("Message is not available.");
        }

        byte[] messageBytes = (byte[]) messageObject;
        String messageString = new String(messageBytes);

//        String messageString = null;
//        Message message = (Message) messageObject;
//        if (message.getContentChunks() != null && !message.getContentChunks().isEmpty()) {
//            ContentChunk chunk = message.getContentChunks().get(0);
//            messageString = new String(chunk.getBytes());
//        }
        context.setReturnValues(new BString(messageString));
    }
}
