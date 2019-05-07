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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.TempCallableUnitCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.InputStream;

import static org.ballerinalang.mime.util.EntityBodyHandler.constructBlobDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructJsonDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructStringDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructXmlDataSource;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;

/**
 * {@code AbstractGetPayloadHandler} is the base class for all get entity body functions.
 *
 * @since 0.995-r1
 */

public abstract class AbstractGetPayloadHandler implements NativeCallableUnit {

    @Override
    public boolean isBlocking() {
        return false;
    }

    static void constructNonBlockingDataSource(Strand strand, TempCallableUnitCallback callback, ObjectValue entity,
                                        SourceType sourceType) {
        HttpCarbonMessage inboundMessage = extractTransportMessageFromEntity(entity);
        inboundMessage.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete(HttpCarbonMessage inboundMessage) {
                Object dataSource = null;
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundMessage);
                InputStream inputStream = dataStreamer.getInputStream();
                switch (sourceType) {
                    case JSON:
                        dataSource = constructJsonDataSource(entity, inputStream);
                        break;
                    case TEXT:
                        dataSource = constructStringDataSource(entity, inputStream);
                        break;
                    case XML:
                        dataSource = constructXmlDataSource(entity, inputStream);
                        break;
                    case BLOB:
                        dataSource = constructBlobDataSource(inputStream);
                        break;
                }
                updateDataSourceAndNotify(strand, callback, entity, dataSource);
            }

            @Override
            public void onError(Exception ex) {
                createErrorAndNotify(strand, callback,
                                     "Error occurred while extracting content from message : " + ex.getMessage());
            }
        });
        callback.sync();
    }

    static void setReturnValuesAndNotify(Strand strand, TempCallableUnitCallback callback, Object result) {
        strand.setReturnValues(result);
        strand.resume();
        //TODO remove this call back
        callback.setReturnValues(result);
        callback.notifySuccess();
    }

    static void createErrorAndNotify(Strand strand, TempCallableUnitCallback callback, String errMsg) {
        ErrorValue error = MimeUtil.createError(errMsg);
        setReturnValuesAndNotify(strand, callback, error);
    }

    static void updateDataSourceAndNotify(Strand strand, TempCallableUnitCallback callback, ObjectValue entityObj,
                                   Object result) {
        EntityBodyHandler.addMessageDataSource(entityObj, result);
        //Set byte channel to null, once the message data source has been constructed
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
        setReturnValuesAndNotify(strand, callback, result);
    }

    private static HttpCarbonMessage extractTransportMessageFromEntity(ObjectValue entityObj) {
        HttpCarbonMessage message = (HttpCarbonMessage) entityObj.getNativeData(TRANSPORT_MESSAGE);
        if (message != null) {
            return message;
        } else {
            throw new BallerinaIOException("Empty content");
        }
    }

    enum SourceType {
        JSON,
        XML,
        TEXT,
        BLOB
    }
}
