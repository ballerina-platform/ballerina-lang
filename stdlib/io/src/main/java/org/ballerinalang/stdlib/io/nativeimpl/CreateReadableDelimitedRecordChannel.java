/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function ballerina/io#createDelimitedRecordChannel.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "ReadableTextRecordChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "charChannel", type = TypeKind.OBJECT, structType = "ReadableCharacterChannel",
                structPackage = "ballerina/io"),
                @Argument(name = "recordSeparator", type = TypeKind.STRING),
                @Argument(name = "fieldSeparator", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.OBJECT, structType = "ReadableRecordChannel",
                structPackage = "ballerina/io"),
                @ReturnType(type = TypeKind.RECORD, structType = "Error", structPackage = "ballerina/io")},
        isPublic = true
)
public class CreateReadableDelimitedRecordChannel {

    private static final Logger log = LoggerFactory.getLogger(CreateReadableDelimitedRecordChannel.class);

    /**
     * Default format type.
     */
    private static final String DEFAULT = "default";

    public static void init(Strand strand, ObjectValue textRecordChannel, ObjectValue characterChannelInfo,
            String fieldSeparator, String recordSeparator, String format) {
        try {
            //Will get the relevant byte channel and will create a character channel
            CharacterChannel characterChannel = (CharacterChannel) characterChannelInfo
                    .getNativeData(IOConstants.CHARACTER_CHANNEL_NAME);
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
            throw new BallerinaException(message, e);
        }
    }
}
