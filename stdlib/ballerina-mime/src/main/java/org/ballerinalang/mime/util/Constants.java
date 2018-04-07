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
    public static final String ENTITY_ERROR = "EntityError";
    public static final String PROTOCOL_PACKAGE_MIME = "ballerina.mime";
    public static final String PROTOCOL_PACKAGE_FILE = "ballerina.file";
    public static final String PROTOCOL_PACKAGE_IO = "ballerina.io";
    public static final String BYTE_CHANNEL_STRUCT = "ByteChannel";

    /**
     * Content type HTTP header.
     */

    public static final String CONTENT_ID = "content-id";

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

    /**
     * Content-type multipart/form-data.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * Content-type multipart/mixed.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    public static final String MULTIPART_AS_PRIMARY_TYPE = "multipart/";
    public static final String BOUNDARY = "boundary";
    public static final String FORM_DATA_PARAM = "form-data";

    public static final String DEFAULT_PRIMARY_TYPE = "application";
    public static final String DEFAULT_SUB_TYPE = "octet-stream";
    public static final String SUFFIX_ATTACHMENT = "+";

    public static final String MESSAGE_ENTITY = "message_entity";
    public static final String MESSAGE_DATA_SOURCE = "message_datasource";
    public static final String IS_BODY_BYTE_CHANNEL_ALREADY_SET = "is_byte_channel_set";
    public static final String ENTITY_BYTE_CHANNEL = "entity_byte_channel";
    public static final String MULTIPART_ENCODER = "MultipartEncoder";
    public static final String BODY_PARTS = "body_parts";
    public static final String ENTITY_HEADERS = "entity_headers";

    //Native argument indexes
    public static final int FIRST_PARAMETER_INDEX = 0;
    public static final int SECOND_PARAMETER_INDEX = 1;

    //Entity properties
    public static final int MEDIA_TYPE_INDEX = 0;
    public static final int CONTENT_ID_INDEX = 0;
    public static final int SIZE_INDEX = 0;
    public static final int CONTENT_DISPOSITION_INDEX = 1;

    public static final int FIRST_BODY_PART_INDEX = 0;

    //Media type properties
    public static final int PRIMARY_TYPE_INDEX = 0;
    public static final int SUBTYPE_INDEX = 1;
    public static final int SUFFIX_INDEX = 2;
    public static final int PARAMETER_MAP_INDEX = 0;

    //Content-Disposition properties
    public static final int CONTENT_DISPOSITION_FILENAME_INDEX = 0;
    public static final int DISPOSITION_INDEX = 1;
    public static final int CONTENT_DISPOSITION_NAME_INDEX = 2;
    public static final int CONTENT_DISPOSITION_PARA_MAP_INDEX = 0;

    //Native function indexes
    public static final int BLOB_INDEX = 0;
    public static final int STRING_INDEX = 0;
    public static final int CHARSET_INDEX = 1;

    public static final int FIRST_ELEMENT = 0;

    public static final int BYTE_LIMIT = 2097152; //2MB
    public static final int READABLE_BUFFER_SIZE = 8192; //8KB

    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TRANSFER_ENCODING_7_BIT = "7bit";
    public static final String CONTENT_TRANSFER_ENCODING_8_BIT = "8bit";

    public static final String TEMP_FILE_EXTENSION = ".tmp";
    public static final String TEMP_FILE_NAME = "tempFile";

    public static final String BALLERINA_TEMP_FILE = "BallerinaTempFile";

    public static final int NO_CONTENT_LENGTH_FOUND = -1;

    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";
    public static final String ASSIGNMENT = "=";
    public static final String DOUBLE_QUOTE = "\"";
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
