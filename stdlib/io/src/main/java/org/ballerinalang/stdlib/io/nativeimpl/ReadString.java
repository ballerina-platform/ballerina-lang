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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.data.ReadStringEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina.io#readString.
 *
 * @since 0.974.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "readString",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableDataChannel",
                structPackage = "ballerina.io"),
        args = {@Argument(name = "nBytes", type = TypeKind.INT),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        isPublic = true
)
public class ReadString implements NativeCallableUnit {
    /**
     * Represents data channel.
     */
    private static final int DATA_CHANNEL_INDEX = 0;
    /**
     * Represents the number of bytes.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 0;
    /**
     * Represents the encoding index.
     */
    private static final int ENCODING_INDEX = 0;

    /**
     * Triggers upon receiving the response.
     *
     * @param result the response received after reading int.
     * @return read int value.
     */
    private static EventResult readResponse(EventResult<String, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        CallableUnitCallback callback = eventContext.getCallback();
        if (null != error) {
            BMap<String, BValue> errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            String readStr = result.getResponse();
            context.setReturnValues(new BString(readStr));
        }
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> dataChannelStruct = (BMap<String, BValue>) context.getRefArgument(DATA_CHANNEL_INDEX);
        long nBytes = context.getIntArgument(NUMBER_OF_BYTES_INDEX);
        String encoding = context.getStringArgument(ENCODING_INDEX);
        DataChannel channel = (DataChannel) dataChannelStruct.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        ReadStringEvent event = new ReadStringEvent(channel, eventContext, (int) nBytes, encoding);
        Register register = EventRegister.getFactory().register(event, ReadString::readResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
