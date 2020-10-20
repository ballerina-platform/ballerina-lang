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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.TXT_RECORD_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *CSVChannel/*RTextRecordChannel.
 *
 * @since 1.1.0
 */
public class RecordChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(RecordChannelUtils.class);
    private static final String DEFAULT = "default";

    private RecordChannelUtils() {
    }

    public static void initRecordChannel(BObject textRecordChannel, BObject characterChannelInfo,
                                         BString fieldSeparator, BString recordSeparator, BString format) {
        try {
            //Will get the relevant byte channel and will create a character channel
            CharacterChannel characterChannel = (CharacterChannel) characterChannelInfo
                    .getNativeData(IOConstants.CHARACTER_CHANNEL_NAME);
            DelimitedRecordChannel delimitedRecordChannel;
            if (DEFAULT.equals(format.getValue())) {
                delimitedRecordChannel = new DelimitedRecordChannel(characterChannel, recordSeparator.getValue(),
                                                                    fieldSeparator.getValue());
            } else {
                delimitedRecordChannel = new DelimitedRecordChannel(characterChannel,
                                                                    Format.valueOf(format.getValue()));
            }
            textRecordChannel.addNativeData(TXT_RECORD_CHANNEL_NAME, delimitedRecordChannel);
        } catch (Exception e) {
            String message =
                    "error occurred while converting character channel to textRecord channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static boolean hasNext(BObject channel) {
        Object textChannel = channel.getNativeData(TXT_RECORD_CHANNEL_NAME);
        if (textChannel == null) {
            return false;
        }
        DelimitedRecordChannel textRecordChannel = (DelimitedRecordChannel) textChannel;
        if (!textRecordChannel.hasReachedEnd()) {
            try {
                return textRecordChannel.hasNext();
            } catch (BallerinaIOException e) {
                String msg = "error occurred while checking hasNext on ReadableTextRecordChannel: " + e.getMessage();
                log.error(msg, e);
                throw IOUtils.createError(msg);
            }
        }
        return false;
    }

    public static Object getNext(BObject channel) {
        DelimitedRecordChannel textRecordChannel =
                (DelimitedRecordChannel) channel.getNativeData(TXT_RECORD_CHANNEL_NAME);
        if (textRecordChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                String[] records = textRecordChannel.read();
                return ValueCreator.createArrayValue(StringUtils.fromStringArray(records));
            } catch (BallerinaIOException e) {
                log.error("error occurred while reading next text record from ReadableTextRecordChannel", e);
                return IOUtils.createError(e);
            }
        }
    }

    public static Object write(BObject channel, BArray content) {
        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel
                .getNativeData(TXT_RECORD_CHANNEL_NAME);
        try {
            delimitedRecordChannel.write(content.getStringArray());
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object close(BObject channel) {
        DelimitedRecordChannel recordChannel = (DelimitedRecordChannel) channel.getNativeData(TXT_RECORD_CHANNEL_NAME);
        try {
            recordChannel.close();
        } catch (ClosedChannelException e) {
            return IOUtils.createError("channel already closed.");
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }
}
