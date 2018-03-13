/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.httpserverconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * {@code RespondContinue} is the respondContinue action implementation of the HTTP Server Connector.
 *
 * @since 0.965.0
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "respondContinue",
        connectorName = HttpConstants.SERVER_CONNECTOR,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                structPackage = "ballerina.net.http")
)
public class RespondContinue extends AbstractConnectorAction {

    @Override
    public void execute(Context context) {
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        HTTPCarbonMessage inboundRequestMsg =
                (HTTPCarbonMessage) bConnector.getNativeData(HttpConstants.TRANSPORT_MESSAGE);

        BStruct outboundResponseStruct = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, HttpConstants.RESPONSE);
        HTTPCarbonMessage outboundResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 100);
        prepareAndRespond(context, bConnector, inboundRequestMsg, outboundResponseStruct, outboundResponseMsg);
    }
}
