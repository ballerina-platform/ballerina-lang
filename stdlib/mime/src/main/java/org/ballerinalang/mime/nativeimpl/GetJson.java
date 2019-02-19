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
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;

/**
 * Get the entity body in JSON form.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getJson",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.JSON), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetJson implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback);
        try {
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            String baseType = HeaderUtil.getBaseType(entityStruct);
            if (MimeUtil.isJSONContentType(entityStruct)) {
                BValue dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                if (dataSource != null) {
                    BRefType<?> result;
                    // If the value is a already JSON, then return it as is.
                    if (isJSON(dataSource)) {
                        result = (BRefType<?>) dataSource;
                    } else {
                        // Else, build the JSON from the string representation of the payload.
                        BString payload = MimeUtil.getMessageAsString(dataSource);
                        result = JsonParser.parse(payload.stringValue());
                    }
                    dataContext.setReturnValuesAndNotify(result);
                } else {
                    constructJsonDataSource(dataContext, entityStruct);
                }
            } else {
                dataContext.createErrorAndNotify(
                        "Entity body is not json compatible since the received content-type is : " + baseType);
            }
        } catch (Throwable e) {
            dataContext.createErrorAndNotify(
                    "Error occurred while extracting json data from entity: " + e.getMessage());
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private void constructJsonDataSource(DataContext dataContext, BMap<String, BValue> entityStruct) {
        HttpCarbonMessage inboundCarbonMsg = (HttpCarbonMessage) entityStruct.getNativeData(TRANSPORT_MESSAGE);

        inboundCarbonMsg.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete() {
                BRefType<?> jsonData;
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundCarbonMsg);
                String contentTypeValue = HeaderUtil.getHeaderValue(entityStruct,
                                                                    HttpHeaderNames.CONTENT_TYPE.toString());
                if (contentTypeValue != null && !contentTypeValue.isEmpty()) {
                    String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                    if (charsetValue != null && !charsetValue.isEmpty()) {
                        jsonData = JsonParser.parse(dataStreamer.getInputStream(), charsetValue);
                    } else {
                        jsonData = JsonParser.parse(dataStreamer.getInputStream());
                    }
                } else {
                    jsonData = JsonParser.parse(dataStreamer.getInputStream());
                }
                EntityBodyHandler.addMessageDataSource(entityStruct, jsonData);
                //Set byte channel to null, once the message data source has been constructed
                entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                dataContext.setReturnValuesAndNotify(jsonData);
            }

            @Override
            public void onError(Exception e) {
                dataContext.createErrorAndNotify(
                        "Error occurred while extracting json content from message: " + e.getMessage());
            }
        });
    }

    private boolean isJSON(BValue value) {
        // If the value is string, it could represent any type of payload.
        // Therefore it needs to be parsed as JSON.
        return value.getType().getTag() != TypeTags.STRING && MimeUtil.isJSONCompatible(value.getType());
    }
}
