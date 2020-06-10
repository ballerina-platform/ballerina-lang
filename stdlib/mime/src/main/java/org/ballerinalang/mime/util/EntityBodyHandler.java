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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
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
    private static final BType MIME_ENTITY_TYPE =
            BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY).getType();
    private static final BArrayType mimeEntityArrayType = new BArrayType(MIME_ENTITY_TYPE);

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
            throw IOUtils.createDistinctError(IOConstants.ErrorCode.GenericError,
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
    public static Object getMessageDataSource(ObjectValue entityObj) {
        return entityObj.getNativeData(MESSAGE_DATA_SOURCE);
    }

    /**
     * Since JSON is a union of multiple data types. There is no specific data source for JSON. Hence use this method to
     * add JSON data source which tracks the JSON type using a flag.
     *
     * @param entityObj         Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addJsonMessageDataSource(ObjectValue entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, true);
    }

    /**
     * Associate a given message data source with a given entity.
     *
     * @param entityObj      Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addMessageDataSource(ObjectValue entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, false);
    }

    private static void setParseJsonAndDataSource(ObjectValue entityObj, Object messageDataSource, boolean json) {
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
    public static ArrayValue constructBlobDataSource(ObjectValue entityObj) throws IOException {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return new ArrayValueImpl(new byte[0]);
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
    public static ArrayValue constructBlobDataSource(InputStream inputStream) {
        byte[] byteData;
        try {
            byteData = MimeUtil.getByteArray(inputStream);
        } catch (IOException ex) {
            throw BallerinaErrors.createError("Error occurred while reading input stream :" + ex.getMessage());
        }
        return new ArrayValueImpl(byteData);
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BJSON data source which is kept in memory
     */
    public static Object constructJsonDataSource(ObjectValue entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return null;
        }
        try {
            return constructJsonDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw BallerinaErrors.createError(e.getMessage());
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
    public static Object constructJsonDataSource(ObjectValue entity, InputStream inputStream) {
        Object jsonData;
        String contentTypeValue = HeaderUtil.getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
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
    public static XMLValue constructXmlDataSource(ObjectValue entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw BallerinaErrors.createError("Empty xml payload");
        }
        try {
            return constructXmlDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw BallerinaErrors.createError(e.getMessage());
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
    public static XMLValue constructXmlDataSource(ObjectValue entityObj, InputStream inputStream) {
        XMLValue xmlContent;
        String contentTypeValue = HeaderUtil.getHeaderValue(entityObj, HttpHeaderNames.CONTENT_TYPE.toString());
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
    public static BString constructStringDataSource(ObjectValue entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw BallerinaErrors.createError("String payload is null");
        }
        try {
            return constructStringDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw BallerinaErrors.createError(e.getMessage());
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
    public static BString constructStringDataSource(ObjectValue entity, InputStream inputStream) {
        BString textContent;
        String contentTypeValue = HeaderUtil.getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
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
    public static boolean checkEntityBodyAvailability(ObjectValue entityObj) {
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
    public static boolean isStreamingRequired(ObjectValue entity) {
        return entity.getNativeData(ENTITY_BYTE_CHANNEL) != null || entity.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Set ballerina body parts to it's top level entity.
     *
     * @param entity    Represent top level message's entity
     * @param bodyParts Represent ballerina body parts
     */
    static void setPartsToTopLevelEntity(ObjectValue entity, ArrayList<ObjectValue> bodyParts) {
        if (!bodyParts.isEmpty()) {
            BObjectType typeOfBodyPart = bodyParts.get(FIRST_BODY_PART_INDEX).getType();
            ObjectValue[] result = bodyParts.toArray(new ObjectValue[bodyParts.size()]);
            ArrayValue partsArray = new ArrayValueImpl(result, new BArrayType(typeOfBodyPart));
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
    public static void populateBodyContent(ObjectValue bodyPart, MIMEPart mimePart) {
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
    public static void writeByteChannelToOutputStream(ObjectValue entityObj,
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
    public static void decodeEntityBody(ObjectValue entityObj, Channel byteChannel) throws IOException {
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
    public static ArrayValue getBodyPartArray(ObjectValue entityObj) {
        return entityObj.getNativeData(BODY_PARTS) != null ? (ArrayValue) entityObj.getNativeData(BODY_PARTS)
                : (ArrayValue) BValueCreator.createArrayValue(mimeEntityArrayType, 0);
    }

    public static Channel getByteChannel(ObjectValue entityObj) {
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
