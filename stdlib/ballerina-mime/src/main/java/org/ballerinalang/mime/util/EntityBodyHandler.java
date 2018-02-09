package org.ballerinalang.mime.util;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.InputStream;

import static org.ballerinalang.mime.util.Constants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BALLERINA_BINARY_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_JSON_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_TEXT_DATA;
import static org.ballerinalang.mime.util.Constants.BALLERINA_XML_DATA;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.TEXT_XML;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

public class EntityBodyHandler {

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
            return MimeUtil.isOverFlowDataNotNull(entity);
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
            return MimeUtil.isOverFlowDataNotNull(entity);
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
            return MimeUtil.isOverFlowDataNotNull(entity);
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
            return MimeUtil.isOverFlowDataNotNull(entity);
        }
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
                return MimeUtil.isMultipartsAvailable(entity);
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
}
