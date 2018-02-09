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

/**
 * Constants related to MIME.
 */
public class Constants {

    public static final String ENTITY = "Entity";
    public static final String FILE = "File";
    public static final String MEDIA_TYPE = "MediaType";
    public static final String CONTENT_DISPOSITION_STRUCT = "ContentDisposition";
    public static final String PROTOCOL_PACKAGE_MIME = "ballerina.mime";
    public static final String PROTOCOL_PACKAGE_FILE = "ballerina.file";

    /**
     * Content type HTTP header.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "content-length";

    public static final String CONTENT_DISPOSITION = "content-disposition";

    /**
     * Content-Transfer-Encoding HTTP header.
     */
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

    /**
     * Content-type application/json.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Content-type application/xml.
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * Content-type text/xml.
     */
    public static final String TEXT_XML = "text/xml";

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

    /**
     * Content-type multipart/form-data.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    /**
     * Content-type multipart/mixed.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    public static final String MULTIPART_AS_PRIMARY_TYPE = "multipart";
    public static final String BOUNDARY = "boundary";

    public static final String DEFAULT_PRIMARY_TYPE = "application";
    public static final String DEFAULT_SUB_TYPE = "octet-stream";
    public static final String SUFFIX_ATTACHMENT = "+";

    public static final String MESSAGE_ENTITY = "message_entity";
    public static final String IS_ENTITY_BODY_PRESENT = "is_entity_body_present";
    public static final String MULTIPART_ENCODER = "MultipartEncoder";

    //Entity properties
    public static final int TEXT_DATA_INDEX = 1;
    public static final int JSON_DATA_INDEX = 2;
    public static final int XML_DATA_INDEX = 3;
    public static final int BYTE_DATA_INDEX = 0;
    public static final int SIZE_INDEX = 0;
    public static final int OVERFLOW_DATA_INDEX = 4;
    public static final int TEMP_FILE_PATH_INDEX = 0;
    public static final int MEDIA_TYPE_INDEX = 0;
    public static final int ENTITY_HEADERS_INDEX = 1;
    public static final int MULTIPART_DATA_INDEX = 5;
    public static final int CONTENT_DISPOSITION_INDEX = 6;

    //Media type properties
    public static final int PRIMARY_TYPE_INDEX = 0;
    public static final int SUBTYPE_INDEX = 1;
    public static final int SUFFIX_INDEX = 2;
    public static final int PARAMETER_MAP_INDEX = 0;

    //Content-Disposition properties
    public static final int FILENAME_INDEX = 0;
    public static final int DISPOSITION_INDEX = 1;
    public static final int NAME_INDEX = 2;
    public static final int CONTENT_DISPOSITION_PARA_MAP_INDEX = 0;

    //Native function indexes
    public static final int BLOB_INDEX = 0;
    public static final int STRING_INDEX = 0;
    public static final int CHARSET_INDEX = 1;

    public static final int HEADER_VALUE_INDEX = 0;

    public static final int FILE_PATH_INDEX = 0;

    public static final int FIRST_ELEMENT = 0;

    public static final int BYTE_LIMIT = 2097152; //2MB
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TRANSFER_ENCODING_7_BIT = "7bit";
    public static final String CONTENT_TRANSFER_ENCODING_8_BIT = "8bit";
    public static final String CONTENT_TRANSFER_ENCODING_BINARY = "binary";
    public static final String FILE_SIZE = "basic:size";

    public static final String TEMP_FILE_EXTENSION = ".tmp";
    public static final String TEMP_FILE_NAME = "tempFile";
    public static final String JSON_EXTENSION = ".json";
    public static final String XML_EXTENSION = ".xml";

    public static final String BALLERINA_TEXT_DATA = "BallerinaTextPayload";
    public static final String BALLERINA_JSON_DATA = "BallerinaJsonPayload";
    public static final String BALLERINA_XML_DATA = "BallerinaXmlPayload";
    public static final String BALLERINA_BINARY_DATA = "BallerinaBinaryPayload";
    public static final String BALLERINA_BODY_PART_CONTENT = "BallerinaBodyPart";

    public static final int NO_CONTENT_LENGTH_FOUND = -1;

    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";
    public static final String PARSER_ERROR = "failed to parse: ";
    public static final String BUILTIN_PACKAGE = "ballerina.builtin";
    public static final String STRUCT_GENERIC_ERROR = "error";
    public static final String CONTENT_DISPOSITION_FILE_NAME = "filename";
    public static final String CONTENT_DISPOSITION_NAME = "name";

    /**
     * Describes the format of the body part.
     */
    public enum BodyPartForm {
        INPUTSTREAM, FILE
    }
}
