/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

/**
 * {@code AbstractGetPayloadHandler} is the base class for all get entity body functions.
 *
 * @since 0.990.4
 */

public abstract class AbstractGetPayloadHandler implements NativeCallableUnit {

    static final String ERROR_OCCURRED_WHILE_EXTRACTING = "Error occurred while extracting ";
    static final String COMPATIBLE_SINCE_CONTENT_TYPE = "compatible since the received content-type is : ";

    @Override
    public boolean isBlocking() {
        return false;
    }

    void setReturnValuesAndNotify(Context context, CallableUnitCallback callback, BValue result) {
        context.setReturnValues(result);
        callback.notifySuccess();
    }

    void createErrorAndNotify(Context context, CallableUnitCallback callback, String errMsg) {
        BError error = MimeUtil.createError(context, errMsg);
        setReturnValuesAndNotify(context, callback, error);
    }

    void updateDataSourceAndNotify(Context context, CallableUnitCallback callback, BMap<String, BValue> entityStruct,
                                   BValue result) {
        EntityBodyHandler.addMessageDataSource(entityStruct, result);
        //Set byte channel to null, once the message data source has been constructed
        entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
        setReturnValuesAndNotify(context, callback, result);
    }

    HttpCarbonMessage getInboundCarbonMessage(BMap<String, BValue> entityStruct) {
        Object message = entityStruct.getNativeData(TRANSPORT_MESSAGE);
        if (message != null) {
            return (HttpCarbonMessage) message;
        } else {
            throw new BallerinaIOException("Empty content");
        }
    }

    void constructNonBlockingStringDataSource(Context context, CallableUnitCallback callback,
                                              BMap<String, BValue> entityStruct) {
        HttpCarbonMessage inboundCarbonMsg = getInboundCarbonMessage(entityStruct);
        inboundCarbonMsg.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete() {
                String textContent;
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundCarbonMsg);
                String contentTypeValue = HeaderUtil.getHeaderValue(entityStruct,
                                                                    HttpHeaderNames.CONTENT_TYPE.toString());
                if (isNotNullAndEmpty(contentTypeValue)) {
                    String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                    if (isNotNullAndEmpty(charsetValue)) {
                        textContent = StringUtils.getStringFromInputStream(dataStreamer.getInputStream(),
                                                                           charsetValue);
                    } else {
                        textContent = StringUtils.getStringFromInputStream(dataStreamer.getInputStream());
                    }
                } else {
                    textContent = StringUtils.getStringFromInputStream(dataStreamer.getInputStream());
                }
                updateDataSourceAndNotify(context, callback, entityStruct, new BString(textContent));
            }

            @Override
            public void onError(Exception e) {
                createErrorAndNotify(context, callback, ERROR_OCCURRED_WHILE_EXTRACTING +
                        "text content from message: " + e.getMessage());
            }
        });
    }
}
