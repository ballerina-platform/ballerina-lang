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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStreamingJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
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
import static org.ballerinalang.mime.util.MimeConstants.JSON_SUFFIX;
import static org.ballerinalang.mime.util.MimeConstants.JSON_TYPE_IDENTIFIER;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MIME_ERROR_CODE;
import static org.ballerinalang.mime.util.MimeConstants.MIME_ERROR_MESSAGE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
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

    /**
     * Given a ballerina entity, get the content-type as a base type.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype' format
     */
    public static String getBaseType(BMap<String, BValue> entity) {
        if (entity.get(MEDIA_TYPE_FIELD) != null) {
            BMap<String, BValue> mediaType = (BMap<String, BValue>) entity.get(MEDIA_TYPE_FIELD);
            if (mediaType != null) {
                return mediaType.get(PRIMARY_TYPE_FIELD).stringValue() + "/" +
                        mediaType.get(SUBTYPE_FIELD).stringValue();
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
    public static String getContentTypeWithParameters(BMap<String, BValue> entity) {
        if (entity.get(MEDIA_TYPE_FIELD) == null) {
            return HeaderUtil.getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
        }
        BMap<String, BValue> mediaType = (BMap<String, BValue>) entity.get(MEDIA_TYPE_FIELD);
        String primaryType = mediaType.get(PRIMARY_TYPE_FIELD).stringValue();
        String subType = mediaType.get(SUBTYPE_FIELD).stringValue();
        String contentType = null;
        if ((primaryType != null && !primaryType.isEmpty()) && (subType != null && !subType.isEmpty())) {
            contentType = primaryType + "/" + subType;
            if (mediaType.get(PARAMETER_MAP_FIELD) != null) {
                BMap map = mediaType.get(PARAMETER_MAP_FIELD) != null ?
                        (BMap) mediaType.get(PARAMETER_MAP_FIELD) : null;
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
    public static String getContentTypeParamValue(String contentType, String parameterName) {
        try {
            MimeType mimeType = new MimeType(contentType);
            MimeTypeParameterList parameterList = mimeType.getParameters();
            return parameterList.get(parameterName);
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
    }

    /**
     * Construct 'MediaType' struct with the given Content-Type and set it into the given 'Entity'.
     *
     * @param mediaType    Represent 'MediaType' struct
     * @param entityStruct Represent 'Entity' struct
     * @param contentType  Content-Type value in string
     */
    public static void setContentType(BMap<String, BValue> mediaType, BMap<String, BValue> entityStruct,
                                      String contentType) {
        BMap<String, BValue> mimeType = parseMediaType(mediaType, contentType);
        if (contentType == null) {
            mimeType.put(PRIMARY_TYPE_FIELD, new BString(DEFAULT_PRIMARY_TYPE));
            mimeType.put(SUBTYPE_FIELD, new BString(DEFAULT_SUB_TYPE));
        }
        entityStruct.put(MEDIA_TYPE_FIELD, mimeType);
    }

    /**
     * Parse 'MediaType' struct with the given Content-Type.
     *
     * @param mediaType   Represent 'MediaType' struct
     * @param contentType Content-Type value in string
     * @return 'MediaType' struct populated with values
     */
    public static BMap<String, BValue> parseMediaType(BMap<String, BValue> mediaType, String contentType) {
        try {
            BMap<String, BValue> parameterMap = new BMap<>();
            BString suffix, primaryType, subType;

            if (contentType != null) {
                MimeType mimeType = new MimeType(contentType);
                primaryType = new BString(mimeType.getPrimaryType());

                String subTypeStr = mimeType.getSubType();
                subType = new BString(subTypeStr);
                if (subTypeStr != null && subTypeStr.contains(SUFFIX_ATTACHMENT)) {
                    suffix = new BString(
                            subTypeStr.substring(subTypeStr.lastIndexOf(SUFFIX_ATTACHMENT) + 1));
                } else {
                    suffix = BTypes.typeString.getZeroValue();
                }

                MimeTypeParameterList parameterList = mimeType.getParameters();
                Enumeration keys = parameterList.getNames();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = parameterList.get(key);
                    parameterMap.put(key, new BString(value));
                }
            } else {
                primaryType = suffix = subType = BTypes.typeString.getZeroValue();
            }

            mediaType.put(PRIMARY_TYPE_FIELD, primaryType);
            mediaType.put(SUBTYPE_FIELD, subType);
            mediaType.put(SUFFIX_FIELD, suffix);
            mediaType.put(PARAMETER_MAP_FIELD, parameterMap);
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
        return mediaType;
    }

    public static void setMediaTypeToEntity(Context context, BMap<String, BValue> entityStruct, String contentType) {
        BMap<String, BValue> mediaType = BLangConnectorSPIUtil.createObject(context, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
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
    public static void setContentDisposition(BMap<String, BValue> contentDisposition, BMap<String, BValue> bodyPart,
                                             String contentDispositionHeaderWithParams) {
        populateContentDispositionObject(contentDisposition, contentDispositionHeaderWithParams);
        bodyPart.put(CONTENT_DISPOSITION_FIELD, contentDisposition);
    }

    public static void populateContentDispositionObject(BMap<String, BValue> contentDisposition,
                                                        String contentDispositionHeaderWithParams) {
        String dispositionValue;
        if (isNotNullAndEmpty(contentDispositionHeaderWithParams)) {
            if (contentDispositionHeaderWithParams.contains(SEMICOLON)) {
                dispositionValue = HeaderUtil.getHeaderValue(contentDispositionHeaderWithParams);
            } else {
                dispositionValue = contentDispositionHeaderWithParams;
            }
            contentDisposition.put(DISPOSITION_FIELD, new BString(dispositionValue));
            BMap<String, BValue> paramMap = HeaderUtil.getParamMap(contentDispositionHeaderWithParams);
            if (paramMap != null) {
                for (String key : paramMap.keys()) {
                    BString paramValue = (BString) paramMap.get(key);
                    switch (key) {
                        case CONTENT_DISPOSITION_FILE_NAME:
                            contentDisposition.put(CONTENT_DISPOSITION_FILENAME_FIELD,
                                    new BString(stripQuotes(paramValue.toString())));
                            break;
                        case CONTENT_DISPOSITION_NAME:
                            contentDisposition.put(CONTENT_DISPOSITION_NAME_FIELD,
                                    new BString(stripQuotes(paramValue.toString())));
                            break;
                        default:
                    }
                }
                paramMap.remove(CONTENT_DISPOSITION_FILE_NAME);
                paramMap.remove(CONTENT_DISPOSITION_NAME);
            }
            contentDisposition.put(CONTENT_DISPOSITION_PARA_MAP_FIELD, paramMap);
        }
    }

    /**
     * Given a ballerina entity, build the content-disposition header value from 'ContentDisposition' object.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    public static String getContentDisposition(BMap<String, BValue> entity) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (entity.get(CONTENT_DISPOSITION_FIELD) != null) {
            BMap<String, BValue> contentDispositionStruct =
                    (BMap<String, BValue>) entity.get(CONTENT_DISPOSITION_FIELD);
            if (contentDispositionStruct != null) {
                BValue disposition = contentDispositionStruct.get(DISPOSITION_FIELD);
                if (disposition == null || disposition.stringValue().isEmpty()) {
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
                                                                 BMap<String, BValue> contentDispositionStruct) {
        BValue nameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_NAME_FIELD);
        String name = nameBVal != null ? nameBVal.stringValue() : null;

        BValue fileNameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_FILENAME_FIELD);
        String fileName = fileNameBVal != null ? fileNameBVal.stringValue() : null;

        if (isNotNullAndEmpty(name)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                    includeQuotes(name)).append(SEMICOLON);
        }
        if (isNotNullAndEmpty(fileName)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                    .append(includeQuotes(fileName)).append(SEMICOLON);
        }
        if (contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD) != null) {
            BMap map = (BMap) contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD);
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
    public static void setContentLength(BMap<String, BValue> entityStruct, long length) {
        entityStruct.put(SIZE_FIELD, new BInteger(length));
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
    private static String stripQuotes(String textValue) {
        if (textValue.startsWith(DOUBLE_QUOTE)) {
            textValue = textValue.substring(1);
        }
        if (textValue.endsWith(DOUBLE_QUOTE)) {
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
    static boolean isNestedPartsAvailable(BMap<String, BValue> bodyPart) {
        String contentTypeOfChildPart = MimeUtil.getBaseType(bodyPart);
        return contentTypeOfChildPart != null && contentTypeOfChildPart.startsWith(MULTIPART_AS_PRIMARY_TYPE) &&
                bodyPart.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Create mime specific error record with '{ballerina/mime}MIMEError' as error code.
     *
     * @param context Represent ballerina context
     * @param errMsg  Actual error message
     * @return Ballerina error record
     */
    public static BError createError(Context context, String errMsg) {
        return createError(context, MIME_ERROR_CODE, errMsg);
    }

    /**
     * Create mime specific error record.
     *
     * @param context Represent ballerina context
     * @param reason  Error code in string form
     * @param errMsg  Actual error message
     * @return Ballerina error record
     */
    public static BError createError(Context context, String reason, String errMsg) {
        BMap<String, BValue> mimeErrorRecord = createMimeErrorRecord(context);
        mimeErrorRecord.put(MIME_ERROR_MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, reason, mimeErrorRecord);
    }

    private static BMap<String, BValue> createMimeErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, MimeConstants.PROTOCOL_PACKAGE_MIME,
                MimeConstants.MIME_ERROR_RECORD);
    }

    public static boolean isJSONContentType(BMap<String, BValue> entityStruct) {
        String baseType;
        try {
            baseType = HeaderUtil.getBaseType(entityStruct);
            if (baseType == null) {
                return false;
            }
            return baseType.toLowerCase(Locale.getDefault()).endsWith(JSON_TYPE_IDENTIFIER) ||
                    baseType.toLowerCase(Locale.getDefault()).endsWith(JSON_SUFFIX);
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
    }

    public static boolean isJSONCompatible(BType type) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.JSON_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                return isJSONCompatible(((BArrayType) type).getElementType());
            case TypeTags.MAP_TAG:
                return isJSONCompatible(((BMapType) type).getConstrainedType());
            default:
                return false;
        }
    }

    public static BString getMessageAsString(BValue dataSource) {
        BType type = dataSource.getType();
        if (type.getTag() == TypeTags.STRING_TAG) {
            return (BString) dataSource;
        } else if (type.getTag() == TypeTags.ARRAY_TAG &&
                ((BArrayType) type).getElementType().getTag() == TypeTags.BYTE_TAG) {
            return new BString(new String(((BByteArray) dataSource).getBytes(), StandardCharsets.UTF_8));
        }

        return new BString(dataSource.stringValue());
    }

    /**
     * Check whether a given value should be serialized specifically as a JSON.
     *
     * @param value        Value to serialize
     * @param entityRecord Entity record
     * @return flag indicating whether the given value should be serialized specifically as a JSON
     */
    public static boolean generateAsJSON(BValue value, BMap<String, BValue> entityRecord) {
        if (value instanceof BStreamingJSON) {
            // Streaming JSON should be serialized using the serialize() method.
            // Hence returning false.
            return false;
        }

        return isJSONContentType(entityRecord) && isJSONCompatible(value.getType());
    }

    public static String validateContentType(String contentType) throws MimeTypeParseException {
        MimeType mimeType = new MimeType(contentType);
        return mimeType.getBaseType();
    }
}
