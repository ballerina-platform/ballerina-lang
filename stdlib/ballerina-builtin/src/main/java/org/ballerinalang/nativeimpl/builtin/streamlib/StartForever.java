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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.StreamingRuntimeManager;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code Start} is the function to start the forever runtime.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "startForever",
        args = {
                @Argument(name = "streamQuery", type = TypeKind.STRING),
                @Argument(name = "inStreamRef", type = TypeKind.ARRAY),
                @Argument(name = "inTableRef", type = TypeKind.ARRAY),
                @Argument(name = "outStreamRef", type = TypeKind.ARRAY),
                @Argument(name = "outTableRefs", type = TypeKind.ARRAY),
                @Argument(name = "funcPointers", type = TypeKind.ARRAY)
        },
        returnType = {@ReturnType(type = TypeKind.NONE)},
        isPublic = true)
public class StartForever extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        context.setReturnValues();

        String siddhiApp = context.getStringArgument(0);
        SiddhiAppRuntime siddhiAppRuntime =
                StreamingRuntimeManager.getInstance().createSiddhiAppRuntime(siddhiApp);
        Set<String> streamIds = siddhiAppRuntime.getStreamDefinitionMap().keySet();
        Map<String, InputHandler> streamSpecificInputHandlerMap = new HashMap<>();
        for (String streamId : streamIds) {
            streamSpecificInputHandlerMap.put(streamId, siddhiAppRuntime.getInputHandler(streamId));
        }

        BRefValueArray inputStreamReferenceArray = (BRefValueArray) context.getRefArgument(0);
        BRefValueArray functionPointerArray = (BRefValueArray) context.getRefArgument(4);

        for (int i = 0; i < inputStreamReferenceArray.size(); i++) {
            BStream stream = (BStream) inputStreamReferenceArray.get(i);
            InputHandler inputHandler = streamSpecificInputHandlerMap.get(stream.getStreamId());
            stream.subscribe(inputHandler);
        }

        for (int i = 0; i < functionPointerArray.size(); i++) {
            BFunctionPointer functionPointer = (BFunctionPointer) functionPointerArray.get(i);
            String functionName = functionPointer.value().getFunctionName();
            String streamId = "stream" + functionName.replaceAll("\\$", "_");
            StreamingRuntimeManager.getInstance().addCallback(streamId, functionPointer, siddhiAppRuntime);
        }
    }
}
