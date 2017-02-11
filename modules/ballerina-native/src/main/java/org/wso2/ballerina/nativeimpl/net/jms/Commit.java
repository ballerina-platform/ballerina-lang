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

package org.wso2.ballerina.nativeimpl.net.jms;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.nativeimpl.connectors.jms.utils.JMSConstants;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * To commit the jms transacted sessions.
 */
@BallerinaFunction(packageName = "ballerina.net.jms", functionName = "commit", args = {
        @Argument(name = "message", type = TypeEnum.MESSAGE) }, isPublic = true)
public class Commit extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        CarbonMessage carbonMessage = ctx.getCarbonMessage();
        if (ctx.getBalCallback() != null) {
            carbonMessage
                    .setProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS, JMSConstants.JMS_MESSAGE_DELIVERY_SUCCESS);
            ctx.getBalCallback().done(carbonMessage);
        }
        return VOID_RETURN;
    }
}
