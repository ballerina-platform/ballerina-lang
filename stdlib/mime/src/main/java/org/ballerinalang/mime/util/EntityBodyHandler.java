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

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import org.ballerinalang.stdlib.io.channels.TempFileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_BODY_PART_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

/**
 * Entity body related operations are included here.
 *
 * @since 0.963.0
 */
public class EntityBodyHandler {

    private static final Logger log = LoggerFactory.getLogger(EntityBodyHandler.class);
    private static final Type MIME_ENTITY_TYPE =
            ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY).getType();
    private static final ArrayType mimeEntityArrayType = TypeCreator.createArrayType(MIME_ENTITY_TYPE);

    /**
     * Get a byte channel for a given text data.
     *
     * @param textPayload Text data that needs to be wrapped in a byte channel
     * @return EntityBodyChannel which represent the given text
     */
    public static EntityWrapper getEntityWrapper(String textPayload) {
        return new EntityWrapper(new EntityBodyChannel(new ByteArrayInputStream(
                textPayload.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Given a temp file location, create a byte channel.
     *
     * @param temporaryFilePath Temporary file path
     * @return ByteChannel which represent the file channel
     */
    public static TempFileIOChannel getByteChannelForTempFile(String temporaryFilePath) {
        FileChannel fileChannel;
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.READ);
        Path path = Paths.get(temporaryFilePath);
        try {
            fileChannel = (FileChannel) Files.newByteChannel(path, options);
        } catch (IOException e) {
            throw IOUtils.createError(IOConstants.ErrorCode.GenericError,
                                              "Error occurred while creating a file channel from a temporary file");
        }
        return new TempFileIOChannel(fileChannel, temporaryFilePath);
    }

    /**
     * Get the message data source associated with a given entity.
     *
     * @param entityObj Represent a ballerina entity
     * @return MessageDataSource which represent the entity body in memory
     */
    public static Object getMessageDataSource(BObject entityObj) {
        return entityObj.getNativeData(MESSAGE_DATA_SOURCE);
    }

    /**
     * Since JSON is a union of multiple data types. There is no specific data source for JSON. Hence use this method to
     * add JSON data source which tracks the JSON type using a flag.
     *
     * @param entityObj         Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addJsonMessageDataSource(BObject entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, true);
    }

    /**
     * Associate a given message data source with a given entity.
     *
     * @param entityObj      Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addMessageDataSource(BObject entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, false);
    }

    private static void setParseJsonAndDataSource(BObject entityObj, Object messageDataSource, boolean json) {
        /* specifies whether the type of the datasource is json. This is necessary because json is a union of
         * different data types and is not a single data type.*/
        entityObj.addNativeData(MimeConstants.PARSE_AS_JSON, json);
        entityObj.addNativeData(MESSAGE_DATA_SOURCE, messageDataSource);
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return Data source for binary data which is kept in memory
     * @throws IOException In case an error occurred while creating blob data source
     */
    public static BArray constructBlobDataSource(BObject entityObj) throws IOException {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return (BArray) ValueCreator.createArrayValue(new byte[0]);
        }
        try {
            return constructBlobDataSource(byteChannel.getInputStream());
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param inputStream Represent the input stream
     * @return Data source for binary data which is kept in memory
     */
    public static BArray constructBlobDataSource(InputStream inputStream) {
        byte[] byteData;
        try {
            byteData = MimeUtil.getByteArray(inputStream);
        } catch (IOException ex) {
            throw ErrorCreator.createError(StringUtils.fromString(("Error occurred while reading input stream :" +
                    ex.getMessage())));
        }
        return (BArray) ValueCreator.createArrayValue(byteData);
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BJSON data source which is kept in memory
     */
    public static Object constructJsonDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return null;
        }
        try {
            return constructJsonDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString((e.getMessage())));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct JsonDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return BJSON data source which is kept in memory
     */
    public static Object constructJsonDataSource(BObject entity, InputStream inputStream) {
        Object jsonData;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entity, CONTENT_TYPE);
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                jsonData = JSONParser.parse(inputStream, charsetValue);
            } else {
                jsonData = JSONParser.parse(inputStream);
            }
        } else {
            jsonData = JSONParser.parse(inputStream);
        }
        return jsonData;
    }

    /**
     * Construct XML data source from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BXML data source which is kept in memory
     */
    public static BXML constructXmlDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw ErrorCreator.createError(StringUtils.fromString(("Empty xml payload")));
        }
        try {
            return constructXmlDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString((e.getMessage())));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct XML data source from the given input stream.
     *
     * @param entityObj Represent an entity object
     * @param inputStream  Represent the input stream
     * @return BXML data source which is kept in memory
     */
    public static BXML constructXmlDataSource(BObject entityObj, InputStream inputStream) {
        BXML xmlContent;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entityObj, CONTENT_TYPE);
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                xmlContent = XMLFactory.parse(inputStream, charsetValue);
            } else {
                xmlContent = XMLFactory.parse(inputStream);
            }
        } else {
            xmlContent = XMLFactory.parse(inputStream);
        }
        return xmlContent;
    }

    /**
     * Construct StringDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static BString constructStringDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw ErrorCreator.createError(StringUtils.fromString(("String payload is null")));
        }
        try {
            return constructStringDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString((e.getMessage())));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct StringDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static BString constructStringDataSource(BObject entity, InputStream inputStream) {
        BString textContent;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entity, CONTENT_TYPE);
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                textContent = StringUtils.getStringFromInputStream(inputStream, charsetValue);
            } else {
                textContent = StringUtils.getStringFromInputStream(inputStream);
            }
        } else {
            textContent = StringUtils.getStringFromInputStream(inputStream);
        }
        return textContent;
    }

    /**
     * Check whether the entity body is present. Entity body can either be a byte channel, fully constructed
     * message data source or a set of body parts.
     *
     * @param entityObj Represent an 'Entity'
     * @return a boolean indicating entity body availability
     */
    public static boolean checkEntityBodyAvailability(BObject entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null || getMessageDataSource(entityObj) != null
                || entityObj.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Check whether the streaming is required as data source should be constructed using byte channel if entity
     * contains body parts or byte channel.
     *
     * @param entity Represent an 'Entity'
     * @return a boolean indicating the streaming requirement
     */
    public static boolean isStreamingRequired(BObject entity) {
        return entity.getNativeData(ENTITY_BYTE_CHANNEL) != null || entity.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Set ballerina body parts to it's top level entity.
     *
     * @param entity    Represent top level message's entity
     * @param bodyParts Represent ballerina body parts
     */
    static void setPartsToTopLevelEntity(BObject entity, ArrayList<BObject> bodyParts) {
        if (!bodyParts.isEmpty()) {
            ObjectType typeOfBodyPart = bodyParts.get(FIRST_BODY_PART_INDEX).getType();
            BObject[] result = bodyParts.toArray(new BObject[bodyParts.size()]);
            BArray partsArray = (BArray) ValueCreator
                    .createArrayValue(result, TypeCreator.createArrayType(typeOfBodyPart));
            entity.addNativeData(BODY_PARTS, partsArray);
        }
    }

    /**
     * Populate ballerina body parts with actual body content. Based on the memory threshhold body part's inputstream
     * can either come from memory or from a temp file maintained by mimepull library.
     *
     * @param bodyPart Represent ballerina body part
     * @param mimePart Represent decoded mime part
     */
    public static void populateBodyContent(BObject bodyPart, MIMEPart mimePart) {
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new MimeEntityWrapper(new EntityBodyChannel(mimePart.readOnce()),
                mimePart));
    }

    /**
     * Write byte channel stream directly into outputstream without converting it to a data source.
     *
     * @param entityObj        Represent a ballerina entity
     * @param messageOutputStream Represent the outputstream that the message should be written to
     * @throws IOException When an error occurs while writing inputstream to outputstream
     */
    public static void writeByteChannelToOutputStream(BObject entityObj,
                                                      OutputStream messageOutputStream)
            throws IOException {
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
        if (byteChannel != null) {
            MimeUtil.writeInputToOutputStream(byteChannel.getInputStream(), messageOutputStream);
            byteChannel.close();
            //Set the byte channel to null, once it is consumed
            entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
        }
    }

    /**
     * Decode a given entity body to get a set of child parts and set them to parent entity's multipart data field.
     *
     * @param entityObj   Parent entity that the nested parts reside
     * @param byteChannel Represent ballerina specific byte channel
     * @throws IOException When an error occurs while getting inputstream
     */
    public static void decodeEntityBody(BObject entityObj, Channel byteChannel) throws IOException {
        String contentType = MimeUtil.getContentTypeWithParameters(entityObj);
        if (!isNotNullAndEmpty(contentType) || !contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            return;
        }
        try {
            MultipartDecoder.parseBody(entityObj, contentType, byteChannel.getInputStream());
        } catch (IOException e) {
            throw new IOException("Unable to get a byte channel input stream to decode entity body", e);
        }
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param entityObj Represent a ballerina entity
     * @return An array of body parts
     */
    public static BArray getBodyPartArray(BObject entityObj) {
        return entityObj.getNativeData(BODY_PARTS) != null ? (BArray) entityObj.getNativeData(BODY_PARTS)
                : (BArray) ValueCreator.createArrayValue(mimeEntityArrayType, 0);
    }

    public static Channel getByteChannel(BObject entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null ? (Channel) entityObj.getNativeData
                (ENTITY_BYTE_CHANNEL) : null;
    }

    private static void closeByteChannel(Channel byteChannel) {
        try {
            byteChannel.close();
        } catch (IOException e) {
            log.error("Error occurred while closing byte channel", e);
        }
    }
}
