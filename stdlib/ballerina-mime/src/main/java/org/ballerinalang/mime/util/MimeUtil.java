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

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.internal.PlatformDependent;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpBodyPart;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BALLERINA_BODY_PART_CONTENT;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING;
import static org.ballerinalang.mime.util.Constants.ENTITY;
import static org.ballerinalang.mime.util.Constants.FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE_SIZE;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_EXTENSION;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUFFIX_INDEX;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.TEXT_XML;
import static org.ballerinalang.mime.util.Constants.UTF_8;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.XML_EXTENSION;

/**
 * Entity related operations and mime utility functions are included here.
 *
 * @since 0.96
 */
public class MimeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MimeUtil.class);

    /**
     * Check whether the 'Entity' body is present as multi parts.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of binary payload
     */
    public static boolean isMultipartsAvailable(BStruct entity) {
        if (entity.getRefField(MULTIPART_DATA_INDEX) != null) {
            return true;
        }
        return false;
    }

    /**
     * Extract the text payload from entity.
     *
     * @param entity Represent 'Entity' struct
     * @return string containing text payload
     */
    public static String getTextPayload(BStruct entity) {
        String returnValue = entity.getStringField(TEXT_DATA_INDEX);
        if (isNotNullAndEmpty(returnValue)) {
            return returnValue;
        } else {
            String filePath = getOverFlowFileLocation(entity);
            try {
                return new String(readFromFile(filePath), UTF_8);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting text payload from entity", e.getMessage());
            }
        }
        return null;
    }

    public static String getTextPayloadFromMemory(BStruct entity) {
        String returnValue = entity.getStringField(TEXT_DATA_INDEX);
        if (isNotNullAndEmpty(returnValue)) {
            return returnValue;
        }
        return null;
    }

    public static String getOverFlowFileLocation(BStruct entity) {
        BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
        return fileHandler.getStringField(FILE_PATH_INDEX);
    }

    /**
     * Extract the json payload from entity.
     *
     * @param entity Represent 'Entity' struct
     * @return json content in BJSON form
     */
    public static BJSON getJsonPayload(BStruct entity) {
        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
        if (jsonRefType != null) {
            return (BJSON) entity.getRefField(JSON_DATA_INDEX);
        } else {
            String filePath = getOverFlowFileLocation(entity);
            try {
                return new BJSON(new String(readFromFile(filePath), UTF_8));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting json payload from entity", e.getMessage());
            }
        }
        return null;
    }

    public static BJSON getJsonPayloadFromMemory(BStruct entity) {
        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
        if (jsonRefType != null) {
            return (BJSON) entity.getRefField(JSON_DATA_INDEX);
        }
        return null;
    }

    /**
     * Extract the xml payload from entity.
     *
     * @param entity Represent 'Entity' struct
     * @return xml content in BXML form
     */
    public static BXML getXmlPayload(BStruct entity) {
        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
        if (xmlRefType != null) {
            return (BXML) entity.getRefField(XML_DATA_INDEX);
        } else {
            String filePath = getOverFlowFileLocation(entity);
            try {
                return XMLUtils.parse(new String(readFromFile(filePath), UTF_8));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting xml payload from entity", e.getMessage());
            }
        }
        return null;
    }

    public static BXML getXmlPayloadFromMemory(BStruct entity) {
        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
        if (xmlRefType != null) {
            return (BXML) entity.getRefField(XML_DATA_INDEX);
        }
        return null;
    }

    /**
     * Extract the binary payload from entity.
     *
     * @param entity Represent 'Entity' struct
     * @return entity body as a byte array
     */
    public static byte[] getBinaryPayload(BStruct entity) {
        byte[] byteData = entity.getBlobField(BYTE_DATA_INDEX);
        if (byteData != null) {
            return entity.getBlobField(BYTE_DATA_INDEX);
        } else {
            String filePath = getOverFlowFileLocation(entity);
            return readFromFile(filePath);
        }
    }

    public static byte[] getBinaryPayloadFromMemory(BStruct entity) {
        byte[] byteData = entity.getBlobField(BYTE_DATA_INDEX);
        if (byteData != null) {
            return entity.getBlobField(BYTE_DATA_INDEX);
        }
        return null;
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
     * Create a ballerina file struct and set it into the given 'Entity'.
     *
     * @param context           Represent ballerina context
     * @param entityStruct      Represent 'Entity'
     * @param temporaryFilePath Temporary file path
     * @return Entity struct populated with file handler
     */
    static BStruct createBallerinaFileHandler(Context context, BStruct entityStruct, String temporaryFilePath) {
        BStruct fileStruct = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_FILE, Constants.FILE);
        fileStruct.setStringField(TEMP_FILE_PATH_INDEX, temporaryFilePath);
        entityStruct.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
        return entityStruct;
    }

    /**
     * Given an input stream, create a temporary file and write the content on to it.
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
                LOG.error("Error occured while closing outputstream in writeToTemporaryFile", e.getMessage());
            }
        }
    }

    /**
     * Read bytes from a given file.
     *
     * @param filePath a string representing the file
     * @return bytes read from the given file
     */
    private static byte[] readFromFile(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new BallerinaException("Error while reading content from file handler: " + e.getMessage());
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
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[4096];
            for (int len; (len = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }

    /**
     * Handle composite media type content. This method populates a set of body parts as an array of ballerina entities
     * and set them into the top level entity. Nested parts are not covered yet.
     *
     * @param context    Represent ballerina context
     * @param entity     Represent an 'Entity'
     * @param multiparts Represent a list of body parts
     */
    public static void handleMultipartFormData(Context context, BStruct entity, List<HttpBodyPart> multiparts) {
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        for (HttpBodyPart bodyPart : multiparts) {
            BStruct partStruct = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, ENTITY);
            String baseType = bodyPart.getContentType();
            partStruct.setIntField(SIZE_INDEX, bodyPart.getSize());
            BStruct mediaType = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
            setContentType(mediaType, partStruct, baseType);
            EntityBodyHandler.handleDiscreteMediaTypeContent(context, partStruct, new ByteArrayInputStream(bodyPart.getContent()));
            bodyParts.add(partStruct);
        }
        setPartsToTopLevelEntity(entity, bodyParts);
    }

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
                BMap map = (BMap) mediaType.getRefField(PARAMETER_MAP_INDEX);
                Set<String> keys = map.keySet();
                int index = 0;
                for (String key : keys) {
                    BString paramValue = (BString) map.get(key);
                    if (index == keys.size() - 1) {
                        contentType = contentType + key + "=" + paramValue.toString();
                    } else {
                        contentType = contentType + key + "=" + paramValue.toString() + ";";
                        index = index + 1;
                    }
                }
            }
        }
        return contentType;
    }

    /**
     * Write content to temp file through a file writer.
     *
     * @param file            Represent the file that the content needs to be written to
     * @param messageAsString Actual content that needs to be written
     * @throws IOException In case an exception occurs when writing to temp file
     */
    public static void writeToTempFile(File file, String messageAsString) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
            bufferedWriter.write(messageAsString);
            bufferedWriter.close();
        }
    }

    /**
     * Check whether the given string is not null and empty.
     *
     * @param textPayload Represent a text value
     * @return a boolean indicating the status of nullability and emptiness
     */
    public static boolean isNotNullAndEmpty(String textPayload) {
        return textPayload != null && !textPayload.isEmpty();
    }

    /**
     * Check whether the file handler which indicates the overflow data is null or not.
     *
     * @param entity Represent ballerina entity
     * @return a boolean indicating nullability of the overflow data
     */
    public static boolean isOverFlowDataNotNull(BStruct entity) {
        BStruct overFlowData = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
        if (overFlowData != null) {
            return true;
        }
        return false;
    }

    /**
     * Set ballerina body parts to it's top level entity.
     *
     * @param entity    Represent top level message's entity
     * @param bodyParts Represent ballerina body parts
     */
    public static void setPartsToTopLevelEntity(BStruct entity, ArrayList<BStruct> bodyParts) {
        if (!bodyParts.isEmpty()) {
            BStructType typeOfBodyPart = bodyParts.get(0).getType();
            BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
            BRefValueArray partsArray = new BRefValueArray(result, typeOfBodyPart);
            entity.setRefField(MULTIPART_DATA_INDEX, partsArray);
        }
    }

    /**
     * Populate ballerina body parts with actual body content. Based on the memory threshhold this will either
     * kept in memory or in a temp file.
     *
     * @param context  Represent ballerina context
     * @param bodyPart Represent ballerina body part
     * @param mimePart Represent decoded mime part
     */
    public static void populateBodyContent(Context context, BStruct bodyPart, MIMEPart mimePart) {
        String baseType = getContentType(bodyPart);
        long contentLength = bodyPart.getIntField(SIZE_INDEX);
        if (contentLength > Constants.BYTE_LIMIT || contentLength == NO_CONTENT_LENGTH_FOUND) {
            writeToTempFile(context, bodyPart, mimePart);
        } else {
            saveInMemory(bodyPart, mimePart, baseType);
        }
    }

    /**
     * Keep body part content in memory.
     *
     * @param entity   Represent top level entity
     * @param mimePart Represent a decoded mime part
     * @param baseType Represent base type of the body part
     */
    private static void saveInMemory(BStruct entity, MIMEPart mimePart, String baseType) {
        try {
            if (baseType != null) {
                switch (baseType) {
                    case TEXT_PLAIN:
                    case APPLICATION_FORM:
                        entity.setStringField(TEXT_DATA_INDEX, StringUtils.getStringFromInputStream(mimePart.read()));
                        break;
                    case APPLICATION_JSON:
                        entity.setRefField(JSON_DATA_INDEX, new BJSON(mimePart.read()));
                        break;
                    case TEXT_XML:
                    case APPLICATION_XML:
                        entity.setRefField(XML_DATA_INDEX, XMLUtils.parse(mimePart.read()));
                        break;
                    default:
                        entity.setBlobField(BYTE_DATA_INDEX, getByteArray(mimePart.read()));
                        break;
                }
            } else {
                entity.setBlobField(BYTE_DATA_INDEX, getByteArray(mimePart.read()));
            }
        } catch (IOException e) {
            LOG.error("Error occurred while reading decoded body part content", e.getMessage());
        }
    }

    /**
     * Save body part content in a temporary file.
     *
     * @param context  Represent ballerina context
     * @param entity   Represent top level entity
     * @param mimePart Represent a decoded mime part
     */
    private static void writeToTempFile(Context context, BStruct entity, MIMEPart mimePart) {
        try {
            File tempFile = File.createTempFile(BALLERINA_BODY_PART_CONTENT, TEMP_FILE_EXTENSION);
            mimePart.moveTo(tempFile);
            createBallerinaFileHandler(context, entity, tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNewMultipartDelimiter() {
        return Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
    }

    public static boolean isContentInMemory(BStruct entity, String baseType) {
        switch (baseType) {
            case TEXT_PLAIN:
                return isTextPayloadInMemory(entity);
            case APPLICATION_JSON:
                return isJsonPayloadInMemory(entity);
            case APPLICATION_XML:
            case TEXT_XML:
                return isXmlPayloadInMemory(entity);
            default:
                return isBinaryPayloadInMemory(entity);
        }
    }

    private static boolean isBinaryPayloadInMemory(BStruct entity) {
        byte[] binaryPayload = entity.getBlobField(BYTE_DATA_INDEX);
        if (binaryPayload != null) {
            return true;
        }
        return false;
    }

    private static boolean isXmlPayloadInMemory(BStruct entity) {
        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
        if (xmlRefType != null) {
            BXML xmlPayload = (BXML) entity.getRefField(XML_DATA_INDEX);
            if (xmlPayload != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJsonPayloadInMemory(BStruct entity) {
        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
        if (jsonRefType != null) {
            BJSON jsonPayload = (BJSON) entity.getRefField(JSON_DATA_INDEX);
            if (jsonPayload != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTextPayloadInMemory(BStruct entity) {
        String textPayload = entity.getStringField(TEXT_DATA_INDEX);
        if (isNotNullAndEmpty(textPayload)) {
            return true;
        }
        return false;
    }

    public static BStruct extractEntity(BStruct httpMessageStruct) {
        Object isEntityBodyAvailable = httpMessageStruct.getNativeData(IS_ENTITY_BODY_PRESENT);
        if (isEntityBodyAvailable == null || !((Boolean) isEntityBodyAvailable)) {
            return null;
        }
        return (BStruct) httpMessageStruct.getNativeData(MESSAGE_ENTITY);
    }

    /**
     * Construct 'MessageDataSource' with the entity body read from memory.
     *
     * @param entity Represent entity
     * @return Newly created 'MessageDataSource' from the entity body
     */
    public static MessageDataSource readMessageDataSource(BStruct entity) {
        String baseType = MimeUtil.getContentType(entity);
        if (baseType != null) {
            switch (baseType) {
                case TEXT_PLAIN:
                    String textPayload = MimeUtil.getTextPayloadFromMemory(entity);
                    return new StringDataSource(textPayload);
                case APPLICATION_JSON:
                    BJSON jsonPayload = MimeUtil.getJsonPayloadFromMemory(entity);
                    if (jsonPayload != null) {
                        return jsonPayload;
                    }
                    break;
                case APPLICATION_XML:
                case TEXT_XML:
                    BXML xmlPayload = MimeUtil.getXmlPayloadFromMemory(entity);
                    if (xmlPayload != null) {
                        return xmlPayload;
                    }
                    break;
                default:
                    byte[] binaryPayload = MimeUtil.getBinaryPayloadFromMemory(entity);
                    if (binaryPayload != null) {
                        return new BlobDataSource(binaryPayload);
                    }
            }
        } else {
            byte[] binaryPayload = MimeUtil.getBinaryPayloadFromMemory(entity);
            if (binaryPayload != null) {
                return new BlobDataSource(binaryPayload);
            }
        }
        return null;
    }

    public static void writeFileToOutputStream(BStruct entityStruct, OutputStream messageOutputStream) throws IOException {
        String overFlowFilePath = MimeUtil.getOverFlowFileLocation(entityStruct);
        Files.copy(Paths.get(overFlowFilePath), messageOutputStream);
    }
}
