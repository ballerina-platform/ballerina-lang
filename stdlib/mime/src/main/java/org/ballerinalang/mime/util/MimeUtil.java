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

package org.ballerinalang.mime.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.internal.PlatformDependent;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.ASSIGNMENT;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILENAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_PARA_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_SUB_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.DOUBLE_QUOTE;
import static org.ballerinalang.mime.util.MimeConstants.FORM_DATA_PARAM;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_LENGTH_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_TYPE_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.MimeConstants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.MimeConstants.ONE_BYTE;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BUFFER_SIZE;
import static org.ballerinalang.mime.util.MimeConstants.SEMICOLON;
import static org.ballerinalang.mime.util.MimeConstants.SIZE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUBTYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_ATTACHMENT;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_FIELD;

/**
 * Mime utility functions are included in here.
 *
 * @since 0.96
 */
public class MimeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MimeUtil.class);

    /**
     * Given a ballerina entity, get the content-type as a base type.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype' format
     */
    public static String getBaseType(ObjectValue entity) {
        if (entity.get(MEDIA_TYPE_FIELD) != null) {
            ObjectValue mediaType = (ObjectValue) entity.get(MEDIA_TYPE_FIELD);
            if (mediaType != null) {
                return mediaType.get(PRIMARY_TYPE_FIELD).toString() + "/" +
                        mediaType.get(SUBTYPE_FIELD).toString();
            }
        }
        return null;
    }

    /**
     * Given a ballerina entity, get the content-type with parameters included.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    public static String getContentTypeWithParameters(ObjectValue entity) {
        if (entity.get(MEDIA_TYPE_FIELD) == null) {
            return HeaderUtil.getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
        }
        ObjectValue mediaType = (ObjectValue) entity.get(MEDIA_TYPE_FIELD);
        String primaryType = String.valueOf(mediaType.get(PRIMARY_TYPE_FIELD));
        String subType = String.valueOf(mediaType.get(SUBTYPE_FIELD));
        String contentType = null;
        if (!primaryType.isEmpty() && !subType.isEmpty()) {
            contentType = primaryType + "/" + subType;
            if (mediaType.get(PARAMETER_MAP_FIELD) != null) {
                MapValue<BString, BString> map = mediaType.get(PARAMETER_MAP_FIELD) != null ?
                        (MapValue<BString, BString>) mediaType.get(PARAMETER_MAP_FIELD) : null;
                if (map != null && !map.isEmpty()) {
                    contentType = contentType + SEMICOLON;
                    return HeaderUtil.appendHeaderParams(new StringBuilder(contentType), map);
                }
            }
        }
        return contentType;
    }

    /**
     * Get parameter value from the content-type header.
     *
     * @param contentType   Content-Type value as a string
     * @param parameterName Name of the parameter
     * @return Parameter value as a string
     */
    public static String getContentTypeBParamValue(String contentType, String parameterName) {
        try {
            MimeType mimeType = new MimeType(contentType);
            MimeTypeParameterList parameterList = mimeType.getParameters();
            return parameterList.get(parameterName);
        } catch (MimeTypeParseException e) {
            throw MimeUtil.createError(INVALID_CONTENT_TYPE_ERROR, e.getMessage());
        }
    }

    /**
     * Get parameter value from the content-type header.
     *
     * @param contentType   Content-Type value as a string
     * @param parameterName Name of the parameter
     * @return Parameter value as a string
     */
    public static String getContentTypeParamValue(String contentType, String parameterName) {
        try {
            MimeType mimeType = new MimeType(contentType);
            MimeTypeParameterList parameterList = mimeType.getParameters();
            return parameterList.get(parameterName);
        } catch (MimeTypeParseException e) {
            throw MimeUtil.createError(INVALID_CONTENT_TYPE_ERROR, e.getMessage());
        }
    }

    /**
     * Construct 'MediaType' struct with the given Content-Type and set it into the given 'Entity'.
     *
     * @param mediaType    Represent 'MediaType' struct
     * @param entityStruct Represent 'Entity' struct
     * @param contentType  Content-Type value in string
     */
    public static void setContentType(ObjectValue mediaType, ObjectValue entityStruct,
                                      String contentType) {
        ObjectValue mimeType = parseMediaType(mediaType, contentType);
        if (contentType == null) {
            mimeType.set(PRIMARY_TYPE_FIELD, DEFAULT_PRIMARY_TYPE);
            mimeType.set(SUBTYPE_FIELD, DEFAULT_SUB_TYPE);
        }
        entityStruct.set(MEDIA_TYPE_FIELD, mimeType);
    }

    /**
     * Parse 'MediaType' struct with the given Content-Type.
     *
     * @param mediaType   Represent 'MediaType' struct
     * @param contentType Content-Type value in string
     * @return 'MediaType' struct populated with values
     */
    public static ObjectValue parseMediaType(ObjectValue mediaType, String contentType) {
        try {
            MapValueImpl<BString, BString> parameterMap =
                    new MapValueImpl<>(new org.ballerinalang.jvm.types.BMapType(BTypes.typeString));
            BString suffix, primaryType, subType;

            if (contentType != null) {
                MimeType mimeType = new MimeType(contentType);
                primaryType = org.ballerinalang.jvm.StringUtils.fromString(mimeType.getPrimaryType());

                String subTypeStr = mimeType.getSubType();
                subType = org.ballerinalang.jvm.StringUtils.fromString(subTypeStr);
                if (subTypeStr != null && subTypeStr.contains(SUFFIX_ATTACHMENT)) {
                    suffix = org.ballerinalang.jvm.StringUtils.fromString(
                            subTypeStr.substring(subTypeStr.lastIndexOf(SUFFIX_ATTACHMENT) + 1));
                } else {
                    suffix = BTypes.typeString.getZeroValue();
                }

                MimeTypeParameterList parameterList = mimeType.getParameters();
                Enumeration keys = parameterList.getNames();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    BString value = org.ballerinalang.jvm.StringUtils.fromString(parameterList.get(key));
                    parameterMap.put(org.ballerinalang.jvm.StringUtils.fromString(key), value);
                }
            } else {
                primaryType = suffix = subType = BTypes.typeString.getZeroValue();
            }

            mediaType.set(PRIMARY_TYPE_FIELD, primaryType);
            mediaType.set(SUBTYPE_FIELD, subType);
            mediaType.set(SUFFIX_FIELD, suffix);
            mediaType.set(PARAMETER_MAP_FIELD, parameterMap);
        } catch (MimeTypeParseException e) {
            throw MimeUtil.createError(INVALID_CONTENT_TYPE_ERROR, e.getMessage());
        }
        return mediaType;
    }

    public static void setMediaTypeToEntity(ObjectValue entityStruct, String contentType) {
        ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
        MimeUtil.setContentType(mediaType, entityStruct, contentType);
        HeaderUtil.setHeaderToEntity(entityStruct, HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
    }

    /**
     * Populate ContentDisposition struct and set it to body part.
     *
     * @param contentDisposition                 Represent the ContentDisposition struct that needs to be filled
     * @param bodyPart                           Represent a body part
     * @param contentDispositionHeaderWithParams Represent Content-Disposition header value with parameters
     */
    public static void setContentDisposition(ObjectValue contentDisposition, ObjectValue bodyPart,
                                             String contentDispositionHeaderWithParams) {
        populateContentDispositionObject(contentDisposition, contentDispositionHeaderWithParams);
        bodyPart.set(CONTENT_DISPOSITION_FIELD, contentDisposition);
    }

    public static void populateContentDispositionObject(ObjectValue contentDisposition,
                                                        String contentDispositionHeaderWithParams) {
        String dispositionValue;
        if (isNotNullAndEmpty(contentDispositionHeaderWithParams)) {
            if (contentDispositionHeaderWithParams.contains(SEMICOLON)) {
                dispositionValue = HeaderUtil.getHeaderValue(contentDispositionHeaderWithParams);
            } else {
                dispositionValue = contentDispositionHeaderWithParams;
            }
            contentDisposition.set(DISPOSITION_FIELD, org.ballerinalang.jvm.StringUtils.fromString(dispositionValue));
            MapValue<BString, BString> paramMap = HeaderUtil.getParamMap(contentDispositionHeaderWithParams);
            for (BString key : paramMap.getKeys()) {
                BString paramValue = paramMap.get(key);
                switch (key.getValue()) {
                    case CONTENT_DISPOSITION_FILE_NAME:
                        contentDisposition.set(CONTENT_DISPOSITION_FILENAME_FIELD, stripQuotes(paramValue));
                        break;
                    case CONTENT_DISPOSITION_NAME:
                        contentDisposition.set(CONTENT_DISPOSITION_NAME_FIELD, stripQuotes(paramValue));
                        break;
                    default:
                }
            }
            paramMap.remove(org.ballerinalang.jvm.StringUtils.fromString(CONTENT_DISPOSITION_FILE_NAME));
            paramMap.remove(org.ballerinalang.jvm.StringUtils.fromString(CONTENT_DISPOSITION_NAME));
            contentDisposition.set(CONTENT_DISPOSITION_PARA_MAP_FIELD, paramMap);
        }
    }

    /**
     * Given a ballerina entity, build the content-disposition header value from 'ContentDisposition' object.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    public static String getContentDisposition(ObjectValue entity) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (entity.get(CONTENT_DISPOSITION_FIELD) != null) {
            ObjectValue contentDispositionStruct =
                    (ObjectValue) entity.get(CONTENT_DISPOSITION_FIELD);
            if (contentDispositionStruct != null) {
                Object disposition = contentDispositionStruct.get(DISPOSITION_FIELD);
                if (disposition == null || disposition.toString().isEmpty()) {
                    String contentType = getBaseType(entity);
                    if (contentType != null && contentType.equals(MULTIPART_FORM_DATA)) {
                        dispositionBuilder.append(FORM_DATA_PARAM);
                    }
                } else {
                    dispositionBuilder.append(disposition);
                }
                if (!dispositionBuilder.toString().isEmpty()) {
                    dispositionBuilder = convertDispositionObjectToString(dispositionBuilder, contentDispositionStruct);
                }
            }
        }
        return dispositionBuilder.toString();
    }

    public static StringBuilder convertDispositionObjectToString(StringBuilder dispositionBuilder,
                                                                 ObjectValue contentDispositionStruct) {
        Object nameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_NAME_FIELD);
        String name = nameBVal != null ? nameBVal.toString() : null;

        Object fileNameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_FILENAME_FIELD);
        String fileName = fileNameBVal != null ? fileNameBVal.toString() : null;

        if (isNotNullAndEmpty(name)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                    includeQuotes(name)).append(SEMICOLON);
        }
        if (isNotNullAndEmpty(fileName)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                    .append(includeQuotes(fileName)).append(SEMICOLON);
        }
        if (contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD) != null) {
            MapValue<BString, BString> map =
                    (MapValue<BString, BString>) contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD);
            HeaderUtil.appendHeaderParams(appendSemiColon(dispositionBuilder), map);
        }

        if (dispositionBuilder.toString().endsWith(SEMICOLON)) {
            dispositionBuilder.setLength(dispositionBuilder.length() - 1);
        }
        return dispositionBuilder;
    }

    private static StringBuilder appendSemiColon(StringBuilder disposition) {
        if (!disposition.toString().endsWith(SEMICOLON)) {
            disposition.append(SEMICOLON);
        }
        return disposition;
    }

    /**
     * Populate given 'Entity' with it's body size.
     *
     * @param entityStruct Represent 'Entity'
     * @param length       Size of the entity body
     */
    public static void setContentLength(ObjectValue entityStruct, long length) {
        entityStruct.set(SIZE_FIELD, length);
    }

    /**
     * Write a given inputstream to a given outputstream.
     *
     * @param outputStream Represent the outputstream that the inputstream should be written to
     * @param inputStream  Represent the inputstream that that needs to be written to outputstream
     * @throws IOException When an error occurs while writing inputstream to outputstream
     */
    public static void writeInputToOutputStream(InputStream inputStream, OutputStream outputStream) throws
            IOException {
        byte[] buffer = new byte[READABLE_BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
    }

    /**
     * Given an input stream, get a byte array.
     *
     * @param input Represent an input stream
     * @return A byte array
     * @throws IOException In case an error occurs while reading input stream
     */
    public static byte[] getByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[READABLE_BUFFER_SIZE];
            for (int len; (len = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }

    /**
     * Check whether the given string is not null and empty.
     *
     * @param textValue Represent a text value
     * @return a boolean indicating the status of nullability and emptiness
     */
    public static boolean isNotNullAndEmpty(String textValue) {
        return textValue != null && !textValue.isEmpty();
    }

    /**
     * Surround the given value with quotes.
     *
     * @param textValue Represent a text value
     * @return a String surrounded by quotes
     */
    public static String includeQuotes(String textValue) {
        if (!textValue.startsWith(DOUBLE_QUOTE)) {
            textValue = DOUBLE_QUOTE + textValue;
        }
        if (!textValue.endsWith(DOUBLE_QUOTE)) {
            textValue = textValue + DOUBLE_QUOTE;
        }
        return textValue;
    }

    /**
     * Strip quotes.
     *
     * @param textValue Represent a text value
     * @return a String surrounded by quotes
     */
    public static BString stripQuotes(BString textValue) {
        if (textValue.getValue().startsWith(DOUBLE_QUOTE)) {
            textValue = textValue.substring(1, textValue.length());
        }
        if (textValue.getValue().endsWith(DOUBLE_QUOTE)) {
            textValue = textValue.substring(0, textValue.length() - 1);
        }
        return textValue;
    }

    /**
     * Get a new multipart boundary delimiter.
     *
     * @return a boundary string
     */
    public static String getNewMultipartDelimiter() {
        return Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
    }

    /**
     * Given a body part, check whether any nested parts are available.
     *
     * @param bodyPart Represent a ballerina body part
     * @return A boolean indicating nested parts availability
     */
    static boolean isNestedPartsAvailable(ObjectValue bodyPart) {
        String contentTypeOfChildPart = MimeUtil.getBaseType(bodyPart);
        return contentTypeOfChildPart != null && contentTypeOfChildPart.startsWith(MULTIPART_AS_PRIMARY_TYPE) &&
                bodyPart.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Create mime specific error with the error message.
     *
     * @param errorTypeName The error type
     * @param errMsg  The actual error message
     * @return Ballerina error value
     */
    public static ErrorValue createError(String errorTypeName, String errMsg) {
        return BallerinaErrors.createDistinctError(errorTypeName, PROTOCOL_MIME_PKG_ID, errMsg);
    }

    /**
     * Create mime specific error with the error message and cause.
     *
     * @param errorTypeName The error type
     * @param errMsg  The actual error message
     * @param errorValue  The error cause
     * @return Ballerina error value
     */
    public static ErrorValue createError(String errorTypeName, String errMsg, ErrorValue errorValue) {
        return BallerinaErrors.createDistinctError(errorTypeName, PROTOCOL_MIME_PKG_ID, errMsg, errorValue);
    }

    public static boolean isJSONCompatible(org.ballerinalang.jvm.types.BType type) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.JSON_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                return isJSONCompatible(((org.ballerinalang.jvm.types.BArrayType) type).getElementType());
            case TypeTags.MAP_TAG:
                return isJSONCompatible(((org.ballerinalang.jvm.types.BMapType) type).getConstrainedType());
            default:
                return false;
        }
    }

    public static String getMessageAsString(Object dataSource) {
        org.ballerinalang.jvm.types.BType type = TypeChecker.getType(dataSource);
//        if (TypeChecker.checkIsType(dataSource, BTypes.typeString)) {
        if (type.getTag() == TypeTags.STRING_TAG) {
            if (dataSource instanceof BString) {
                return ((BString) dataSource).getValue();
            }
            return (String) dataSource;
        } else if (type.getTag() == TypeTags.ARRAY_TAG &&
                ((org.ballerinalang.jvm.types.BArrayType) type).getElementType().getTag() == TypeTags.BYTE_TAG) {
            return new String(((ArrayValue) dataSource).getBytes(), StandardCharsets.UTF_8);
        }

        return StringUtils.getJsonString(dataSource);
    }

    /**
     * Check whether a given value should be serialized specifically as a JSON.
     *
     * @param value        Value to serialize
     * @param entity Entity record
     * @return flag indicating whether the given value should be serialized specifically as a JSON
     */
    public static boolean generateAsJSON(Object value, ObjectValue entity) {
        if (value instanceof StreamingJsonValue) {
            /* Streaming JSON should be serialized using the serialize() method. This is because there are two types
            of JSON in Ballerina namely internally built and custom built. The custom built needs to be parsed
            differently than the internally built. StreamingJsonValue being the custom built type it is parsed in the
             serialize method.
            Hence returning false.*/
            return false;
        }

        return parseAsJson(entity) && isJSONCompatible(
                TypeChecker.getType(value));
    }

    private static boolean parseAsJson(ObjectValue entity) {
        Object parseAsJson = entity.getNativeData(MimeConstants.PARSE_AS_JSON);
        // A data source might not exist for multipart and byteChannel. Hence the null check. If PARSE_AS_JSON is
        // true then it indicates that the data source need to be parsed as JSON.
        return parseAsJson != null && (boolean) entity.getNativeData(MimeConstants.PARSE_AS_JSON);
    }

    /**
     * Validate the given Content-Type.
     *
     * @param contentType Content-Type value as a string
     * @return true if the value is valid
     */
    public static boolean isValidateContentType(String contentType) {
        try {
            new MimeType(contentType);
        } catch (MimeTypeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Given a {@link HttpCarbonMessage}, returns the content length extracting from headers.
     *
     * @param httpCarbonMessage Represent the message
     * @return length of the content
     */
    public static long extractContentLength(HttpCarbonMessage httpCarbonMessage) {
        long contentLength = NO_CONTENT_LENGTH_FOUND;
        String lengthStr = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
            if (contentLength == NO_CONTENT_LENGTH_FOUND) {
                //Read one byte to make sure the incoming stream has data
                contentLength = httpCarbonMessage.countMessageLengthTill(ONE_BYTE);
            }
        } catch (NumberFormatException e) {
            throw MimeUtil.createError(INVALID_CONTENT_LENGTH_ERROR, "Invalid content length");
        }
        return contentLength;
    }

    public static void closeOutputStream(OutputStream outputStream) throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
    }
}
