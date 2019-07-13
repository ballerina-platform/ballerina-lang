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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Get a float property in the JMS Message.
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX,
        packageName = JmsConstants.JMS,
        functionName = "getReplyTo",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class GetReplyTo extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object getReplyTo(Strand strand, ObjectValue msgObj) {
        Message message = JmsUtils.getJMSMessage(msgObj);

        ObjectValue destObj = BallerinaValues.createObjectValue(JmsConstants.PROTOCOL_PACKAGE_JMS,
                                                                JmsConstants.JMS_DESTINATION_OBJ_NAME);
        try {
            Destination destination = message.getJMSReplyTo();
            if (destination instanceof Queue) {
                Queue replyTo = (Queue) destination;
                destObj.addNativeData(JmsConstants.JMS_DESTINATION_OBJECT, replyTo);
                destObj.set(JmsConstants.DESTINATION_NAME, replyTo.getQueueName());
                destObj.set(JmsConstants.DESTINATION_TYPE, "queue");
                return destObj;
            } else if (destination instanceof Topic) {
                Topic replyTo = (Topic) destination;
                destObj.addNativeData(JmsConstants.JMS_DESTINATION_OBJECT, replyTo);
                destObj.set(JmsConstants.DESTINATION_NAME, replyTo.getTopicName());
                destObj.set(JmsConstants.DESTINATION_TYPE, "topic");
                return destObj;
            } else {
                return BallerinaAdapter.getError("ReplyTo header has not been set");
            }
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Error when retrieving replyTo", e);
        }
    }

}
