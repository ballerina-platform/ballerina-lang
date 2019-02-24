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
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
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

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

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
public class GetJson extends AbstractGetPayloadHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BRefType<?> result;
            BMap<String, BValue> entity = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);

            if (!MimeUtil.isJSONContentType(entity)) {
                String baseType = HeaderUtil.getBaseType(entity);
                createErrorAndNotify(context, callback, "Entity body is not json " + COMPATIBLE_SINCE_CONTENT_TYPE +
                        baseType);
                return;
            }

            BValue dataSource = EntityBodyHandler.getMessageDataSource(entity);
            if (dataSource != null) {
                // If the value is a already JSON, then return it as is.
                if (isJSON(dataSource)) {
                    result = (BRefType<?>) dataSource;
                } else {
                    // Else, build the JSON from the string representation of the payload.
                    BString payload = MimeUtil.getMessageAsString(dataSource);
                    result = JsonParser.parse(payload.stringValue());
                }
                setReturnValuesAndNotify(context, callback, result);
                return;
            }

            if (isStreamingRequired(entity)) {
                result = EntityBodyHandler.constructJsonDataSource(entity);
                updateDataSourceAndNotify(context, callback, entity, result);
                return;
            }

            // Construct non-blocking JSON data source
            HttpCarbonMessage inboundCarbonMsg = getInboundCarbonMessage(entity);
            inboundCarbonMsg.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
                @Override
                public void onComplete() {
                    BRefType<?> jsonData;
                    HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundCarbonMsg);
                    String contentTypeValue = HeaderUtil.getHeaderValue(entity,
                                                                        HttpHeaderNames.CONTENT_TYPE.toString());
                    if (isNotNullAndEmpty(contentTypeValue)) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (isNotNullAndEmpty(charsetValue)) {
                            jsonData = JsonParser.parse(dataStreamer.getInputStream(), charsetValue);
                        } else {
                            jsonData = JsonParser.parse(dataStreamer.getInputStream());
                        }
                    } else {
                        jsonData = JsonParser.parse(dataStreamer.getInputStream());
                    }
                    updateDataSourceAndNotify(context, callback, entity, jsonData);
                }

                @Override
                public void onError(Exception e) {
                    createErrorAndNotify(context, callback, ERROR_OCCURRED_WHILE_EXTRACTING +
                            "json content from content collector: " + e.getMessage());
                }
            });
        } catch (Throwable e) {
            createErrorAndNotify(context, callback, ERROR_OCCURRED_WHILE_EXTRACTING +
                    "json data from entity: " + e.getMessage());
        }
    }

    private boolean isJSON(BValue value) {
        // If the value is string, it could represent any type of payload.
        // Therefore it needs to be parsed as JSON.
        return value.getType().getTag() != TypeTags.STRING && MimeUtil.isJSONCompatible(value.getType());
    }
}
