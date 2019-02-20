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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;

/**
 * Get the entity body as a blob.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getByteArray",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetByteArray implements NativeCallableUnit {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback);

        try {
            BValueArray result = null;
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            BValue messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (messageDataSource != null) {
                if (messageDataSource instanceof BValueArray) {
                    result = (BValueArray) messageDataSource;
                } else {
                    String contentTypeValue = HeaderUtil.getHeaderValue(entityStruct,
                            HttpHeaderNames.CONTENT_TYPE.toString());
                    if (contentTypeValue != null && !contentTypeValue.isEmpty()) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (charsetValue != null && !charsetValue.isEmpty()) {
                            result = new BValueArray(messageDataSource.stringValue().getBytes(charsetValue));
                        } else {
                            result = new BValueArray(messageDataSource.stringValue().getBytes(
                                    Charset.defaultCharset()));
                        }
                    }
                }
                dataContext.setReturnValuesAndNotify(result != null ? result : new BValueArray(new byte[0]));
            } else {
                if (EntityBodyHandler.isBodyPartEntity(entityStruct)) {
                    result = EntityBodyHandler.constructBlobDataSource(entityStruct);
                    EntityBodyHandler.addMessageDataSource(entityStruct, result);
                    //Set byte channel to null, once the message data source has been constructed
                    entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                    dataContext.setReturnValuesAndNotify(result);
                } else {
                    constructNonBlockingBlobDataSource(dataContext, entityStruct);
                }
            }
        } catch (Throwable e) {
            dataContext.createErrorAndNotify(
                    "Error occurred while extracting blob data from entity : " + e.getMessage());
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private void constructNonBlockingBlobDataSource(DataContext dataContext, BMap<String, BValue> entityStruct) {
        HttpCarbonMessage inboundCarbonMsg = (HttpCarbonMessage) entityStruct.getNativeData(TRANSPORT_MESSAGE);
        inboundCarbonMsg.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete() {
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundCarbonMsg);
                try {
                    byte[] byteData = MimeUtil.getByteArray(dataStreamer.getInputStream());
                    BValueArray result = new BValueArray(byteData != null ? byteData : new byte[0]);
                    EntityBodyHandler.addMessageDataSource(entityStruct, result);
                    //Set byte channel to null, once the message data source has been constructed
                    entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                    dataContext.setReturnValuesAndNotify(result);
                } catch (IOException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                dataContext.createErrorAndNotify(
                        "Error occurred while extracting blob content from message: " + e.getMessage());
            }
        });
    }
}
