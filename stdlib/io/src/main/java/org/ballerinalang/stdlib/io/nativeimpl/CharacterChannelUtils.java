/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.readers.CharacterChannelReader;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.stdlib.io.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.CHARACTER_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *CharacterChannels.
 *
 * @since 1.1.0
 */
public class CharacterChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(CharacterChannelUtils.class);

    private CharacterChannelUtils() {
    }

    public static void initCharacterChannel(BObject characterChannel, BObject byteChannelInfo,
                                            BString encoding) {
        try {
            Channel byteChannel = (Channel) byteChannelInfo.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            CharacterChannel bCharacterChannel = new CharacterChannel(byteChannel, encoding.getValue());
            characterChannel.addNativeData(CHARACTER_CHANNEL_NAME, bCharacterChannel);
        } catch (Exception e) {
            String message = "error occurred while converting byte channel to character channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static Object read(BObject channel, long numberOfCharacters) {
        CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        if (characterChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                return StringUtils
                        .fromString(characterChannel.read((int) numberOfCharacters));
            } catch (BallerinaIOException e) {
                log.error("error occurred while reading characters.", e);
                return IOUtils.createError(e);
            }
        }
    }

    public static Object readJson(BObject channel) {
        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        CharacterChannelReader reader = new CharacterChannelReader(charChannel);
        try {
            Object returnValue = JSONParser.parse(reader, JSONParser.NonStringValueProcessingMode.FROM_JSON_STRING);
            if (returnValue instanceof String) {

                return StringUtils.fromString((String) returnValue);
            }
            return returnValue;
        } catch (BallerinaException e) {
            log.error("unable to read json from character channel", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readXml(BObject channel) {
        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        CharacterChannelReader reader = new CharacterChannelReader(charChannel);
        try {
            return XMLFactory.parse(reader);
        } catch (BallerinaException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readProperty(BObject channel, BString key, BString defaultValue) {
        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        CharacterChannelReader reader = new CharacterChannelReader(charChannel);
        try {
            return PropertyUtils.readProperty(reader, key, defaultValue, Integer.toString(charChannel.id()));
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object readAllProperties(BObject channel) {
        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        CharacterChannelReader reader = new CharacterChannelReader(charChannel);
        try {
            return PropertyUtils.readAllProperties(reader, Integer.toString(charChannel.id()));
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object close(BObject channel) {
        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        try {
            charChannel.close();
        } catch (ClosedChannelException e) {
            return IOUtils.createError("channel already closed.");
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object write(BObject channel, BString content, long startOffset) {
        CharacterChannel characterChannel = (CharacterChannel) channel.getNativeData(CHARACTER_CHANNEL_NAME);
        try {
            return characterChannel.write(content.getValue(), (int) startOffset);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
    }

    public static Object writeJson(BObject characterChannelObj, Object content) {
        try {
            CharacterChannel characterChannel = (CharacterChannel) characterChannelObj
                    .getNativeData(CHARACTER_CHANNEL_NAME);
            IOUtils.writeFull(characterChannel, StringUtils.getJsonString(content));
        } catch (BallerinaIOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeXml(BObject characterChannelObj, BXML content) {
        try {
            CharacterChannel characterChannel = (CharacterChannel) characterChannelObj
                    .getNativeData(CHARACTER_CHANNEL_NAME);
            IOUtils.writeFull(characterChannel, content.toString());
        } catch (BallerinaIOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeProperties(BObject characterChannelObj,
                                         BMap<BString, BString> propertyMap, BString comment) {
        try {
            CharacterChannel characterChannel = (CharacterChannel) characterChannelObj
                    .getNativeData(CHARACTER_CHANNEL_NAME);
            PropertyUtils.writePropertyContent(characterChannel, propertyMap, comment);
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }
}
