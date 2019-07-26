/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.readers.CharacterChannelReader;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Extern function ballerina/io#readXml.
 *
 * @since 0.971.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "io",
        functionName = "readXml",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableCharacterChannel",
                structPackage = "ballerina/io"),
        isPublic = true
)
public class ReadXml {

    public static Object readXml(Strand strand, ObjectValue channel) {

        CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(IOConstants.CHARACTER_CHANNEL_NAME);
        CharacterChannelReader reader = new CharacterChannelReader(charChannel, new EventContext());
        try {
            return XMLFactory.parse(reader);
        } catch (BallerinaException e) {
            return IOUtils.createError(e.getMessage());
        }
    }
}
