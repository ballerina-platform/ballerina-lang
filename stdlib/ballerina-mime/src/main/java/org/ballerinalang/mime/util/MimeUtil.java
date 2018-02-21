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
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
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
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.ASSIGNMENT;
import static org.ballerinalang.mime.util.Constants.BUILTIN_PACKAGE;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILENAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_PARA_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUFFIX_INDEX;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.Constants.UTF_8;

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
    public static String getContentType(BStruct entity) {
        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
            if (mediaType != null) {
                return mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" + mediaType.getStringField(SUBTYPE_INDEX);
            }
        }
        return null;
    }

//    public static void readAndSetStringPayload(BStruct entityStruct, InputStream inputStream,
//                                               long contentLength) {
//        if (contentLength > Constants.BYTE_LIMIT) {
//            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_TEXT_DATA);
//            populateBallerinaFileStruct(entityStruct, temporaryFilePath);
//        } else {
//            String payload = StringUtils.getStringFromInputStream(inputStream);
//            entityStruct.setStringField(TEXT_DATA_INDEX, payload);
//        }
//    }
//
//    /**
//     * Read the json payload from input stream and set it into request or response's entity struct. If the content
//     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
//     * memory.
//     *
//     * @param entityStruct  Represent 'Entity' struct
//     * @param inputStream   Represent input stream coming from the request/response
//     * @param contentLength Content length of the request
//     */
//    public static void readAndSetJsonPayload(BStruct entityStruct, InputStream inputStream,
//                                             long contentLength) {
//        if (contentLength > Constants.BYTE_LIMIT) {
//            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_JSON_DATA);
//            populateBallerinaFileStruct(entityStruct, temporaryFilePath);
//        } else {
//            BJSON payload = new BJSON(inputStream);
//            entityStruct.setRefField(JSON_DATA_INDEX, payload);
//        }
//    }
//
//    /**
//     * Read the xml payload from input stream and set it into request or response's entity struct. If the content
//     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
//     * memory.
//     *
//     * @param entityStruct  Represent 'Entity' struct
//     * @param inputStream   Represent input stream coming from the request/response
//     * @param contentLength Content length of the request
//     */
//    public static void readAndSetXmlPayload(BStruct entityStruct, InputStream inputStream,
//                                            long contentLength) {
//        if (contentLength > Constants.BYTE_LIMIT) {
//            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_XML_DATA);
//            populateBallerinaFileStruct(entityStruct, temporaryFilePath);
//        } else {
//            BXML payload = XMLUtils.parse(inputStream);
//            entityStruct.setRefField(XML_DATA_INDEX, payload);
//        }
//    }
//
//    /**
//     * Read the binary payload from input stream and set it into request or response's entity struct. If the content
//     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
//     * memory.
//     *
//     * @param entityStruct  Represent 'Entity' struct
//     * @param inputStream   Represent input stream coming from the request/response
//     * @param contentLength Content length of the request
//     */
//    public static void readAndSetBinaryPayload(BStruct entityStruct, InputStream inputStream,
//                                               long contentLength) {
//        if (contentLength > Constants.BYTE_LIMIT) {
//            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_BINARY_DATA);
//            populateBallerinaFileStruct(entityStruct, temporaryFilePath);
//        } else {
//            byte[] payload;
//            try {
//                payload = getByteArray(inputStream);
//            } catch (IOException e) {
//                throw new BallerinaException("Error while converting inputstream to a byte array: " + e.getMessage());
//            }
//            entityStruct.setBlobField(BYTE_DATA_INDEX, payload);
//        }
//    }
//
//    /**
//     * Check whether the 'Entity' body is present in text form.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return a boolean denoting the availability of text payload
//     */
//    public static boolean isTextBodyPresent(BStruct entity) {
//        String textPayload = entity.getStringField(TEXT_DATA_INDEX);
//        if (isNotNullAndEmpty(textPayload)) {
//            return true;
//        } else {
//            return isOverFlowDataNotNull(entity);
//        }
//    }
//
//    /**
//     * Check whether the 'Entity' body is present in json form.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return a boolean denoting the availability of json payload
//     */
//    public static boolean isJsonBodyPresent(BStruct entity) {
//        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
//        if (jsonRefType != null) {
//            BJSON jsonPayload = (BJSON) entity.getRefField(JSON_DATA_INDEX);
//            if (jsonPayload != null) {
//                return true;
//            }
//        } else {
//            return isOverFlowDataNotNull(entity);
//        }
//        return false;
//    }
//
//    /**
//     * Check whether the 'Entity' body is present in xml form.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return a boolean denoting the availability of xml payload
//     */
//    public static boolean isXmlBodyPresent(BStruct entity) {
//        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
//        if (xmlRefType != null) {
//            BXML xmlPayload = (BXML) entity.getRefField(XML_DATA_INDEX);
//            if (xmlPayload != null) {
//                return true;
//            }
//        } else {
//            return isOverFlowDataNotNull(entity);
//        }
//        return false;
//    }
//
//    /**
//     * Check whether the 'Entity' body is present in binary form.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return a boolean denoting the availability of binary payload
//     */
//    public static boolean isBinaryBodyPresent(BStruct entity) {
//        byte[] binaryPayload = entity.getBlobField(BYTE_DATA_INDEX);
//        if (binaryPayload != null) {
//            return true;
//        } else {
//            return isOverFlowDataNotNull(entity);
//        }
//    }
//
//    /**
//     * Check whether the 'Entity' body is present as multi parts.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return a boolean denoting the availability of binary payload
//     */
//    public static boolean isMultipartsAvailable(BStruct entity) {
//        if (entity.getRefField(MULTIPART_DATA_INDEX) != null) {
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Extract the text payload from entity.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return string containing text payload
//     */
//    public static String getTextPayload(BStruct entity) {
//        String returnValue = entity.getStringField(TEXT_DATA_INDEX);
//        if (isNotNullAndEmpty(returnValue)) {
//            return returnValue;
//        } else {
//            String filePath = getFilePathFromFileStruct(entity);
//            try {
//                return filePath.isEmpty() ? null : new String(readFromFile(filePath), UTF_8);
//            } catch (UnsupportedEncodingException e) {
//                log.error("Error occurred while extracting text payload from entity", e.getMessage());
//            }
//        }
//        return null;
//    }

    /**
     * Given a ballerina entity, get the content-type with parameters included.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    static String getContentTypeWithParameters(BStruct entity) {
        String contentType = null;
        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
            if (mediaType != null) {
                contentType = mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" +
                        mediaType.getStringField(SUBTYPE_INDEX);
                if (mediaType.getRefField(PARAMETER_MAP_INDEX) != null) {
                    BMap map = (BMap) mediaType.getRefField(PARAMETER_MAP_INDEX);
                    return HeaderUtil.appendHeaderParams(new StringBuilder(contentType), map);
                }
            }
        }
        return contentType;
    }

//    /**
//     * Extract the json payload from entity.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return json content in BJSON form
//     */
//    public static BJSON getJsonPayload(BStruct entity) {
//        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
//        if (jsonRefType != null) {
//            return (BJSON) entity.getRefField(JSON_DATA_INDEX);
//        } else {
//            String filePath = getFilePathFromFileStruct(entity);
//            try {
//                return filePath.isEmpty() ? null : new BJSON(new String(readFromFile(filePath), UTF_8));
//            } catch (UnsupportedEncodingException e) {
//                log.error("Error occurred while extracting json payload from entity", e.getMessage());
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Extract the xml payload from entity.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return xml content in BXML form
//     */
//    public static BXML getXmlPayload(BStruct entity) {
//        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
//        if (xmlRefType != null) {
//            return (BXML) entity.getRefField(XML_DATA_INDEX);
//        } else {
//            String filePath = getFilePathFromFileStruct(entity);
//            try {
//                return filePath.isEmpty() ? null : XMLUtils.parse(new String(readFromFile(filePath), UTF_8));
//            } catch (UnsupportedEncodingException e) {
//                log.error("Error occurred while extracting xml payload from entity", e.getMessage());
//            }
//        }
//        return null;
//    }

//    /**
//     * Extract the binary payload from entity.
//     *
//     * @param entity Represent 'Entity' struct
//     * @return entity body as a byte array
//     */
//    public static byte[] getBinaryPayload(BStruct entity) {
//        byte[] byteData = entity.getBlobField(BYTE_DATA_INDEX);
//        if (byteData != null) {
//            return entity.getBlobField(BYTE_DATA_INDEX);
//        } else {
//            String filePath = getFilePathFromFileStruct(entity);
//            return filePath.isEmpty() ? null : readFromFile(filePath);
//        }
//    }
//
//    private static String getFilePathFromFileStruct(BStruct entity) {
//        BStruct fileStruct = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
//        return fileStruct.getStringField(FILE_PATH_INDEX);
//    }

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
        String dispositionValue = null;
        if (isNotNullAndEmpty(contentDispositionHeaderWithParams)) {
            if (contentDispositionHeaderWithParams.contains(SEMICOLON)) {
                dispositionValue = HeaderUtil.getHeaderValue(contentDispositionHeaderWithParams);
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
                                    paramValue.toString());
                            break;
                        case CONTENT_DISPOSITION_NAME:
                            contentDisposition.setStringField(CONTENT_DISPOSITION_NAME_INDEX, paramValue.toString());
                            break;
                        default:
                    }
                }
            }
            contentDisposition.setRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX, paramMap);
            bodyPart.setRefField(CONTENT_DISPOSITION_INDEX, contentDisposition);
        }
    }

    /**
     * Given a ballerina entity, get the content-type with parameters included.
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
                    String contentType = getContentType(entity);
                    if (contentType != null && contentType.equals(MULTIPART_FORM_DATA)) {
                        dispositionBuilder.append(MULTIPART_FORM_DATA);
                    }
                } else {
                    dispositionBuilder.append(disposition);
                }
                if (!dispositionBuilder.toString().isEmpty()) {
                    String name = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_NAME_INDEX);
                    String fileName = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_FILENAME_INDEX);
                    if (isNotNullAndEmpty(name)) {
                        appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                                name).append(SEMICOLON);
                    }
                    if (isNotNullAndEmpty(fileName)) {
                        appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                                .append(fileName).append(SEMICOLON);
                    }
                    if (contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX) != null) {
                        BMap map = (BMap) contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX);
                        HeaderUtil.appendHeaderParams(appendSemiColon(dispositionBuilder), map);
                    }
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
//    /**
//     * Populate ballerina file struct with temporary file path.
//     *
//     * @param entityStruct      Represent 'Entity'
//     * @param temporaryFilePath Temporary file path
//     * @return Entity struct populated with file handler
//     */
//    private static BStruct populateBallerinaFileStruct(BStruct entityStruct, String temporaryFilePath) {
//        BStruct fileStruct = (BStruct) entityStruct.getRefField(OVERFLOW_DATA_INDEX);
//        fileStruct.setStringField(TEMP_FILE_PATH_INDEX, temporaryFilePath);
//        return entityStruct;
//    }

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
            byte[] buffer = new byte[1024];
            int bytesRead;
            //read from inputstream to buffer
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
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
     * Given an input stream, get a byte array.
     *
     * @param input Represent an input stream
     * @return a byte array
     * @throws IOException In case an error occurs while reading input stream
     */
    static byte[] getByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int len; (len = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }

//    /**
//     * Handle discrete media type content. This method populates ballerina entity with the relevant payload.
//     *
//     * @param context     Represent ballerina context
//     * @param entity      Represent an 'Entity'
//     * @param inputStream Represent input stream coming from the request/response
//     */
//    public static void handleDiscreteMediaTypeContent(Context context, BStruct entity, InputStream inputStream) {
//        String baseType = getContentType(entity);
//        long contentLength = entity.getIntField(SIZE_INDEX);
//        if (baseType != null) {
//            switch (baseType) {
//                case TEXT_PLAIN:
//                case APPLICATION_FORM:
//                    MimeUtil.readAndSetStringPayload(context, entity, inputStream, contentLength);
//                    break;
//                case APPLICATION_JSON:
//                    MimeUtil.readAndSetJsonPayload(context, entity, inputStream, contentLength);
//                    break;
//                case TEXT_XML:
//                case APPLICATION_XML:
//                    MimeUtil.readAndSetXmlPayload(context, entity, inputStream, contentLength);
//                    break;
//                default:
//                    MimeUtil.readAndSetBinaryPayload(context, entity, inputStream, contentLength);
//                    break;
//            }
//        } else {
//            MimeUtil.readAndSetBinaryPayload(context, entity, inputStream, contentLength);
//        }
//    }
//
//    /**
//     * Handle composite media type content. This method populates a set of body parts as an array of ballerina entities
//     * and set them into the top level entity. Nested parts are not covered yet.
//     *
//     * @param context    Represent ballerina context
//     * @param entity     Represent an 'Entity'
//     * @param multiparts Represent a list of body parts
//     */
//    public static void handleCompositeMediaTypeContent(Context context, BStruct entity, List<HttpBodyPart> multiparts) {
//        ArrayList<BStruct> bodyParts = new ArrayList<>();
//        for (HttpBodyPart bodyPart : multiparts) {
//            BStruct partStruct = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, ENTITY);
//            String baseType = bodyPart.getContentType();
//            partStruct.setIntField(SIZE_INDEX, bodyPart.getSize());
//            BStruct mediaType = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
//            setContentType(mediaType, partStruct, baseType);
//            handleDiscreteMediaTypeContent(context, partStruct, new ByteArrayInputStream(bodyPart.getContent()));
//            bodyParts.add(partStruct);
//        }
//        if (!bodyParts.isEmpty()) {
//            BStructType typeOfBodyPart = bodyParts.get(0).getType();
//            BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
//            BRefValueArray partsArray = new BRefValueArray(result, typeOfBodyPart);
//            entity.setRefField(MULTIPART_DATA_INDEX, partsArray);
//        }
//    }
//
//    /**
//     * Given a ballerina entity, get the content-type as a base type.
//     *
//     * @param entity Represent an 'Entity'
//     * @return content-type in 'primarytype/subtype' format
//     */
//    public static String getContentType(BStruct entity) {
//        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
//            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
//            if (mediaType != null) {
//                return mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" + mediaType.getStringField(SUBTYPE_INDEX);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Check whether the entity body is present.
//     *
//     * @param entity   Represent an 'Entity'
//     * @param baseType Content type that describes the entity body
//     * @return a boolean indicating entity body availability
//     */
//    public static boolean checkEntityBodyAvailability(BStruct entity, String baseType) {
//        switch (baseType) {
//            case TEXT_PLAIN:
//                return MimeUtil.isTextBodyPresent(entity);
//            case APPLICATION_JSON:
//                return MimeUtil.isJsonBodyPresent(entity);
//            case APPLICATION_XML:
//                return MimeUtil.isXmlBodyPresent(entity);
//            case MULTIPART_FORM_DATA:
//                return MimeUtil.isMultipartsAvailable(entity);
//            default:
//                return MimeUtil.isBinaryBodyPresent(entity);
//        }
//    }
//
//    /**
//     * Encode a given body part and add it to multipart request encoder.
//     *
//     * @param nettyEncoder Helps encode multipart/form-data
//     * @param httpRequest  Represent top level http request that should hold multiparts
//     * @param bodyPart     Represent a ballerina body part
//     * @throws HttpPostRequestEncoder.ErrorDataEncoderException when an error occurs while encoding
//     */
//    public static void encodeBodyPart(HttpPostRequestEncoder nettyEncoder, HttpRequest httpRequest,
//                                      BStruct bodyPart) throws HttpPostRequestEncoder.ErrorDataEncoderException {
//        try {
//            InterfaceHttpData encodedData;
//            String baseType = MimeUtil.getContentType(bodyPart);
//            if (baseType != null) {
//                switch (baseType) {
//                    case TEXT_PLAIN:
//                        encodedData = getEncodedTextBodyPart(httpRequest, bodyPart);
//                        break;
//                    case APPLICATION_JSON:
//                        encodedData = getEncodedJsonBodyPart(httpRequest, bodyPart);
//                        break;
//                    case APPLICATION_XML:
//                        encodedData = getEncodedXmlBodyPart(httpRequest, bodyPart);
//                        break;
//                    default:
//                        encodedData = getEncodedBinaryBodyPart(httpRequest, bodyPart);
//                        break;
//                }
//            } else {
//                encodedData = getEncodedBinaryBodyPart(httpRequest, bodyPart);
//            }
//            if (encodedData != null) {
//                nettyEncoder.addBodyHttpData(encodedData);
//            }
//        } catch (IOException e) {
//            LOG.error("Error occurred while encoding body part in ", e.getMessage());
//        }
//    }
//
//    /**
//     * Encode a text body part.
//     *
//     * @param httpRequest Represent the top level http request that should hold the body part
//     * @param bodyPart    Represent a ballerina body part
//     * @return InterfaceHttpData which represent an encoded file upload part
//     * @throws IOException When an error occurs while encoding text body part
//     */
//    private static InterfaceHttpData getEncodedTextBodyPart(HttpRequest httpRequest, BStruct bodyPart) throws
//            IOException {
//        String bodyPartName = getBodyPartName(bodyPart);
//        if (isNotNullAndEmpty(bodyPart.getStringField(TEXT_DATA_INDEX))) {
//            return getAttribute(httpRequest, bodyPartName,
//                    bodyPart.getStringField(TEXT_DATA_INDEX));
//        } else {
//            return readFromFile(httpRequest, bodyPart, bodyPartName, TEXT_PLAIN);
//        }
//    }
//
//    /**
//     * Get an encoded body part from json content.
//     *
//     * @param httpRequest Represent the top level http request that should hold the body part
//     * @param bodyPart    Represent a ballerina body part
//     * @return InterfaceHttpData which represent an encoded file upload part with json content
//     * @throws IOException When an error occurs while encoding json body part
//     */
//    private static InterfaceHttpData getEncodedJsonBodyPart(HttpRequest httpRequest, BStruct bodyPart)
//            throws IOException {
//        String bodyPartName = getBodyPartName(bodyPart);
//        if (bodyPart.getRefField(JSON_DATA_INDEX) != null) {
//            BJSON jsonContent = (BJSON) bodyPart.getRefField(JSON_DATA_INDEX);
//            return readFromMemory(httpRequest, bodyPart, bodyPartName, APPLICATION_JSON, JSON_EXTENSION,
//                    jsonContent.getMessageAsString());
//        } else {
//            return readFromFile(httpRequest, bodyPart, bodyPartName, APPLICATION_JSON);
//        }
//    }
//
//    /**
//     * Get an encoded body part from xml content.
//     *
//     * @param httpRequest Represent the top level http request that should hold the body part
//     * @param bodyPart    Represent a ballerina body part
//     * @return InterfaceHttpData which represent an encoded file upload part with xml content
//     * @throws IOException When an error occurs while encoding xml body part
//     */
//    private static InterfaceHttpData getEncodedXmlBodyPart(HttpRequest httpRequest, BStruct bodyPart)
//            throws IOException {
//        String bodyPartName = getBodyPartName(bodyPart);
//        if (bodyPart.getRefField(XML_DATA_INDEX) != null) {
//            BXML xmlPayload = (BXML) bodyPart.getRefField(XML_DATA_INDEX);
//            return readFromMemory(httpRequest, bodyPart, bodyPartName, APPLICATION_XML, XML_EXTENSION,
//                    xmlPayload.getMessageAsString());
//        } else {
//            return readFromFile(httpRequest, bodyPart, bodyPartName, getContentType(bodyPart));
//        }
//    }
//
//    /**
//     * Get an encoded body part from binary content.
//     *
//     * @param httpRequest Represent the top level http request that should hold the body part
//     * @param bodyPart    Represent a ballerina body part
//     * @return InterfaceHttpData which represent an encoded file upload part with xml content
//     * @throws IOException When an error occurs while encoding binary body part
//     */
//    private static InterfaceHttpData getEncodedBinaryBodyPart(HttpRequest httpRequest, BStruct bodyPart)
//            throws IOException {
//        String bodyPartName = getBodyPartName(bodyPart);
//        byte[] binaryPayload = bodyPart.getBlobField(BYTE_DATA_INDEX);
//        if (binaryPayload != null) {
//            InputStream inputStream = new ByteArrayInputStream(binaryPayload);
//            FileUploadContentHolder contentHolder = new FileUploadContentHolder();
//            contentHolder.setRequest(httpRequest);
//            contentHolder.setBodyPartName(bodyPartName);
//            contentHolder.setFileName(TEMP_FILE_NAME + TEMP_FILE_EXTENSION);
//            contentHolder.setContentType(OCTET_STREAM);
//            contentHolder.setFileSize(binaryPayload.length);
//            contentHolder.setContentStream(inputStream);
//            contentHolder.setBodyPartFormat(Constants.BodyPartForm.INPUTSTREAM);
//            String contentTransferHeaderValue = getHeaderValue(bodyPart, CONTENT_TRANSFER_ENCODING);
//            if (contentTransferHeaderValue != null) {
//                contentHolder.setContentTransferEncoding(contentTransferHeaderValue);
//            }
//            return getFileUpload(contentHolder);
//        } else {
//            return readFromFile(httpRequest, bodyPart, bodyPartName, getContentType(bodyPart));
//        }
//    }
//
//    /**
//     * Get the header value for a given header name from a body part.
//     *
//     * @param bodyPart   Represent a ballerina body part.
//     * @param headerName Represent an http header name
//     * @return a header value for the given header name
//     */
//    private static String getHeaderValue(BStruct bodyPart, String headerName) {
//        BMap<String, BValue> headerMap = bodyPart.getRefField(ENTITY_HEADERS_INDEX) != null ?
//                (BMap<String, BValue>) bodyPart.getRefField(ENTITY_HEADERS_INDEX) : null;
//        if (headerMap != null) {
//            BStringArray headerValue = (BStringArray) headerMap.get(headerName);
//            return headerValue.get(0);
//        }
//        return null;
//    }
//
//    /**
//     * Create an encoded body part from a given string.
//     *
//     * @param request         Represent the top level http request that should hold the body part
//     * @param bodyPartName    Represent body part's name
//     * @param bodyPartContent Actual content that needs to be encoded
//     * @return InterfaceHttpData which represent an encoded attribute
//     * @throws IOException When an error occurs while encoding a text string
//     */
//    private static InterfaceHttpData getAttribute(HttpRequest request, String bodyPartName, String bodyPartContent)
//            throws IOException {
//        return dataFactory.createAttribute(request, bodyPartName, bodyPartContent);
//    }
//
//    /**
//     * Get a body part as a file upload.
//     *
//     * @param httpRequest  Represent the top level http request that should hold the body part
//     * @param bodyPart     Represent a ballerina body part
//     * @param bodyPartName Represent body part's name
//     * @param contentType  Content-Type of the body part
//     * @return InterfaceHttpData which represent an encoded file upload part
//     * @throws IOException When an error occurs while creating a file upload
//     */
//    private static InterfaceHttpData readFromFile(HttpRequest httpRequest, BStruct bodyPart, String bodyPartName,
//                                                  String contentType) throws IOException {
//        BStruct fileHandler = (BStruct) bodyPart.getRefField(OVERFLOW_DATA_INDEX);
//        if (fileHandler != null) {
//            String filePath = fileHandler.getStringField(FILE_PATH_INDEX);
//            if (filePath != null) {
//                Path path = Paths.get(filePath);
//                Path fileName = path != null ? path.getFileName() : null;
//                if (fileName != null) {
//                    InputStream inputStream = Files.newInputStream(path);
//                    long size = (long) Files.getAttribute(path, FILE_SIZE);
//                    FileUploadContentHolder contentHolder = new FileUploadContentHolder();
//                    contentHolder.setRequest(httpRequest);
//                    contentHolder.setBodyPartName(bodyPartName);
//                    contentHolder.setFileName(fileName.toString());
//                    contentHolder.setContentType(contentType);
//                    contentHolder.setFileSize(size);
//                    contentHolder.setContentStream(inputStream);
//                    contentHolder.setBodyPartFormat(Constants.BodyPartForm.INPUTSTREAM);
//                    String contentTransferHeaderValue = getHeaderValue(bodyPart, CONTENT_TRANSFER_ENCODING);
//                    if (contentTransferHeaderValue != null) {
//                        contentHolder.setContentTransferEncoding(contentTransferHeaderValue);
//                    }
//                    return getFileUpload(contentHolder);
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Create an encoded body part from the data in memory.
//     *
//     * @param request       Represent the top level http request that should hold the body part
//     * @param bodyPart      Represent a ballerina body part
//     * @param bodyPartName  Represent body part's name
//     * @param contentType   Content-Type of the data in memory
//     * @param fileExtension File extension to be used when writing data in the memory to temp file
//     * @param actualContent Actual content in the memory
//     * @return InterfaceHttpData which represent an encoded file upload part for the given
//     * @throws IOException When an error occurs while creating a file upload from data read from memory
//     */
//    private static InterfaceHttpData readFromMemory(HttpRequest request, BStruct bodyPart, String bodyPartName,
//                                                    String contentType, String fileExtension,
//                                                    String actualContent)
//            throws IOException {
//        File file = File.createTempFile(TEMP_FILE_NAME, fileExtension);
//        file.deleteOnExit();
//        writeToTempFile(file, actualContent);
//        FileUploadContentHolder contentHolder = new FileUploadContentHolder();
//        contentHolder.setRequest(request);
//        contentHolder.setBodyPartName(bodyPartName);
//        contentHolder.setFileName(file.getName());
//        contentHolder.setContentType(contentType);
//        contentHolder.setFileSize(file.length());
//        contentHolder.setFile(file);
//        contentHolder.setBodyPartFormat(Constants.BodyPartForm.FILE);
//        String contentTransferHeaderValue = getHeaderValue(bodyPart, CONTENT_TRANSFER_ENCODING);
//        if (contentTransferHeaderValue != null) {
//            contentHolder.setContentTransferEncoding(contentTransferHeaderValue);
//        }
//        return getFileUpload(contentHolder);
//    }
//
//    /**
//     * Get a body part as a file upload.
//     *
//     * @param contentHolder Holds attributes required for creating a body part
//     * @return InterfaceHttpData which represent an encoded file upload part for the given
//     * @throws IOException In case an error occurs while creating file part
//     */
//    private static InterfaceHttpData getFileUpload(FileUploadContentHolder contentHolder)
//            throws IOException {
//        FileUpload fileUpload = dataFactory.createFileUpload(contentHolder.getRequest(), contentHolder.getBodyPartName()
//                , contentHolder.getFileName(), contentHolder.getContentType(),
//                contentHolder.getContentTransferEncoding(), contentHolder.getCharset(), contentHolder.getFileSize());
//        switch (contentHolder.getBodyPartFormat()) {
//            case INPUTSTREAM:
//                fileUpload.setContent(contentHolder.getContentStream());
//                break;
//            case FILE:
//                fileUpload.setContent(contentHolder.getFile());
//                break;
//        }
//        return fileUpload;
//    }
//
//    /**
//     * Get the body part name and if the user hasn't set a name set a random string as the part name.
//     *
//     * @param bodyPart Represent a ballerina body part
//     * @return a string denoting the body part's name
//     */
//    private static String getBodyPartName(BStruct bodyPart) {
//        String bodyPartName = bodyPart.getStringField(ENTITY_NAME_INDEX);
//        if (bodyPartName == null || bodyPartName.isEmpty()) {
//            bodyPartName = UUID.randomUUID().toString();
//        }
//        return bodyPartName;
//    }
//
//    /**
//     * Write content to temp file through a file writer.
//     *
//     * @param file            Represent the file that the content needs to be written to
//     * @param messageAsString Actual content that needs to be written
//     * @throws IOException In case an exception occurs when writing to temp file
//     */
//    private static void writeToTempFile(File file, String messageAsString) throws IOException {
//        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
//             BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
//            bufferedWriter.write(messageAsString);
//            bufferedWriter.close();
//        }
//    }
//
//    /**
//     * Set the data factory that needs to be used for encoding body parts.
//     *
//     * @param dataFactory which enables creation of InterfaceHttpData objects
//     */
//    public static void setDataFactory(HttpDataFactory dataFactory) {
//        MimeUtil.dataFactory = dataFactory;
//    }

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
     * Get a new multipart boundary delimiter.
     *
     * @return a boundary string
     */
    public static String getNewMultipartDelimiter() {
        return Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
    }

    /**
     * Wrap the byte channel associated with the given entity as the EntityBody.
     *
     * @param entityStruct Represent an entity struct
     * @return EntityBody which wraps the underline byte channel
     */
    public static EntityBody constructEntityBody(BStruct entityStruct) {
        return EntityBodyHandler.getEntityBody(entityStruct.getNativeData(ENTITY_BYTE_CHANNEL));
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
