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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#closeTextRecordChannel.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "closeDelimitedRecordChannel",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedRecordChannel",
                structPackage = "ballerina.io"),
        isPublic = true
)
public class CloseDelimitedRecordChannel extends BlockingNativeCallableUnit {

    /**
     * The index of the DelimitedRecordChannel in ballerina.io#closeDelimitedRecordChannel().
     */
    private static final int RECORD_CHANNEL_INDEX = 0;

    /**
     * <p>
     * Closes a text record channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct channel;
        try {
            channel = (BStruct) context.getRefArgument(RECORD_CHANNEL_INDEX);
            DelimitedRecordChannel charChannel = (DelimitedRecordChannel)
                    channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            charChannel.close();
        } catch (Throwable e) {
            String message = "Failed to close the text record channel:" + e.getMessage();
            throw new BallerinaException(message, context);
        }
        context.setReturnValues();
    }
}
