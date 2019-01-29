/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function ballerina/io#createWritableDelimitedRecordChannel.
 *
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "WritableTextRecordChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "channel", type = TypeKind.OBJECT, structType = "WritableCharacterChannel",
                structPackage = "ballerina/io"),
                @Argument(name = "recordSeparator", type = TypeKind.STRING),
                @Argument(name = "fieldSeparator", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.OBJECT, structType = "WritableRecordChannel",
                structPackage = "ballerina/io"),
                @ReturnType(type = TypeKind.RECORD, structType = "IOError", structPackage = "ballerina/io")},
        isPublic = true
)
public class CreateWritableDelimitedRecordChannel extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(CreateWritableDelimitedRecordChannel.class);

    /**
     * The index od the text record channel in ballerina/io#createDelimitedRecordChannel().
     */
    private static final int CHAR_CHANNEL_INDEX = 1;
    /**
     * Specifies the index of the record channel.
     */
    private static final int RECORD_CHANNEL_INDEX = 0;
    /**
     * The index of the record channel separator in ballerina/io#createDelimitedRecordChannel().
     */
    private static final int RECORD_SEPARATOR_INDEX = 1;
    /**
     * The index of the field separator in ballerina/io#createDelimitedRecordChannel().
     */
    private static final int FIELD_SEPARATOR_INDEX = 0;
    /**
     * Represents the format of the record.
     */
    private static final int FORMAT_INDEX = 2;
    /**
     * Default format type.
     */
    private static final String DEFAULT = "default";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        try {
            // File which holds access to the channel information
            BMap<String, BValue> characterChannelInfo =
                    (BMap<String, BValue>) context.getRefArgument(CHAR_CHANNEL_INDEX);
            String recordSeparator = context.getStringArgument(RECORD_SEPARATOR_INDEX);
            String fieldSeparator = context.getStringArgument(FIELD_SEPARATOR_INDEX);
            String format = context.getStringArgument(FORMAT_INDEX);
            BMap<String, BValue> textRecordChannel =
                    (BMap<String, BValue>) context.getRefArgument(RECORD_CHANNEL_INDEX);
            //Will get the relevant byte channel and will create a character channel
            CharacterChannel characterChannel = (CharacterChannel) characterChannelInfo.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            DelimitedRecordChannel delimitedRecordChannel;
            if (DEFAULT.equals(format)) {
                delimitedRecordChannel = new DelimitedRecordChannel(characterChannel, recordSeparator, fieldSeparator);
            } else {
                delimitedRecordChannel = new DelimitedRecordChannel(characterChannel, Format.valueOf(format));
            }
            textRecordChannel.addNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME, delimitedRecordChannel);
        } catch (Throwable e) {
            String message =
                    "Error occurred while converting character channel to textRecord channel:" + e.getMessage();
            log.error(message, e);
            throw new BallerinaIOException(message, e);
        }
    }
}
