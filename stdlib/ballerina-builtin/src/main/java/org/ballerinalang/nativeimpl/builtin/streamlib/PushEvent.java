/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.nativeimpl.builtin.streamlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStreamlet;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.wso2.siddhi.core.stream.input.InputHandler;

/**
 * {@code PushEvent} is the function to push event to a streamlet through stream.
 *
 * @since 0.963.0
 */
@BallerinaFunction(packageName = "ballerina.builtin",
        functionName = "pushEvent",
        args = {
                @Argument(name = "st",
                        type = TypeKind.ANY),
                @Argument(name = "streamId",
                        type = TypeKind.STRING),
                @Argument(name = "eventBody",
                        type = TypeKind.ANY),
        })

public class PushEvent extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStreamlet streamlet = (BStreamlet) getRefArgument(context, 0);
        String streamId = getStringArgument(context, 0);
        BRefValueArray eventReferenceArray = (BRefValueArray) getRefArgument(context, 1);
        Object[] eventObjectArray = new Object[(int) eventReferenceArray.size()];
        long arraySize = eventReferenceArray.size();

        for (int i = 0; i < arraySize; i++) {
            eventObjectArray[i] = eventReferenceArray.get(i).value();
        }

        InputHandler inputHandler = streamlet.getStreamSpecificInputHandlerMap().get(streamId);
        try {
            inputHandler.send(eventObjectArray);
        } catch (InterruptedException e) {
            System.out.printf("Exception when pushing events to Siddhi stream handler ", e);
        }
        return new BValue[]{};
    }
}
