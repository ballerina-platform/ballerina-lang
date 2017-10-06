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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.BCharacterChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#readCharacters
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readCharacters",
        args = {@Argument(name = "channel", type = TypeKind.STRUCT),
                @Argument(name = "numberOfChars", type = TypeKind.INT)
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ReadCharacters extends AbstractNativeFunction {

    /**
     * Reads characters from the channel
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {

        BStruct channel;
        long numberOfCharacters;
        BString content;

        try {
            channel = (BStruct) getRefArgument(context, 0);
            numberOfCharacters = getIntArgument(context, 0);
            BCharacterChannel characterChannel = (BCharacterChannel) channel.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            String readBytes = characterChannel.read((int) numberOfCharacters);
            content = new BString(readBytes);

        } catch (Throwable e) {
            String message = "Error occurred while reading characters. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }

        return getBValues(content);
    }
}
