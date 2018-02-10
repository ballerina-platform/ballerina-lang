package org.ballerinalang.mime.util;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BALLERINA_BINARY_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_BODY_PART_CONTENT;
import static org.ballerinalang.mime.util.Constants.BALLERINA_JSON_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_TEXT_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_XML_DATA;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.TEXT_XML;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Entity body related operations are included here.
 */
public class EntityBodyHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyHandler.class);

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
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_TEXT_DATA);
            MimeUtil.createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
        } else {
            String payload = StringUtils.getStringFromInputStream(inputStream);
            entityStruct.setStringField(TEXT_DATA_INDEX, payload);
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
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_JSON_DATA);
            MimeUtil.createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
        } else {
            BJSON payload = new BJSON(inputStream);
            entityStruct.setRefField(JSON_DATA_INDEX, payload);
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
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_XML_DATA);
            MimeUtil.createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
        } else {
            BXML payload = XMLUtils.parse(inputStream);
            entityStruct.setRefField(XML_DATA_INDEX, payload);
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
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_BINARY_DATA);
            MimeUtil.createBallerinaFileHandler(context, entityStruct, temporaryFilePath);
        } else {
            byte[] payload;
            try {
                payload = MimeUtil.getByteArray(inputStream);
            } catch (IOException e) {
                throw new BallerinaException("Error while converting inputstream to a byte array: " + e.getMessage());
            }
            entityStruct.setBlobField(BYTE_DATA_INDEX, payload);
        }
    }

    /**
     * Check whether the 'Entity' body is present in text form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of text payload
     */
    public static boolean isTextBodyPresent(BStruct entity) {
        String textPayload = entity.getStringField(TEXT_DATA_INDEX);
        if (MimeUtil.isNotNullAndEmpty(textPayload)) {
            return true;
        } else {
            return isOverFlowDataNotNull(entity);
        }
    }

    /**
     * Check whether the 'Entity' body is present in json form.
     *
     * @param entity Represent 'Entity' struct
     * @return a boolean denoting the availability of json payload
     */
    public static boolean isJsonBodyPresent(BStruct entity) {
        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
        if (jsonRefType != null) {
            BJSON jsonPayload = (BJSON) entity.getRefField(JSON_DATA_INDEX);
            if (jsonPayload != null) {
                return true;
            }
        } else {
            return isOverFlowDataNotNull(entity);
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
        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
        if (xmlRefType != null) {
            BXML xmlPayload = (BXML) entity.getRefField(XML_DATA_INDEX);
            if (xmlPayload != null) {
                return true;
            }
        } else {
            return isOverFlowDataNotNull(entity);
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
        byte[] binaryPayload = entity.getBlobField(BYTE_DATA_INDEX);
        if (binaryPayload != null) {
            return true;
        } else {
            return isOverFlowDataNotNull(entity);
        }
    }

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
     * Check whether the entity body is present.
     *
     * @param entity   Represent an 'Entity'
     * @param baseType Content type that describes the entity body
     * @return a boolean indicating entity body availability
     */
    public static boolean checkEntityBodyAvailability(BStruct entity, String baseType) {
        switch (baseType) {
            case TEXT_PLAIN:
                return isTextBodyPresent(entity);
            case APPLICATION_JSON:
                return isJsonBodyPresent(entity);
            case APPLICATION_XML:
            case TEXT_XML:
                return isXmlBodyPresent(entity);
            case MULTIPART_FORM_DATA:
                return isMultipartsAvailable(entity);
            default:
                return isBinaryBodyPresent(entity);
        }
    }

    /**
     * Handle discrete media type content. This method populates ballerina entity with the relevant payload.
     *
     * @param context     Represent ballerina context
     * @param entity      Represent an 'Entity'
     * @param inputStream Represent input stream coming from the request/response
     */
    public static void handleDiscreteMediaTypeContent(Context context, BStruct entity, InputStream inputStream) {
        String baseType = MimeUtil.getContentType(entity);
        long contentLength = entity.getIntField(SIZE_INDEX);
        if (baseType != null) {
            switch (baseType) {
                case TEXT_PLAIN:
                case APPLICATION_FORM:
                    readAndSetStringPayload(context, entity, inputStream, contentLength);
                    break;
                case APPLICATION_JSON:
                    readAndSetJsonPayload(context, entity, inputStream, contentLength);
                    break;
                case TEXT_XML:
                case APPLICATION_XML:
                    readAndSetXmlPayload(context, entity, inputStream, contentLength);
                    break;
                default:
                    readAndSetBinaryPayload(context, entity, inputStream, contentLength);
                    break;
            }
        } else {
            readAndSetBinaryPayload(context, entity, inputStream, contentLength);
        }
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
                    String textPayload = getTextPayloadFromMemory(entity);
                    return new StringDataSource(textPayload);
                case APPLICATION_JSON:
                    BJSON jsonPayload = getJsonPayloadFromMemory(entity);
                    if (jsonPayload != null) {
                        return jsonPayload;
                    }
                    break;
                case APPLICATION_XML:
                case TEXT_XML:
                    BXML xmlPayload = getXmlPayloadFromMemory(entity);
                    if (xmlPayload != null) {
                        return xmlPayload;
                    }
                    break;
                default:
                    byte[] binaryPayload = getBinaryPayloadFromMemory(entity);
                    if (binaryPayload != null) {
                        return new BlobDataSource(binaryPayload);
                    }
            }
        } else {
            byte[] binaryPayload = getBinaryPayloadFromMemory(entity);
            if (binaryPayload != null) {
                return new BlobDataSource(binaryPayload);
            }
        }
        return null;
    }

    /**
     * Extract text payload which is in memory from a given entity.
     *
     * @param entity Represent a ballerina entity, which can either be a top level entity or a body part.
     * @return Text payload as a string
     */
    private static String getTextPayloadFromMemory(BStruct entity) {
        String returnValue = entity.getStringField(TEXT_DATA_INDEX);
        if (MimeUtil.isNotNullAndEmpty(returnValue)) {
            return returnValue;
        }
        return null;
    }

    /**
     * Extract json payload which is in memory from a given entity.
     *
     * @param entity Represent a ballerina entity, which can either be a top level entity or a body part.
     * @return json payload as a BJSON
     */
    private static BJSON getJsonPayloadFromMemory(BStruct entity) {
        BRefType jsonRefType = entity.getRefField(JSON_DATA_INDEX);
        if (jsonRefType != null) {
            return (BJSON) entity.getRefField(JSON_DATA_INDEX);
        }
        return null;
    }

    /**
     * Extract the xml payload from entity.
     *
     * @param entity Represent a ballerina entity, which can either be a top level entity or a body part.
     * @return xml content in BXML form
     */
    private static BXML getXmlPayloadFromMemory(BStruct entity) {
        BRefType xmlRefType = entity.getRefField(XML_DATA_INDEX);
        if (xmlRefType != null) {
            return (BXML) entity.getRefField(XML_DATA_INDEX);
        }
        return null;
    }

    /**
     * Extract the binary payload from entity.
     *
     * @param entity Represent a ballerina entity, which can either be a top level entity or a body part.
     * @return entity body as a byte array
     */
    private static byte[] getBinaryPayloadFromMemory(BStruct entity) {
        byte[] byteData = entity.getBlobField(BYTE_DATA_INDEX);
        if (byteData != null) {
            return entity.getBlobField(BYTE_DATA_INDEX);
        }
        return null;
    }

    /**
     * Get overflow file location from entity.
     *
     * @param entity Represent a ballerina entity, which can either be a top level entity or a body part.
     * @return Overflow file location as a string
     */
    public static String getOverFlowFileLocation(BStruct entity) {
        BStruct fileHandler = (BStruct) entity.getRefField(OVERFLOW_DATA_INDEX);
        return fileHandler.getStringField(FILE_PATH_INDEX);
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
        String baseType = MimeUtil.getContentType(bodyPart);
        long contentLength = bodyPart.getIntField(SIZE_INDEX);
        if (contentLength > Constants.BYTE_LIMIT || contentLength == NO_CONTENT_LENGTH_FOUND) {
            writeToTempFile(context, bodyPart, mimePart);
        } else {
            saveInMemory(bodyPart, mimePart, baseType);
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
            MimeUtil.createBallerinaFileHandler(context, entity, tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
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
                        entity.setBlobField(BYTE_DATA_INDEX, MimeUtil.getByteArray(mimePart.read()));
                        break;
                }
            } else {
                entity.setBlobField(BYTE_DATA_INDEX, MimeUtil.getByteArray(mimePart.read()));
            }
        } catch (IOException e) {
            LOG.error("Error occurred while reading decoded body part content", e.getMessage());
        }
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
        if (MimeUtil.isNotNullAndEmpty(textPayload)) {
            return true;
        }
        return false;
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
}
