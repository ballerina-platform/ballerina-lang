package org.ballerinalang.net.mime.util;

/**
 * Constants related to MIME.
 */
public class Constants {

    public static final String ENTITY = "Entity";
    public static final String FILE = "File";
    public static final String MEDIA_TYPE = "MediaType";
    public static final String PROTOCOL_PACKAGE_MIME = "ballerina.net.mime";
    public static final String PROTOCOL_PACKAGE_FILE = "ballerina.file";
    public static final String HEADER_VALUE_STRUCT = "HeaderValue";

    //TODO: Remove following properties from http package.
    /**
     * Content type HTTP header.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Content-type application/json.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Content-type application/xml.
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * Content-type text/plain.
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * Content-type application/octet-stream.
     */
    public static final String OCTET_STREAM = "application/octet-stream";

    /**
     * Content-type application/x-www-form-urlencoded.
     */
    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";

    public static final String DEFAULT_PRIMARY_TYPE = "application";
    public static final String DEFAULT_SUB_TYPE = "octet-stream";
    public static final String SUFFIX_ATTACHMENT = "+";

    public static final String MESSAGE_ENTITY = "message_entity";
    public static final String IS_ENTITY_BODY_PRESENT = "is_entity_body_present";

    //Entity properties
    public static final int TEXT_DATA_INDEX = 1;
    public static final int JSON_DATA_INDEX = 2;
    public static final int XML_DATA_INDEX = 3;
    public static final int BYTE_DATA_INDEX = 0;
    public static final int SIZE_INDEX = 0;
    public static final int OVERFLOW_DATA_INDEX = 4;
    public static final int TEMP_FILE_PATH_INDEX = 0;
    public static final int IS_IN_MEMORY_INDEX = 0;
    public static final int MEDIA_TYPE_INDEX = 0;
    public static final int ENTITY_HEADERS_INDEX = 1;

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    //Media type properties
    public static final int PRIMARY_TYPE_INDEX = 0;
    public static final int SUBTYPE_INDEX = 1;
    public static final int SUFFIX_INDEX = 2;
    public static final int PARAMETER_MAP_INDEX = 0;

    public static final int BYTE_LIMIT = 2097152; //2MB

    public static final String TEMP_FILE_EXTENSION = ".tmp";
    public static final String BALLERINA_TEXT_DATA = "ballerinaTextPayload";
    public static final String BALLERINA_JSON_DATA = "ballerinaJsonPayload";
    public static final String BALLERINA_XML_DATA = "ballerinaXmlPayload";
    public static final String BALLERINA_BINARY_DATA = "ballerinaBinaryPayload";
}
