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

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Constants related to MIME.
 */
public class MimeConstants {

    public static final String ENTITY = "Entity";
    public static final String MEDIA_TYPE = "MediaType";
    public static final String CONTENT_DISPOSITION_STRUCT = "ContentDisposition";
    public static final String PROTOCOL_PACKAGE_MIME = BALLERINA_PACKAGE_PREFIX + "mime";
    public static final BPackage PROTOCOL_MIME_PKG_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "mime");
    public static final String PROTOCOL_PACKAGE_IO = BALLERINA_PACKAGE_PREFIX + "io";
    public static final BPackage PROTOCOL_IO_PKG_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "io");
    public static final String READABLE_BYTE_CHANNEL_STRUCT = "ReadableByteChannel";
    public static final String MIME_ERROR_MESSAGE = "message";
    public static final String PARSING_ENTITY_BODY_FAILED = "{ballerina/mime}ParsingEntityBodyFailed";
    public static final String INVALID_CONTENT_TYPE = "{ballerina/mime}InvalidContentType";
    public static final String READING_HEADER_FAILED = "{ballerina/mime}ReadingHeaderFailed";
    public static final String MIME_ERROR_CODE = "{ballerina/mime}MIMEError";
    public static final String INVALID_HEADER_VALUE = "{ballerina/mime}InvalidHeaderValue";
    public static final String INVALID_HEADER_PARAM = "{ballerina/mime}InvalidHeaderParam";
    public static final String INVALID_CONTENT_LENGTH = "{ballerina/mime}InvalidContentLength";
    public static final String HEADER_NOT_FOUND = "{ballerina/mime}HeaderNotFound";
    public static final String SERIALIZATION_ERROR_CODE = "{ballerina/mime}SerializationError";
    public static final String NO_CONTENT_ERROR_CODE = "{ballerina/mime}NoContentError";
    public static final String INVALID_HEADER_OPERATION = "{ballerina/mime}InvalidHeaderOperation";

    /**
     * Content id HTTP header.
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

    public static final String JSON_SUFFIX = "+json";

    public static final String JSON_TYPE_IDENTIFIER = "/json";

    public static final String XML_SUFFIX = "+xml";

    public static final String XML_TYPE_IDENTIFIER = "/xml";

    public static final String TEXT_AS_PRIMARY_TYPE = "text/";

    public static final String MULTIPART_AS_PRIMARY_TYPE = "multipart/";
    public static final String MESSAGE_AS_PRIMARY_TYPE = "message/";
    public static final String BOUNDARY = "boundary";
    public static final String FORM_DATA_PARAM = "form-data";

    public static final String DEFAULT_PRIMARY_TYPE = "application";
    public static final String DEFAULT_SUB_TYPE = "octet-stream";
    public static final String SUFFIX_ATTACHMENT = "+";

    public static final String MESSAGE_DATA_SOURCE = "message_datasource";
    public static final String IS_BODY_BYTE_CHANNEL_ALREADY_SET = "is_byte_channel_set";
    public static final String ENTITY_BYTE_CHANNEL = "entity_byte_channel";
    public static final String MULTIPART_ENCODER = "MultipartEncoder";
    public static final String BODY_PARTS = "body_parts";
    public static final String ENTITY_HEADERS = "entity_headers";
    public static final String ENTITY_TRAILER_HEADERS = "entity_trailer_headers";
    public static final String LEADING_HEADER = "leading";
    public static final String TRANSPORT_MESSAGE = "transport_message";
    public static final String PARSE_AS_JSON = "PARSE_AS_TEXT";

    public static final String CHARSET = "charset";

    public static final String REQUEST_ENTITY_FIELD = "entity";
    public static final String RESPONSE_ENTITY_FIELD = "entity";

    //Native argument indexes
    public static final int FIRST_PARAMETER_INDEX = 0;
    public static final int SECOND_PARAMETER_INDEX = 1;

    //Entity properties
    public static final String MEDIA_TYPE_FIELD = "cType";
    public static final String CONTENT_ID_FIELD = "cId";
    public static final String SIZE_FIELD = "cLength";
    public static final String CONTENT_DISPOSITION_FIELD = "cDisposition";

    public static final int FIRST_BODY_PART_INDEX = 0;

    //Media type properties
    public static final String PRIMARY_TYPE_FIELD = "primaryType";
    public static final String SUBTYPE_FIELD = "subType";
    public static final String SUFFIX_FIELD = "suffix";
    public static final String PARAMETER_MAP_FIELD = "parameters";

    //Content-Disposition properties
    public static final String CONTENT_DISPOSITION_FILENAME_FIELD = "fileName";
    public static final String DISPOSITION_FIELD = "disposition";
    public static final String CONTENT_DISPOSITION_NAME_FIELD = "name";
    public static final String CONTENT_DISPOSITION_PARA_MAP_FIELD = "parameters";

    //extern function indexes
    public static final int STRING_INDEX = 0;

    public static final int FIRST_ELEMENT = 0;

    public static final short ONE_BYTE = 1;
    public static final int READABLE_BUFFER_SIZE = 8192; //8KB
    public static final double MAX_THRESHOLD_PERCENTAGE = 0.1;

    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TRANSFER_ENCODING_7_BIT = "7bit";
    public static final String CONTENT_TRANSFER_ENCODING_8_BIT = "8bit";

    public static final String TEMP_FILE_EXTENSION = ".tmp";
    public static final String TEMP_FILE_NAME = "tempFile";

    public static final int NO_CONTENT_LENGTH_FOUND = -1;

    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";
    public static final String ASSIGNMENT = "=";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String TOKEN_SPECIAL = "\" ()<>@,;:/[]?=\\\"\"";
    public static final String PARSER_ERROR = "failed to parse: ";
    public static final String CONTENT_DISPOSITION_FILE_NAME = "filename";
    public static final String CONTENT_DISPOSITION_NAME = "name";
    public static final String LEFT_ANGLE_BRACKET = "<";
    public static final String RIGHT_ANGLE_BRACKET = ">";

    /**
     * Describes the format of the body part.
     */
    public enum BodyPartForm {
        INPUTSTREAM, FILE
    }
}
