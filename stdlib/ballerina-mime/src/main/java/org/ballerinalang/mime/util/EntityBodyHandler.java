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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.TempFileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.jvnet.mimepull.MIMEPart;

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

import static org.ballerinalang.mime.util.Constants.BALLERINA_TEMP_FILE;
import static org.ballerinalang.mime.util.Constants.BODY_PARTS;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.Constants.FIRST_BODY_PART_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;

/**
 * Entity body related operations are included here.
 *
 * @since 0.963.0
 */
public class EntityBodyHandler {

    /**
     * Set new entity to in/out request/response struct.
     *
     * @param context ballerina context.
     * @param struct  request/response struct.
     */
    public static BStruct createNewEntity(Context context, BStruct struct) {
        BStruct entity = ConnectorUtils.createAndGetStruct(context
                , org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME
                , org.ballerinalang.mime.util.Constants.ENTITY);
        entity.addNativeData(ENTITY_HEADERS, new DefaultHttpHeaders());
        entity.addNativeData(ENTITY_BYTE_CHANNEL, null);
        struct.addNativeData(MESSAGE_ENTITY, entity);
        struct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
        return entity;
    }

    /**
     * Handle discrete media type content. This method populates ballerina entity with a byte channel from a given
     * inputstream. If the payload size exceeds 2MB limit, write the stream to a temp file and get a reference to
     * a file channel. After that delete the temp file. If the size does not exceed, then wrap the inputstream with an
     * EntityBodyChannel.
     *
     * @param entityStruct      Represent an 'Entity'
     * @param inputStream       Represent input stream coming from the request/response
     * @param numberOfBytesRead Number of bytes read
     */
    public static void setDiscreteMediaTypeBodyContent(BStruct entityStruct, InputStream inputStream,
                                                       int numberOfBytesRead) {
        if (numberOfBytesRead < Constants.BYTE_LIMIT) {
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(new EntityBodyChannel(inputStream)));
        } else {
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_TEMP_FILE);
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, getByteChannelForTempFile(temporaryFilePath));
        }
    }

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
            throw new BallerinaException("Error occurred while creating a file channel from a temporary file");
        }
        return new TempFileIOChannel(fileChannel, IOConstants.CHANNEL_BUFFER_SIZE, temporaryFilePath);
    }

    /**
     * Get the message data source associated with a given entity.
     *
     * @param entityStruct Represent a ballerina entity
     * @return MessageDataSource which represent the entity body in memory
     */
    public static MessageDataSource getMessageDataSource(BStruct entityStruct) {
        return entityStruct.getNativeData(MESSAGE_DATA_SOURCE) != null ? (MessageDataSource) entityStruct.getNativeData
                (MESSAGE_DATA_SOURCE) : null;
    }

    /**
     * Associate a given message data source with a given entity.
     *
     * @param entityStruct      Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addMessageDataSource(BStruct entityStruct, MessageDataSource messageDataSource) {
        entityStruct.addNativeData(MESSAGE_DATA_SOURCE, messageDataSource);
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return BlobDataSource Data source for binary data which is kept in memory
     * @throws IOException In case an error occurred while creating blob data source
     */
    public static BlobDataSource constructBlobDataSource(BStruct entityStruct) throws IOException {
        Channel byteChannel = getByteChannel(entityStruct);
        if (byteChannel == null) {
            return null;
        }
        byte[] byteData = MimeUtil.getByteArray(byteChannel.getInputStream());
        byteChannel.close();
        return new BlobDataSource(byteData);
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return BJSON data source which is kept in memory
     */
    public static BJSON constructJsonDataSource(BStruct entityStruct) {
        try {
            Channel byteChannel = getByteChannel(entityStruct);
            if (byteChannel == null) {
                return null;
            }
            BJSON jsonData = new BJSON(byteChannel.getInputStream());
            byteChannel.close();
            return jsonData;
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing connection", e);
        }
    }

    /**
     * Construct XMl data source from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return BXML data source which is kept in memory
     */
    public static BXML constructXmlDataSource(BStruct entityStruct) {
        try {
            Channel byteChannel = getByteChannel(entityStruct);
            if (byteChannel == null) {
                throw new BallerinaIOException("Empty xml payload");
            }
            BXML xmlContent = XMLUtils.parse(byteChannel.getInputStream());
            byteChannel.close();
            return xmlContent;
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing the channel", e);
        }
    }

    /**
     * Construct StringDataSource from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static StringDataSource constructStringDataSource(BStruct entityStruct) {
        try {
            Channel byteChannel = getByteChannel(entityStruct);
            if (byteChannel == null) {
                throw new BallerinaIOException("Payload is null");
            }
            String textContent = StringUtils.getStringFromInputStream(byteChannel.getInputStream());
            byteChannel.close();
            return new StringDataSource(textContent);
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing the channel", e);
        }
    }

    /**
     * Check whether the entity body is present. Entity body can either be a byte channel, fully constructed
     * message data source or a set of body parts.
     *
     * @param entityStruct Represent an 'Entity'
     * @return a boolean indicating entity body availability
     */
    public static boolean checkEntityBodyAvailability(BStruct entityStruct) {
        return entityStruct.getNativeData(ENTITY_BYTE_CHANNEL) != null || getMessageDataSource(entityStruct) != null
                || entityStruct.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Set ballerina body parts to it's top level entity.
     *
     * @param entity    Represent top level message's entity
     * @param bodyParts Represent ballerina body parts
     */
    static void setPartsToTopLevelEntity(BStruct entity, ArrayList<BStruct> bodyParts) {
        if (!bodyParts.isEmpty()) {
            BStructType typeOfBodyPart = bodyParts.get(FIRST_BODY_PART_INDEX).getType();
            BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
            BRefValueArray partsArray = new BRefValueArray(result, typeOfBodyPart);
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
    public static void populateBodyContent(BStruct bodyPart, MIMEPart mimePart) {
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new MimeEntityWrapper(new EntityBodyChannel(mimePart.readOnce()),
                mimePart));
    }

    /**
     * Write byte channel stream directly into outputstream without converting it to a data source.
     *
     * @param entityStruct        Represent a ballerina entity
     * @param messageOutputStream Represent the outputstream that the message should be written to
     * @throws IOException When an error occurs while writing inputstream to outputstream
     */
    public static void writeByteChannelToOutputStream(BStruct entityStruct, OutputStream messageOutputStream)
            throws IOException {
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityStruct);
        if (byteChannel != null) {
            MimeUtil.writeInputToOutputStream(byteChannel.getInputStream(), messageOutputStream);
            byteChannel.close();
            //Set the byte channel to null, once it is consumed
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
        }
    }

    /**
     * Decode a given entity body to get a set of child parts and set them to parent entity's multipart data field.
     *
     * @param context      Represent the ballerina context
     * @param entityStruct Parent entity that the nested parts reside
     * @param byteChannel  Represent ballerina specific byte channel
     */
    public static void decodeEntityBody(Context context, BStruct entityStruct, Channel byteChannel) {
        String contentType = MimeUtil.getContentTypeWithParameters(entityStruct);
        if (!MimeUtil.isNotNullAndEmpty(contentType) || !contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            return;
        }

        MultipartDecoder.parseBody(context, entityStruct, contentType, byteChannel.getInputStream());
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param entityStruct Represent a ballerina entity
     * @return An array of body parts
     */
    public static BRefValueArray getBodyPartArray(BStruct entityStruct) {
        return entityStruct.getNativeData(BODY_PARTS) != null ?
                (BRefValueArray) entityStruct.getNativeData(BODY_PARTS) : new BRefValueArray();
    }

    public static Channel getByteChannel(BStruct entityStruct) {
        return entityStruct.getNativeData(ENTITY_BYTE_CHANNEL) != null ? (Channel) entityStruct.getNativeData
                (ENTITY_BYTE_CHANNEL) : null;
    }
}
