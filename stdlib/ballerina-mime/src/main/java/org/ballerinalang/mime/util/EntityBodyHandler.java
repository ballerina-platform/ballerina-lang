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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
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
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.FIRST_BODY_PART_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.READABLE_BUFFER_SIZE;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;

/**
 * Entity body related operations are included here.
 *
 * @since 0.963.0
 */
public class EntityBodyHandler {

    /**
     * Handle discrete media type content. This method populates ballerina entity with a byte channel from a given
     * inputstream. If the payload size exceeds 2MB limit, write the stream to a temp file and get a reference to
     * a file channel. After that delete the temp file. If the size does not exceed, then wrap the inputstream with an
     * EntityBodyChannel.
     *
     * @param entityStruct Represent an 'Entity'
     * @param inputStream  Represent input stream coming from the request/response
     */
    public static void setDiscreteMediaTypeBodyContent(BStruct entityStruct, InputStream inputStream) {
        long contentLength = entityStruct.getIntField(SIZE_INDEX);
        if (contentLength > Constants.BYTE_LIMIT) {
            String temporaryFilePath = MimeUtil.writeToTemporaryFile(inputStream, BALLERINA_TEMP_FILE);
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, getByteChannelForTempFile(temporaryFilePath));
        } else {
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(new EntityBodyChannel(inputStream)));
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
    public static FileIOChannel getByteChannelForTempFile(String temporaryFilePath) {
        FileChannel fileChannel;
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.READ);
        Path path = Paths.get(temporaryFilePath);
        try {
            fileChannel = (FileChannel) Files.newByteChannel(path, options);
            Files.delete(path);
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while creating a file channel from a temporary file");
        }
        return new FileIOChannel(fileChannel, IOConstants.CHANNEL_BUFFER_SIZE);
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
        EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
        if (entityBody != null) {
            if (entityBody.isStream()) {
                return new BlobDataSource(MimeUtil.getByteArray(entityBody.getEntityWrapper().getInputStream()));
            } else {
                FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
                return new BlobDataSource(MimeUtil.getByteArray(fileIOChannel.getInputStream()));
            }
        }
        return null;
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return BJSON data source which is kept in memory
     */
    public static BJSON constructJsonDataSource(BStruct entityStruct) {
        EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
        if (entityBody != null) {
            if (entityBody.isStream()) {
                return new BJSON(entityBody.getEntityWrapper().getInputStream());
            } else {
                FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
                return new BJSON(fileIOChannel.getInputStream());
            }
        }
        return null;
    }

    /**
     * Construct XMl data source from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return BXML data source which is kept in memory
     */
    public static BXML constructXmlDataSource(BStruct entityStruct) {
        EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
        if (entityBody != null) {
            if (entityBody.isStream()) {
                return XMLUtils.parse(entityBody.getEntityWrapper().getInputStream());
            } else {
                FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
                return XMLUtils.parse(fileIOChannel.getInputStream());
            }
        }
        return null;
    }

    /**
     * Construct StringDataSource from the underneath byte channel which is associated with the entity struct.
     *
     * @param entityStruct Represent an entity struct
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static StringDataSource constructStringDataSource(BStruct entityStruct) {
        EntityBody entityBodyReader = MimeUtil.constructEntityBody(entityStruct);
        if (entityBodyReader != null) {
            String textContent;
            if (entityBodyReader.isStream()) {
                textContent = StringUtils.getStringFromInputStream(entityBodyReader.getEntityWrapper()
                        .getInputStream());
                return new StringDataSource(textContent);
            } else {
                FileIOChannel fileIOChannel = entityBodyReader.getFileIOChannel();
                textContent = StringUtils.getStringFromInputStream(fileIOChannel.getInputStream());
                return new StringDataSource(textContent);
            }
        }
        return null;
    }

    /**
     * Check whether the entity body is present. Entity body can either be a byte channel or fully constructed
     * message data source.
     *
     * @param entityStruct Represent an 'Entity'
     * @return a boolean indicating entity body availability
     */
    public static boolean checkEntityBodyAvailability(BStruct entityStruct) {
        return entityStruct.getNativeData(ENTITY_BYTE_CHANNEL) != null || getMessageDataSource(entityStruct) != null;
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
            entity.setRefField(MULTIPART_DATA_INDEX, partsArray);
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
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(new EntityBodyChannel(mimePart.readOnce())));
        mimePart.close(); //Clean up temp files
    }

    /**
     * Given a channel as Object, convert it to correct channel type and get the high level representation of
     * EntityBody.
     *
     * @param channel Channel as an object
     * @return Channel wrapped as entity body
     */
    public static EntityBody getEntityBody(Object channel) {
        EntityBody entityBody = null;
        if (channel != null) {
            if (channel instanceof EntityWrapper) {
                entityBody = new EntityBody((EntityWrapper) channel, true);
            } else if (channel instanceof EntityBodyChannel) {
                entityBody = new EntityBody(new EntityWrapper((EntityBodyChannel) channel), true);
            } else if (channel instanceof FileIOChannel) {
                entityBody = new EntityBody((FileIOChannel) channel, false);
            } else if (channel instanceof FileChannel) {
                entityBody = new EntityBody(new FileIOChannel((FileChannel) channel,
                        IOConstants.CHANNEL_BUFFER_SIZE), false);
            }
        }
        return entityBody;
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
        EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
        if (entityBody != null) {
            InputStream inputStream;
            if (entityBody.isStream()) {
                inputStream = entityBody.getEntityWrapper().getInputStream();
            } else {
                FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
                inputStream = fileIOChannel.getInputStream();
            }
            writeInputToOutputStream(messageOutputStream, inputStream);
        }
    }

    /**
     * Write a given inputstream to a given outputstream.
     *
     * @param messageOutputStream Represent the outputstream that the inputstream should be written to
     * @param inputStream         Represent the inputstream that that needs to be written to outputstream
     * @throws IOException When an error occurs while writing inputstream to outputstream
     */
    public static void writeInputToOutputStream(OutputStream messageOutputStream, InputStream inputStream) throws
            IOException {
        byte[] buffer = new byte[READABLE_BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            messageOutputStream.write(buffer, 0, len);
        }
    }

    /**
     * Decode a given entity body to get a set of child parts and set them to parent entity's multipart data field.
     *
     * @param context      Represent the ballerina context
     * @param entityStruct Parent entity that the nested parts reside
     * @param entityBody   Represent entity body which hold that actual content in a channel
     */
    public static void decodeEntityBody(Context context, BStruct entityStruct, EntityBody entityBody) {
        if (entityBody == null) {
            return;
        }
        String contentType = MimeUtil.getContentTypeWithParameters(entityStruct);
        if (!MimeUtil.isNotNullAndEmpty(contentType) || !contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            return;
        }
        if (entityBody.isStream()) {
            if (entityBody.getEntityWrapper() != null) {
                MultipartDecoder.parseBody(context, entityStruct, contentType, entityBody.getEntityWrapper().
                        getInputStream());
            }
        } else {
            FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
            if (fileIOChannel != null) {
                MultipartDecoder.parseBody(context, entityStruct, contentType, fileIOChannel.getInputStream());
            }
        }
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param entityStruct Represent a ballerina entity
     * @return An array of body parts
     */
    public static BRefValueArray getBodyPartArray(BStruct entityStruct) {
        return entityStruct.getRefField(MULTIPART_DATA_INDEX) != null ?
                (BRefValueArray) entityStruct.getRefField(MULTIPART_DATA_INDEX) : null;
    }
}
