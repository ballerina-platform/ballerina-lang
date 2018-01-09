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

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.BALLERINA_BINARY_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_JSON_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_TEXT_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_XML_DATA;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.FALSE;
import static org.ballerinalang.mime.util.Constants.FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_IN_MEMORY_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUFFIX_INDEX;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TRUE;
import static org.ballerinalang.mime.util.Constants.UTF_8;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Entity related operations and mime utility functions are included here.
 *
 * @since 0.96
 */
public class MimeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MimeUtil.class);

    /**
     * Read the string payload from input stream and set it into request or response's entity struct. If the content
     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
     * memory.
     *
     * @param context       Ballerina Context
     * @param entityStruct  Represent 'Entity' struct
     * @param inputStream   Represent input stream coming from the request/response
     * @param contentLength Content length of the request
     */
    public static void readAndSetStringPayload(Context context, BStruct entityStruct, InputStream inputStream,
            long contentLength) {
        if (contentLength > Constants.BYTE_LIMIT) {
            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_TEXT_DATA);
            createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            String payload = StringUtils.getStringFromInputStream(inputStream);
            entityStruct.setStringField(TEXT_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    /**
     * Read the json payload from input stream and set it into request or response's entity struct. If the content
     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
     * memory.
     *
     * @param context       Ballerina Context
     * @param entityStruct  Represent 'Entity' struct
     * @param inputStream   Represent input stream coming from the request/response
     * @param contentLength Content length of the request
     */
    public static void readAndSetJsonPayload(Context context, BStruct entityStruct, InputStream inputStream,
            long contentLength) {
        if (contentLength > Constants.BYTE_LIMIT) {
            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_JSON_DATA);
            createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            BJSON payload = new BJSON(inputStream);
            entityStruct.setRefField(JSON_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    /**
     * Read the xml payload from input stream and set it into request or response's entity struct. If the content
     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
     * memory.
     *
     * @param context       Ballerina Context
     * @param entityStruct  Represent 'Entity' struct
     * @param inputStream   Represent input stream coming from the request/response
     * @param contentLength Content length of the request
     */
    public static void readAndSetXmlPayload(Context context, BStruct entityStruct, InputStream inputStream,
            long contentLength) {
        if (contentLength > Constants.BYTE_LIMIT) {
            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_XML_DATA);
            createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            BXML payload = XMLUtils.parse(inputStream);
            entityStruct.setRefField(XML_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    /**
     * Read the binary payload from input stream and set it into request or response's entity struct. If the content
     * length exceeds the BYTE_LIMIT, data will be written on to a temporary file. Otherwise data will be kept in
     * memory.
     *
     * @param context       Ballerina Context
     * @param entityStruct  Represent 'Entity' struct
     * @param inputStream   Represent input stream coming from the request/response
     * @param contentLength Content length of the request
     */
    public static void readAndSetBinaryPayload(Context context, BStruct entityStruct, InputStream inputStream,
            long contentLength) {
        if (contentLength > Constants.BYTE_LIMIT) {
            String temporaryFilePath = writeToTemporaryFile(inputStream, BALLERINA_BINARY_DATA);
            createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            byte[] payload;
            try {
                payload = getByteArray(inputStream);
            } catch (IOException e) {
                throw new BallerinaException("Error while converting inputstream to a byte array: " + e.getMessage());
            }
            entityStruct.setBlobField(BYTE_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    /**
     * Check whether the 'Entity' body is present in text form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of text payload
     */
    public static boolean isTextBodyPresent(BStruct entity) {
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            String textPayload = entity.getStringField(TEXT_DATA_INDEX);
            if (textPayload != null) {
                return true;
            }
        } else {
            BStruct overFlowData = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            if (overFlowData != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the 'Entity' body is present in json form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of json payload
     */
    public static boolean isJsonBodyPresent(BStruct entity) {
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            BJSON jsonPayload = (BJSON) entity.getRefField(JSON_DATA_INDEX);
            if (jsonPayload != null) {
                return true;
            }
        } else {
            BStruct overFlowData = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            if (overFlowData != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the 'Entity' body is present in xml form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of xml payload
     */
    public static boolean isXmlBodyPresent(BStruct entity) {
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            BXML xmlPayload = (BXML) entity.getRefField(XML_DATA_INDEX);
            if (xmlPayload != null) {
                return true;
            }
        } else {
            BStruct overFlowData = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            if (overFlowData != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the 'Entity' body is present in binary form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of binary payload
     */
    public static boolean isBinaryBodyPresent(BStruct entity) {
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            byte[] binaryPayload = entity.getBlobField(BYTE_DATA_INDEX);
            if (binaryPayload != null) {
                return true;
            }
        } else {
            BStruct overFlowData = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            if (overFlowData != null) {
                return true;
            }
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
        String returnValue = null;
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            return entity.getStringField(TEXT_DATA_INDEX);
        } else {
            BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            String filePath = fileHandler.getStringField(FILE_PATH_INDEX);
            try {
                returnValue = new String(readFromFile(filePath), UTF_8);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting text payload from entity", e.getMessage());
            }
        }
        return returnValue;
    }

    /**
     * Extract the json payload from entity.
     *
     * @param entity Represent 'Entity' struct
     * @return json content in BJSON form
     */
    public static BJSON getJsonPayload(BStruct entity) {
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            return (BJSON) entity.getRefField(JSON_DATA_INDEX);
        } else {
            BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            String filePath = fileHandler.getStringField(FILE_PATH_INDEX);
            try {
                return new BJSON(new String(readFromFile(filePath), UTF_8));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting json payload from entity", e.getMessage());
            }
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
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            return  (BXML) entity.getRefField(XML_DATA_INDEX);
        } else {
            BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            String filePath = fileHandler.getStringField(FILE_PATH_INDEX);
            try {
                return XMLUtils.parse(new String(readFromFile(filePath), UTF_8));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occured while extracting xml payload from entity", e.getMessage());
            }
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
        boolean isInMemory = entity.getBooleanField(IS_IN_MEMORY_INDEX) == 1;
        if (isInMemory) {
            return entity.getBlobField(BYTE_DATA_INDEX);
        } else {
            BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
            String filePath = fileHandler.getStringField(FILE_PATH_INDEX);
            return readFromFile(filePath);
        }
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
     * Populate given 'Entity' with it's body size.
     *
     * @param entityStruct Represent 'Entity'
     * @param length       Size of the entity body
     */
    public static void setContentLength(BStruct entityStruct, int length) {
        entityStruct.setIntField(SIZE_INDEX, length);
    }

    /**
     * Create a ballerina file struct and set it into the given 'Entity'.
     *
     * @param context           Represent ballerina context
     * @param entityStruct      Represent 'Entity'
     * @param temporaryFilePath Temporary file path
     * @return Entity struct populated with file handler
     */
    private static BStruct createBallerinaFileHandler(Context context, BStruct entityStruct, String temporaryFilePath) {
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
    private static String writeToTemporaryFile(InputStream inputStream, String fileName) {
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
     * @throws IOException
     */
    private static byte[] getByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[4096];
            for (int len; (len = input.read(buffer)) != -1; ) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }
}
