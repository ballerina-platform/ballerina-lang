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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.characters.CloseCharacterChannelEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Native function ballerina.io#closeCharacterChannel.
 *
 * @since 0.95
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "io",
        functionName = "closeCharacterChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "io"),
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "io")},
        isPublic = true
)
public class CloseCharacterChannel extends AbstractNativeFunction {

    /**
     * The index of the CharacterChannel in ballerina.io#closeCharacterChannel().
     */
    private static final int CHARACTER_CHANNEL_INDEX = 0;

    private static final Logger log = LoggerFactory.getLogger(CloseCharacterChannel.class);

    /**
     * <p>
     * Closes a character channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct errorStruct = null;
        try {
            BStruct channel = (BStruct) getRefArgument(context, CHARACTER_CHANNEL_INDEX);
            CharacterChannel charChannel = (CharacterChannel) channel.getNativeData(IOConstants.CHARACTER_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            CloseCharacterChannelEvent closeEvent = new CloseCharacterChannelEvent(charChannel, eventContext);
            CompletableFuture<EventResult> future = EventManager.getInstance().publish(closeEvent);
            EventResult eventResult = future.get();
            Throwable error = ((EventContext) eventResult.getContext()).getError();
            if (null != error) {
                throw new ExecutionException(error);
            }
        } catch (Throwable e) {
            String message = "Failed to close the character channel:" + e.getMessage();
            log.error(message, e);
            errorStruct = IOUtils.createError(context, message);
        }
        return getBValues(errorStruct);
    }
}
