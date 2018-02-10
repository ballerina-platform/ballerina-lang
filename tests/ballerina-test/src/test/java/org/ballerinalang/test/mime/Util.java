package org.ballerinalang.test.mime;

import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.FILE;
import static org.ballerinalang.mime.util.Constants.FILE_PATH_INDEX;
import static org.ballerinalang.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_ENCODER;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.Constants.XML_DATA_INDEX;

/**
 * Contains utility functions used by mime test cases.
 */
public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final String REQUEST_STRUCT = Constants.IN_REQUEST;
    private static final String PROTOCOL_PACKAGE_HTTP = Constants.PROTOCOL_PACKAGE_HTTP;
    private static final String PACKAGE_MIME = org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
    private static final String PACKAGE_FILE = PROTOCOL_PACKAGE_FILE;
    private static final String ENTITY_STRUCT = Constants.ENTITY;
    private static final String MEDIA_TYPE_STRUCT = MEDIA_TYPE;
    private static final String CARBON_MESSAGE = "CarbonMessage";
    private static final String BALLERINA_REQUEST = "BallerinaRequest";
    private static final String MULTIPART_ENTITY = "MultipartEntity";

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BRefValueArray representing an array of entities
     */
    public static BRefValueArray getArrayOfBodyParts(ArrayList<BStruct> bodyParts) {
        BStructType typeOfBodyPart = bodyParts.get(0).getType();
        BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
        return new BRefValueArray(result, typeOfBodyPart);
    }

    /**
     * Get a text body part from a given text content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BStruct getTextBodyPart(CompileResult result) {
        BStruct bodyPart = getEntityStruct(result);
        bodyPart.setStringField(TEXT_DATA_INDEX, "Ballerina text body part");
        // bodyPart.setStringField(ENTITY_NAME_INDEX, "Text Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @return a body part with text content in a file
     */
    public static BStruct getTextFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina text as a file part");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_FILE, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            //   bodyPart.setStringField(ENTITY_NAME_INDEX, "Text File Part");
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a text body part from a given text content and content transfer encoding.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BStruct getTextFilePartWithEncoding(String contentTransferEncoding, String message, CompileResult result) {

        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(message);
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_FILE, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            //   bodyPart.setStringField(ENTITY_NAME_INDEX, "Text File Part");
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            BMap<String, BValue> headerMap = new BMap<>();
            headerMap.put(CONTENT_TRANSFER_ENCODING, new BStringArray(new String[]{contentTransferEncoding}));
            bodyPart.setRefField(ENTITY_HEADERS_INDEX, headerMap);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a json body part from a given json content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BStruct getJsonBodyPart(CompileResult result) {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BStruct bodyPart = getEntityStruct(result);
        bodyPart.setRefField(JSON_DATA_INDEX, new BJSON(jsonContent));
        //   bodyPart.setStringField(ENTITY_NAME_INDEX, "Json Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @return a body part with json content in a file
     */
    public static BStruct getJsonFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'name':'wso2'}");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_FILE, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            //    bodyPart.setStringField(ENTITY_NAME_INDEX, "Json File Part");
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for json file part in getJsonFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a xml body part from a given xml content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BStruct getXmlBodyPart(CompileResult result) {
        BXMLItem xmlContent = new BXMLItem("<name>Ballerina</name>");
        BStruct bodyPart = getEntityStruct(result);
        bodyPart.setRefField(XML_DATA_INDEX, xmlContent);
        //    bodyPart.setStringField(ENTITY_NAME_INDEX, "Xml Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @return a body part with xml content in a file
     */
    public static BStruct getXmlFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".xml");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("<name>Ballerina xml file part</name>");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_FILE, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            //    bodyPart.setStringField(ENTITY_NAME_INDEX, "Xml File Part");
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for xml file part in getXmlFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a binary body part from a given blob content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BStruct getBinaryBodyPart(CompileResult result) {
        BStruct bodyPart = getEntityStruct(result);
        bodyPart.setBlobField(BYTE_DATA_INDEX, "Ballerina binary part".getBytes());
        //    bodyPart.setStringField(ENTITY_NAME_INDEX, "Binary Body Part");
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @return a body part with blob content in a file
     */
    public static BStruct getBinaryFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina binary file part");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_FILE, FILE);
            fileStruct.setStringField(FILE_PATH_INDEX, file.getAbsolutePath());
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            //   bodyPart.setStringField(ENTITY_NAME_INDEX, "Binary File Part");
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occured while creating a temp file for binary file part in getBinaryFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Create prerequisite messages that are needed to proceed with the test cases.
     *
     * @param path Represent path to the resource
     * @return A map of relevant messages
     */
    public static Map<String, Object> createPrerequisiteMessages(String path, String topLevelContentType, CompileResult result) {
        Map<String, Object> messageMap = new HashMap<>();
        BStruct request = getRequestStruct(result);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessageForMultiparts(path, Constants.HTTP_METHOD_POST);
        HttpUtil.addCarbonMsg(request, cMsg);
        BStruct entity = getEntityStruct(result);
        MimeUtil.setContentType(getMediaTypeStruct(result), entity, topLevelContentType);
        messageMap.put(CARBON_MESSAGE, cMsg);
        messageMap.put(BALLERINA_REQUEST, request);
        messageMap.put(MULTIPART_ENTITY, entity);
        return messageMap;
    }

    /**
     * Create multipart entity and fill the carbon message with body parts.
     *
     * @param messageMap Represent the map of prerequisite messages
     * @param bodyParts  Represent body parts that needs to be added to multipart entity
     * @return A test carbon message to be used for invoking the service with.
     */
    public static HTTPTestRequest getCarbonMessageWithBodyParts(Map<String, Object> messageMap, BRefValueArray bodyParts) {
        HTTPTestRequest cMsg = (HTTPTestRequest) messageMap.get(CARBON_MESSAGE);
        BStruct request = (BStruct) messageMap.get(BALLERINA_REQUEST);
        BStruct entity = (BStruct) messageMap.get(MULTIPART_ENTITY);
        entity.setRefField(MULTIPART_DATA_INDEX, bodyParts);
        request.addNativeData(MESSAGE_ENTITY, entity);
        setCarbonMessageWithMultiparts(request, cMsg);
        return cMsg;
    }

    /**
     * Add body parts to carbon message.
     *
     * @param request Ballerina request struct
     * @param cMsg    Represent carbon message
     */
    public static void setCarbonMessageWithMultiparts(BStruct request, HTTPTestRequest cMsg) {
        HttpUtil.prepareRequestWithMultiparts(cMsg, request);
        try {
            HttpPostRequestEncoder nettyEncoder = (HttpPostRequestEncoder) request.getNativeData(MULTIPART_ENCODER);
            HttpUtil.addMultipartsToCarbonMessage(cMsg, nettyEncoder);
        } catch (Exception e) {
            LOG.error("Error occured while adding multiparts to carbon message in setCarbonMessageWithMultiparts",
                    e.getMessage());
        }
    }

    private static BStruct getRequestStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PROTOCOL_PACKAGE_HTTP, REQUEST_STRUCT);
    }

    private static BStruct getEntityStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME, ENTITY_STRUCT);
    }

    private static BStruct getMediaTypeStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME,
                MEDIA_TYPE_STRUCT);
    }
}
