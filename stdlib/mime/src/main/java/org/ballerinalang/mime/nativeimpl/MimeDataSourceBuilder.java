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

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityHeaderHandler;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.PARSER_ERROR;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

/**
 * Utilities related to MIME entity body that can be built as a data source.
 *
 * @since 1.1.0
 */
public abstract class MimeDataSourceBuilder {

    public static Object getByteArray(BObject entityObj) {
        try {
            Object messageDataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (messageDataSource != null) {
                return getAlreadyBuiltByteArray(entityObj, messageDataSource);
            }
            BArray result = EntityBodyHandler.constructBlobDataSource(entityObj);
            updateDataSource(entityObj, result);
            return result;
        } catch (Exception ex) {
            return createError(ex, "blob");
        }
    }

    protected static Object getAlreadyBuiltByteArray(BObject entityObj, Object messageDataSource)
            throws UnsupportedEncodingException {
        if (messageDataSource instanceof BArray) {
            return messageDataSource;
        }
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entityObj, MimeConstants.CONTENT_TYPE);
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                return ValueCreator.createArrayValue(
                        StringUtils.getJsonString(messageDataSource).getBytes(charsetValue));
            }
            return ValueCreator.createArrayValue(
                    StringUtils.getJsonString(messageDataSource).getBytes(Charset.defaultCharset()));
        }
        return ValueCreator.createArrayValue(new byte[0]);
    }

    public static Object getJson(BObject entityObj) {
        BRefValue result;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return getAlreadyBuiltJson(dataSource);
            }
            result = (BRefValue) EntityBodyHandler.constructJsonDataSource(entityObj);
            updateJsonDataSource(entityObj, result);
            return result;
        } catch (Exception ex) {
            return createError(ex, "json");
        }
    }

    protected static Object getAlreadyBuiltJson(Object dataSource) {
        // If the value is already a JSON, then return as it is.
        BRefValue result;
        if (isJSON(dataSource)) {
            result = (BRefValue) dataSource;
        } else {
            // Else, build the JSON from the string representation of the payload.
            String payload = MimeUtil.getMessageAsString(dataSource);
            result = (BRefValue) JSONParser.parse(payload);
        }
        return result;
    }

    private static boolean isJSON(Object value) {
        // If the value is string, it could represent any type of payload.
        // Therefore it needs to be parsed as JSON.
        Type objectType = TypeChecker.getType(value);
        return objectType.getTag() != TypeTags.STRING && MimeUtil.isJSONCompatible(objectType);
    }

    public static Object getText(BObject entityObj) {
        BString result;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return StringUtils.fromString(MimeUtil.getMessageAsString(dataSource));
            }
            result = EntityBodyHandler.constructStringDataSource(entityObj);
            updateDataSource(entityObj, result);
            return result;
        } catch (Exception ex) {
            return createError(ex, "text");
        }
    }

    public static Object getXml(BObject entityObj) {
        BXML result;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return getAlreadyBuiltXml(dataSource);
            }
            result = EntityBodyHandler.constructXmlDataSource(entityObj);
            updateDataSource(entityObj, result);
            return result;

        } catch (Exception ex) {
            return createError(ex, "xml");
        }
    }

    protected static Object getAlreadyBuiltXml(Object dataSource) {
        if (dataSource instanceof BXML) {
            return dataSource;
        }
        // Build the XML from string representation of the payload.
        String payload = MimeUtil.getMessageAsString(dataSource);
        return XMLFactory.parse(payload);
    }

    protected static void updateDataSource(BObject entityObj, Object result) {
        EntityBodyHandler.addMessageDataSource(entityObj, result);
        removeByteChannel(entityObj);
    }

    protected static void updateJsonDataSource(BObject entityObj, Object result) {
        EntityBodyHandler.addJsonMessageDataSource(entityObj, result);
        removeByteChannel(entityObj);
    }

    private static void removeByteChannel(BObject entityObj) {
        //Set byte channel to null, once the message data source has been constructed
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
    }

    protected static Object createError(Exception ex, String type) {
        String message = "Error occurred while extracting " + type + " data from entity";
        if (ex instanceof BError) {
            return MimeUtil.createError(PARSER_ERROR, message, (BError) ex);
        }
        return MimeUtil.createError(PARSER_ERROR, message + ": " + getErrorMsg(ex), null);
    }

    protected static String getErrorMsg(Throwable err) {
        return err instanceof BError ? err.toString() : err.getMessage();
    }
}
