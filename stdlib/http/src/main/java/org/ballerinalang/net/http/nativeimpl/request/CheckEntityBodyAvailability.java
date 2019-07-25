/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.HttpUtil.checkRequestBodySizeHeadersAvailability;

/**
 * Check whether the entity body is present. Entity body can either be a byte channel, fully constructed
 * message data source or a set of body parts.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "checkEntityBodyAvailability",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Request",
                             structPackage = "ballerina/http"),
        isPublic = false
)
public class CheckEntityBodyAvailability {
    public static boolean checkEntityBodyAvailability(Strand strand, ObjectValue requestObj) {
        ObjectValue entityObj = (ObjectValue) requestObj.get(REQUEST_ENTITY_FIELD);
        return lengthHeaderCheck(requestObj) || EntityBodyHandler.checkEntityBodyAvailability(entityObj);
    }

    private static boolean lengthHeaderCheck(ObjectValue requestObj) {
        Object outboundMsg = requestObj.getNativeData(TRANSPORT_MESSAGE);
        if (outboundMsg == null) {
            return false;
        }
        return checkRequestBodySizeHeadersAvailability((HttpCarbonMessage) outboundMsg);
    }
}
