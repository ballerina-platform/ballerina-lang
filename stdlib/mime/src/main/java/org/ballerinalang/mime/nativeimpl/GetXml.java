/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.nativeimpl;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.mime.util.DataContext;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.util.Locale;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.mime.util.MimeConstants.XML_SUFFIX;
import static org.ballerinalang.mime.util.MimeConstants.XML_TYPE_IDENTIFIER;

/**
 * Get the entity body in xml form.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getXml",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.XML), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetXml implements NativeCallableUnit {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback);
        try {
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            String baseType = HeaderUtil.getBaseType(entityStruct);
            if (baseType != null && (baseType.toLowerCase(Locale.getDefault()).endsWith(XML_TYPE_IDENTIFIER) ||
                    baseType.toLowerCase(Locale.getDefault()).endsWith(XML_SUFFIX))) {
                BXML result;
                BValue dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                if (dataSource != null) {
                    if (dataSource instanceof BXML) {
                        result = (BXML) dataSource;
                    } else {
                        // Build the XML from string representation of the payload.
                        BString payload = MimeUtil.getMessageAsString(dataSource);
                        result = XMLUtils.parse(payload.stringValue());
                    }
                    dataContext.setReturnValuesAndNotify(result);
                } else {
                    if (EntityBodyHandler.isBodyPartEntity(entityStruct)) {
                        result = EntityBodyHandler.constructXmlDataSource(entityStruct);
                        EntityBodyHandler.addMessageDataSource(entityStruct, result);
                        //Set byte channel to null, once the message data source has been constructed
                        entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                        dataContext.setReturnValuesAndNotify(result);
                    } else {
                        constructNonBlockingXmlDataSource(dataContext, entityStruct);
                    }
                }
            } else {
                dataContext.createErrorAndNotify(
                        "Entity body is not xml compatible since the received content-type is : " + baseType);
            }
        } catch (Throwable e) {
            dataContext.createErrorAndNotify(
                    "Error occurred while retrieving xml data from entity : " + e.getMessage());
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private void constructNonBlockingXmlDataSource(DataContext dataContext, BMap<String, BValue> entityStruct) {
        HttpCarbonMessage inboundCarbonMsg = (HttpCarbonMessage) entityStruct.getNativeData(TRANSPORT_MESSAGE);
        inboundCarbonMsg.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete() {
                BXML xmlContent;
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundCarbonMsg);
                String contentTypeValue = HeaderUtil.getHeaderValue(entityStruct,
                                                                    HttpHeaderNames.CONTENT_TYPE.toString());
                if (contentTypeValue != null && !contentTypeValue.isEmpty()) {
                    String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                    if (charsetValue != null && !charsetValue.isEmpty()) {
                        xmlContent = XMLUtils.parse(dataStreamer.getInputStream(), charsetValue);
                    } else {
                        xmlContent = XMLUtils.parse(dataStreamer.getInputStream());
                    }
                } else {
                    xmlContent = XMLUtils.parse(dataStreamer.getInputStream());
                }
                EntityBodyHandler.addMessageDataSource(entityStruct, xmlContent);
                //Set byte channel to null, once the message data source has been constructed
                entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                dataContext.setReturnValuesAndNotify(xmlContent);
            }

            @Override
            public void onError(Exception e) {
                dataContext.createErrorAndNotify(
                        "Error occurred while extracting xml content from message: " + e.getMessage());
            }
        });
    }
}
