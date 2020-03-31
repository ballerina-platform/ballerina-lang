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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.FullHttpMessageListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static org.ballerinalang.mime.util.EntityBodyHandler.constructBlobDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructJsonDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructStringDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.constructXmlDataSource;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.NO_CONTENT_ERROR_CODE;
import static org.ballerinalang.mime.util.MimeConstants.PARSING_ENTITY_BODY_FAILED;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;

/**
 * {@code AbstractGetPayloadHandler} is the base class for all get entity body functions.
 *
 * @since 0.995-r1
 */

public abstract class AbstractGetPayloadHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractGetPayloadHandler.class);

    public static void constructNonBlockingDataSource(NonBlockingCallback callback, ObjectValue entity,
                                                      SourceType sourceType) {
        HttpCarbonMessage inboundMessage = extractTransportMessageFromEntity(entity);
        inboundMessage.getFullHttpCarbonMessage().addListener(new FullHttpMessageListener() {
            @Override
            public void onComplete(HttpCarbonMessage inboundMessage) {
                Object dataSource = null;
                HttpMessageDataStreamer dataStreamer = new HttpMessageDataStreamer(inboundMessage);
                InputStream inputStream = dataStreamer.getInputStream();
                try {
                    switch (sourceType) {
                        case JSON:
                            dataSource = constructJsonDataSource(entity, inputStream);
                            updateJsonDataSourceAndNotify(callback, entity, dataSource);
                            return;
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
                    updateDataSourceAndNotify(callback, entity, dataSource);
                } catch (Exception e) {
                    createParsingEntityBodyFailedErrorAndNotify(callback,
                            "Error occurred while extracting " + sourceType.toString().toLowerCase(Locale.ENGLISH)
                                    + " data from entity: " + getErrorMsg(e), null);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        log.error("Error occurred while closing the inbound data stream", ex);
                    }
                }
            }

            @Override
            public void onError(Exception ex) {
                createParsingEntityBodyFailedErrorAndNotify(callback,
                        "Error occurred while extracting content from message : " + ex.getMessage(), null);
            }
        });
    }

    private static void setReturnValuesAndNotify(NonBlockingCallback callback, Object result) {
        callback.setReturnValues(result);
        callback.notifySuccess();
    }

    static Object createParsingEntityBodyFailedErrorAndNotify(NonBlockingCallback callback, String errMsg,
                                                              ErrorValue errorValue) {
        ErrorValue error = MimeUtil.createError(PARSING_ENTITY_BODY_FAILED, errMsg, errorValue);
        return returnErrorValue(callback, error);
    }

    private static Object returnErrorValue(NonBlockingCallback callback, Object err) {
        if (callback != null) {
            setReturnValuesAndNotify(callback, err);
            return null;
        }
        return err;
    }

    static String getErrorMsg(Throwable err) {
        return err instanceof ErrorValue ? err.toString() : err.getMessage();
    }

    static void updateDataSource(ObjectValue entityObj, Object result) {
        EntityBodyHandler.addMessageDataSource(entityObj, result);
        removeByteChannel(entityObj);
    }

    static void updateJsonDataSource(ObjectValue entityObj, Object result) {
        EntityBodyHandler.addJsonMessageDataSource(entityObj, result);
        removeByteChannel(entityObj);
    }

    private static void removeByteChannel(ObjectValue entityObj) {
        //Set byte channel to null, once the message data source has been constructed
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
    }

    private static void updateDataSourceAndNotify(NonBlockingCallback callback, ObjectValue entityObj,
                                                  Object result) {
        updateDataSource(entityObj, result);
        setReturnValuesAndNotify(callback, result);
    }


    private static void updateJsonDataSourceAndNotify(NonBlockingCallback callback, ObjectValue entityObj,
                                                      Object result) {
        updateJsonDataSource(entityObj, result);
        setReturnValuesAndNotify(callback, result);
    }

    private static HttpCarbonMessage extractTransportMessageFromEntity(ObjectValue entityObj) {
        HttpCarbonMessage message = (HttpCarbonMessage) entityObj.getNativeData(TRANSPORT_MESSAGE);
        if (message != null) {
            return message;
        } else {
            throw BallerinaErrors.createError(NO_CONTENT_ERROR_CODE, "Empty content");
        }
    }

    /**
     * Type of content to construct the data source.
     */
    public enum SourceType {
        JSON,
        XML,
        TEXT,
        BLOB
    }
}
