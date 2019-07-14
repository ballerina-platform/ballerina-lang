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

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Get text content of the JMS Message.
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JMS,
        functionName = "getTextMessageContent",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class GetTextMessageContent extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(GetTextMessageContent.class);

    @Override
    public void execute(Context context) {
    }

    public static Object getTextMessageContent(Strand strand, ObjectValue msgObj) {

        Message jmsMessage = JmsUtils.getJMSMessage(msgObj);

        String messageContent = null;

        try {
            if (jmsMessage instanceof TextMessage) {
                messageContent = ((TextMessage) jmsMessage).getText();
            } else {
                log.error("JMSMessage is not a Text message. ");
            }
        } catch (JMSException e) {
           return BallerinaAdapter.getError("Error when retrieving JMS message content.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Get content from the JMS message");
        }

        return messageContent;
    }

}
