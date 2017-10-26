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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.TextRecordChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#readTextRecords.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readTextRecord",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "TextRecordChannel", structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class ReadTextRecord extends AbstractNativeFunction {
    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        BStringArray record;
        try {
            channel = (BStruct) getRefArgument(context, BYTE_CHANNEL_INDEX);

            TextRecordChannel textRecordChannel = (TextRecordChannel) channel.getNativeData(IOConstants
                    .TXT_RECORD_CHANNEL_NAME);
            String[] recordValue = textRecordChannel.read();
            record = new BStringArray(recordValue);
        } catch (Throwable e) {
            String message = "Error occurred while reading text records. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return getBValues(record);
    }
}
