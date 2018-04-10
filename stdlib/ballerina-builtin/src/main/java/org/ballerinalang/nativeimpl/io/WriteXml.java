/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Writes XML to a given location.
 *
 * @since ballerina-0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "writeXml",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class WriteXml implements NativeCallableUnit {
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BStruct characterChannelStruct = (BStruct) context.getRefArgument(0);
            BXML content = (BXML) context.getRefArgument(1);
            CharacterChannel characterChannel = (CharacterChannel) characterChannelStruct.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            IOUtils.writeFull(characterChannel, content.stringValue(), eventContext);
            callback.notifySuccess();
        } catch (BallerinaException e) {
            BStruct errorStruct = IOUtils.createError(context, e.getMessage());
            context.setReturnValues(errorStruct);
            callback.notifySuccess();
        }

    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
