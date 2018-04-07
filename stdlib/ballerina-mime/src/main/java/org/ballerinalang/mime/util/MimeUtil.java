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


import io.netty.util.internal.PlatformDependent;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.ASSIGNMENT;
import static org.ballerinalang.mime.util.Constants.BODY_PARTS;
import static org.ballerinalang.mime.util.Constants.BUILTIN_PACKAGE;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILENAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_PARA_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.DOUBLE_QUOTE;
import static org.ballerinalang.mime.util.Constants.ENTITY_ERROR;
import static org.ballerinalang.mime.util.Constants.FORM_DATA_PARAM;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.READABLE_BUFFER_SIZE;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUFFIX_INDEX;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;

/**
 * Mime utility functions are included in here.
 *
 * @since 0.96
 */
public class MimeUtil {
    private static final Logger log = LoggerFactory.getLogger(MimeUtil.class);

    /**
     * Given a ballerina entity, get the content-type as a base type.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype' format
     */
    public static String getBaseType(BStruct entity) {
        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
            if (mediaType != null) {
                return mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" + mediaType.getStringField(SUBTYPE_INDEX);
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
    public static String getContentTypeWithParameters(BStruct entity) {
        String contentType = null;
        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
            if (mediaType != null) {
                contentType = mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" +
                        mediaType.getStringField(SUBTYPE_INDEX);
                if (mediaType.getRefField(PARAMETER_MAP_INDEX) != null) {
                    BMap map = mediaType.getRefField(PARAMETER_MAP_INDEX) != null ?
                            (BMap) mediaType.getRefField(PARAMETER_MAP_INDEX) : null;
                    if (map != null && !map.isEmpty()) {
                        contentType = contentType + SEMICOLON;
                        return HeaderUtil.appendHeaderParams(new StringBuilder(contentType), map);
                    }
                }
            }
        }
        return contentType;
    }

    /**
     * Construct 'MediaType' struct with the given Content-Type and set it into the given 'Entity'.
     *
     * @param mediaType    Represent 'MediaType' struct
     * @param entityStruct Represent 'Entity' struct
     * @param contentType  Content-Type value in string
     */
    public static void setContentType(BStruct mediaType, BStruct entityStruct, String contentType) {
        BStruct mimeType = parseMediaType(mediaType, contentType);
        if (contentType == null) {
            mimeType.setStringField(PRIMARY_TYPE_INDEX, Constants.DEFAULT_PRIMARY_TYPE);
            mimeType.setStringField(SUBTYPE_INDEX, Constants.DEFAULT_SUB_TYPE);
        }
        entityStruct.setRefField(MEDIA_TYPE_INDEX, mimeType);
    }

    /**
     * Parse 'MediaType' struct with the given Content-Type.
     *
     * @param mediaType   Represent 'MediaType' struct
     * @param contentType Content-Type value in string
     * @return 'MediaType' struct populated with values
     */
    public static BStruct parseMediaType(BStruct mediaType, String contentType) {
        try {
            if (contentType != null) {
                MimeType mimeType = new MimeType(contentType);
                mediaType.setStringField(PRIMARY_TYPE_INDEX, mimeType.getPrimaryType());
                mediaType.setStringField(SUBTYPE_INDEX, mimeType.getSubType());
                if (mimeType.getSubType() != null && mimeType.getSubType().contains(Constants.SUFFIX_ATTACHMENT)) {
                    mediaType.setStringField(SUFFIX_INDEX, mimeType.getSubType()
                            .substring(mimeType.getSubType().lastIndexOf(Constants.SUFFIX_ATTACHMENT) + 1));
                }
                MimeTypeParameterList parameterList = mimeType.getParameters();
                Enumeration keys = parameterList.getNames();
                BMap<String, BValue> parameterMap = new BMap<>();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = parameterList.get(key);
                    parameterMap.put(key, new BString(value));
                }
                mediaType.setRefField(PARAMETER_MAP_INDEX, parameterMap);
            }
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
        return mediaType;
    }

    /**
     * Populate ContentDisposition struct and set it to body part.
     *
     * @param contentDisposition                 Represent the ContentDisposition struct that needs to be filled
     * @param bodyPart                           Represent a body part
     * @param contentDispositionHeaderWithParams Represent Content-Disposition header value with parameters
     */
    public static void setContentDisposition(BStruct contentDisposition, BStruct bodyPart,
                                             String contentDispositionHeaderWithParams) {
        String dispositionValue;
        if (isNotNullAndEmpty(contentDispositionHeaderWithParams)) {
            if (contentDispositionHeaderWithParams.contains(SEMICOLON)) {
                dispositionValue = HeaderUtil.getHeaderValue(contentDispositionHeaderWithParams);
            } else {
                dispositionValue = contentDispositionHeaderWithParams;
            }
            contentDisposition.setStringField(DISPOSITION_INDEX, dispositionValue);
            BMap<String, BValue> paramMap = HeaderUtil.getParamMap(contentDispositionHeaderWithParams);
            if (paramMap != null) {
                Set<String> keys = paramMap.keySet();
                for (String key : keys) {
                    BString paramValue = (BString) paramMap.get(key);
                    switch (key) {
                        case CONTENT_DISPOSITION_FILE_NAME:
                            contentDisposition.setStringField(CONTENT_DISPOSITION_FILENAME_INDEX,
                                    stripQuotes(paramValue.toString()));
                            break;
                        case CONTENT_DISPOSITION_NAME:
                            contentDisposition.setStringField(CONTENT_DISPOSITION_NAME_INDEX,
                                    stripQuotes(paramValue.toString()));
                            break;
                        default:
                    }
                }
                paramMap.remove(CONTENT_DISPOSITION_FILE_NAME);
                paramMap.remove(CONTENT_DISPOSITION_NAME);
            }
            contentDisposition.setRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX, paramMap);
            bodyPart.setRefField(CONTENT_DISPOSITION_INDEX, contentDisposition);
        }
    }

    /**
     * Given a ballerina entity, build the content-disposition header value from 'ContentDisposition' object.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    public static String getContentDisposition(BStruct entity) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (entity.getRefField(CONTENT_DISPOSITION_INDEX) != null) {
            BStruct contentDispositionStruct = (BStruct) entity.getRefField(CONTENT_DISPOSITION_INDEX);
            if (contentDispositionStruct != null) {
                String disposition = contentDispositionStruct.getStringField(DISPOSITION_INDEX);
                if (disposition == null || disposition.isEmpty()) {
                    String contentType = getBaseType(entity);
                    if (contentType != null && contentType.equals(MULTIPART_FORM_DATA)) {
                        dispositionBuilder.append(FORM_DATA_PARAM);
                    }
                } else {
                    dispositionBuilder.append(disposition);
                }
                if (!dispositionBuilder.toString().isEmpty()) {
                    String name = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_NAME_INDEX);
                    String fileName = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_FILENAME_INDEX);
                    if (isNotNullAndEmpty(name)) {
                        appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                                includeQuotes(name)).append(SEMICOLON);
                    }
                    if (isNotNullAndEmpty(fileName)) {
                        appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                                .append(includeQuotes(fileName)).append(SEMICOLON);
                    }
                    if (contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX) != null) {
                        BMap map = (BMap) contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX);
                        HeaderUtil.appendHeaderParams(appendSemiColon(dispositionBuilder), map);
                    }
                }
                if (dispositionBuilder.toString().endsWith(SEMICOLON)) {
                    dispositionBuilder.setLength(dispositionBuilder.length() - 1);
                }
            }
        }
        return dispositionBuilder.toString();
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
    public static void setContentLength(BStruct entityStruct, int length) {
        entityStruct.setIntField(SIZE_INDEX, length);
    }

    public static BStruct extractEntity(BStruct httpMessageStruct) {
        Object isEntityBodyAvailable = httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
        if (isEntityBodyAvailable == null || !((Boolean) isEntityBodyAvailable)) {
            return null;
        }
        return (BStruct) httpMessageStruct.getNativeData(MESSAGE_ENTITY);
    }

    /**
     * Given an input stream, create a temporary file and write the content to it.
     *
     * @param inputStream Input stream coming from the request/response.
     * @param fileName    Temporary file name
     * @return Absolute path of the created temporary file.
     */
    static String writeToTemporaryFile(InputStream inputStream, String fileName) {
        OutputStream outputStream = null;
        try {
            File tempFile = File.createTempFile(fileName, TEMP_FILE_EXTENSION);
            outputStream = new FileOutputStream(tempFile.getAbsolutePath());
            writeInputToOutputStream(inputStream, outputStream);
            inputStream.close();
            //flush OutputStream to write any buffered data to file
            outputStream.flush();
            outputStream.close();
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new BallerinaException("Error while writing the payload info into a temp file: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("Error occured while closing outputstream in writeToTemporaryFile", e.getMessage());
            }
        }
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
    static byte[] getByteArray(InputStream input) throws IOException {
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
    private static String includeQuotes(String textValue) {
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
    static boolean isNestedPartsAvailable(BStruct bodyPart) {
        String contentTypeOfChildPart = MimeUtil.getBaseType(bodyPart);
        return contentTypeOfChildPart != null && contentTypeOfChildPart.startsWith(MULTIPART_AS_PRIMARY_TYPE) &&
                bodyPart.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Get entity error as a ballerina struct.
     *
     * @param context Represent ballerina context
     * @param msg     Error message in string form
     * @return Ballerina struct with entity error
     */
    public static BStruct createEntityError(Context context, String msg) {
        PackageInfo filePkg = context.getProgramFile().getPackageInfo(PROTOCOL_PACKAGE_MIME);
        StructInfo entityErrInfo = filePkg.getStructInfo(ENTITY_ERROR);
        return BLangVMStructs.createBStruct(entityErrInfo, msg);
    }

    /**
     * Get parser error as a ballerina struct.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error message in string form
     * @return Ballerina struct with parse error
     */
    public static BStruct getParserError(Context context, String errMsg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(BUILTIN_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);

        BStruct parserError = new BStruct(errorStructInfo.getType());
        parserError.setStringField(0, errMsg);
        return parserError;
    }
}
