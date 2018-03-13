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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BEnumerator;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code Redirect} is the redirect action implementation of the HTTP Server Connector.
 *
 * @since 0.965.0
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "redirect",
        connectorName = HttpConstants.SERVER_CONNECTOR,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                        structPackage = "ballerina.net.http"),
                @Argument(name = "code", type = TypeKind.ENUM),
                @Argument(name = "locations", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                structPackage = "ballerina.net.http")
)
public class Redirect extends AbstractConnectorAction {

    @Override
    public void execute(Context context) {
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        HTTPCarbonMessage inboundRequestMsg =
                (HTTPCarbonMessage) bConnector.getNativeData(HttpConstants.TRANSPORT_MESSAGE);

        BStruct outboundResponseStruct = (BStruct) context.getRefArgument(1);
        HTTPCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseStruct, HttpUtil.createHttpCarbonMessage(false));

        BEnumerator code = (BEnumerator) context.getRefArgument(2);

        if (code.stringValue().equals(RedirectCode.MULTIPLE_CHOICES_300.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 300);
        } else if (code.stringValue().equals(RedirectCode.MOVED_PERMANENTLY_301.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 301);
        } else if (code.stringValue().equals(RedirectCode.FOUND_302.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 302);
        } else if (code.stringValue().equals(RedirectCode.SEE_OTHER_303.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 303);
        } else if (code.stringValue().equals(RedirectCode.NOT_MODIFIED_304.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 304);
        } else if (code.stringValue().equals(RedirectCode.USE_PROXY_305.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 305);
        } else if (code.stringValue().equals(RedirectCode.TEMPORARY_REDIRECT_307.toString())) {
            outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 307);
        }

        BStringArray locations = (BStringArray) context.getRefArgument(3);

        StringBuilder locationsStr = new StringBuilder();
        for (int index = 0; index < locations.size(); index++) {
            locationsStr.append(locations.get(index)).append(",");
        }
        outboundResponseMsg.setHeader(HttpHeaderNames.LOCATION.toString(),
                locationsStr.substring(0, locationsStr.length() - 1));

        prepareAndRespond(context, bConnector, inboundRequestMsg, outboundResponseStruct, outboundResponseMsg);
    }
}
