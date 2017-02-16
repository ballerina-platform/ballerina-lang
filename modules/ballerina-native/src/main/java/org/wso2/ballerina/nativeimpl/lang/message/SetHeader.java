/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.nativeimpl.lang.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Native function to set given header to carbon message.
 * ballerina.lang.message:setHeader
 */

@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "setHeader",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "key", type = TypeEnum.STRING),
                @Argument(name = "value", type = TypeEnum.STRING)},
        isPublic = true
)
public class SetHeader extends AbstractNativeFunction {

    private static final Logger LOG = LoggerFactory.getLogger(SetHeader.class);

    @Override
    public BValue[] execute(Context context) {
        BMessage msg = (BMessage) getArgument(context, 0);
        String headerName = getArgument(context, 1).stringValue();
        String headerValue = getArgument(context, 2).stringValue();
        // Set new header.
        msg.setHeader(headerName, headerValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set " + headerName + " header with value: " + headerValue);
        }
        return VOID_RETURN;
    }
}
