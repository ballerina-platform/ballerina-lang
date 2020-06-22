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
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

/**
 * Utilities related to MIME entity body that can be built as a data source.
 *
 * @since 1.1.0
 */
public class MimeDataSourceBuilder extends AbstractGetPayloadHandler {

    public static Object getByteArray(ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        ArrayValue result = null;
        try {
            Object messageDataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (messageDataSource != null) {
                if (messageDataSource instanceof ArrayValue) {
                    result = (ArrayValue) messageDataSource;
                } else {
                    String contentTypeValue = HeaderUtil.getHeaderValue(entityObj,
                                                                        HttpHeaderNames.CONTENT_TYPE.toString());
                    if (isNotNullAndEmpty(contentTypeValue)) {
                        String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
                        if (isNotNullAndEmpty(charsetValue)) {
                            result = new ArrayValueImpl(StringUtils.getJsonString(messageDataSource)
                                                                .getBytes(charsetValue));
                        } else {
                            result = new ArrayValueImpl(StringUtils.getJsonString(messageDataSource)
                                                                .getBytes(Charset.defaultCharset()));
                        }
                    }
                }
                return result != null ? result : new ArrayValueImpl(new byte[0]);
            }

            Object transportMessage = entityObj.getNativeData(TRANSPORT_MESSAGE);
            if (isStreamingRequired(entityObj) || transportMessage == null) {
                result = EntityBodyHandler.constructBlobDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(Scheduler.getStrand());
                constructNonBlockingDataSource(callback, entityObj, SourceType.BLOB);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                                   "Error occurred while extracting blob data from " +
                                                                           "entity", (ErrorValue) ex);
            }
            createParsingEntityBodyFailedErrorAndNotify(callback,
                                                        "Error occurred while extracting blob data from entity : " +
                                                                getErrorMsg(ex), null);
        }
        return result;
    }

    public static Object getJson(ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        RefValue result = null;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                // If the value is already a JSON, then return as it is.
                if (isJSON(dataSource)) {
                    result = (RefValue) dataSource;
                } else {
                    // Else, build the JSON from the string representation of the payload.
                    String payload = MimeUtil.getMessageAsString(dataSource);
                    result = (RefValue) JSONParser.parse(payload);
                }
                return result;
            }

            if (isStreamingRequired(entityObj)) {
                result = (RefValue) EntityBodyHandler.constructJsonDataSource(entityObj);
                updateJsonDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(Scheduler.getStrand());
                constructNonBlockingDataSource(callback, entityObj, SourceType.JSON);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                                   "Error occurred while extracting json data from " +
                                                                           "entity", (ErrorValue) ex);
            }
            return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                               "Error occurred while extracting json data from " +
                                                                       "entity: " + getErrorMsg(ex), null);
        }
        return result;
    }

    private static boolean isJSON(Object value) {
        // If the value is string, it could represent any type of payload.
        // Therefore it needs to be parsed as JSON.
        BType objectType = TypeChecker.getType(value);
        return objectType.getTag() != TypeTags.STRING && MimeUtil.isJSONCompatible(objectType);
    }

    public static Object getText(ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        BString result = null;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return org.ballerinalang.jvm.StringUtils.fromString(MimeUtil.getMessageAsString(dataSource));
            }

            if (isStreamingRequired(entityObj)) {
                result = EntityBodyHandler.constructStringDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(Scheduler.getStrand());
                constructNonBlockingDataSource(callback, entityObj, SourceType.TEXT);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                                   "Error occurred while extracting text data from " +
                                                                           "entity",
                                                                   (ErrorValue) ex);
            }
            return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                               "Error occurred while extracting text data from entity" +
                                                                       " : " + getErrorMsg(ex), null);
        }
        return result;
    }

    public static Object getXml(ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        XMLValue result = null;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                if (dataSource instanceof XMLValue) {
                    result = (XMLValue) dataSource;
                } else {
                    // Build the XML from string representation of the payload.
                    String payload = MimeUtil.getMessageAsString(dataSource);
                    result = XMLFactory.parse(payload);
                }
                return result;
            }

            if (isStreamingRequired(entityObj)) {
                result = EntityBodyHandler.constructXmlDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(Scheduler.getStrand());
                constructNonBlockingDataSource(callback, entityObj, SourceType.XML);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                                   "Error occurred while extracting xml data from " +
                                                                           "entity", (ErrorValue) ex);
            }
            return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                               "Error occurred while extracting xml data from entity " +
                                                                       ": " + getErrorMsg(ex), null);
        }
        return result;
    }
}
