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
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
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
import java.util.HashSet;
import java.util.Iterator;
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
        isPublic = false)
public class StartForever extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        context.setReturnValues();

        StringBuilder streamDefinitionQuery = new StringBuilder();
        String siddhiQuery = context.getStringArgument(0);
        BRefValueArray inputStreamReferenceArray = (BRefValueArray) context.getRefArgument(0);

        for (int i = 0; i < inputStreamReferenceArray.size(); i++) {
            BStream stream = (BStream) inputStreamReferenceArray.get(i);
            siddhiQuery = siddhiQuery.replaceFirst("\\[\\[streamName\\]\\]", stream.getStreamId());

            Map<String, BField> structFields = ((BStructureType) stream.getConstraintType()).getFields();
            StringBuilder streamDefinition = new StringBuilder("define stream ");
            streamDefinition.append(stream.getStreamId()).append("( ");
            generateStreamDefinition(structFields, streamDefinition);
            streamDefinitionQuery.append(streamDefinition).append("\n ");
        }

        SiddhiAppRuntime siddhiAppRuntime =
                StreamingRuntimeManager.getInstance().createSiddhiAppRuntime(streamDefinitionQuery + siddhiQuery);
        Set<String> streamIds = siddhiAppRuntime.getStreamDefinitionMap().keySet();
        Map<String, InputHandler> streamSpecificInputHandlerMap = new HashMap<>();
        for (String streamId : streamIds) {
            streamSpecificInputHandlerMap.put(streamId, siddhiAppRuntime.getInputHandler(streamId));
        }

        Set<String> alreadySubscribedStreams = new HashSet<>();
        for (int i = 0; i < inputStreamReferenceArray.size(); i++) {
            BStream stream = (BStream) inputStreamReferenceArray.get(i);
            if (!alreadySubscribedStreams.contains(stream.getStreamId())) {
                InputHandler inputHandler = streamSpecificInputHandlerMap.get(stream.getStreamId());
                stream.subscribe(inputHandler);
                alreadySubscribedStreams.add(stream.getStreamId());
            }
        }

        BRefValueArray functionPointerArray = (BRefValueArray) context.getRefArgument(4);
        for (int i = 0; i < functionPointerArray.size(); i++) {
            BFunctionPointer functionPointer = (BFunctionPointer) functionPointerArray.get(i);
            String functionName = functionPointer.value().getName();
            String streamId = "stream" + functionName.replaceAll("\\$", "_");
            StreamingRuntimeManager.getInstance().addCallback(streamId, functionPointer, siddhiAppRuntime);
        }
    }

    private void generateStreamDefinition(Map<String, BField> structFields,
                                          StringBuilder streamDefinition) {
        Iterator<BField> fieldIterator = structFields.values().iterator();

        if (!fieldIterator.hasNext()) {
            return;
        }

        addTypesToStreamDefinitionQuery(streamDefinition, fieldIterator.next());

        fieldIterator.forEachRemaining(field -> {
            streamDefinition.append(" , ");
            addTypesToStreamDefinitionQuery(streamDefinition, field);
        });

        streamDefinition.append(" ); ");
    }

    private void addTypesToStreamDefinitionQuery(StringBuilder streamDefinition, BField structField) {
        streamDefinition.append(structField.fieldName).append(" ");
        String type = structField.fieldType.toString();
        //even though, type defined as int, actual value is a long. To handle this case in Siddhi, type is defined
        //as long.
        if (type.equalsIgnoreCase("int")) {
            type = "long";
        } else if (type.equalsIgnoreCase("float")) {
            type = "double";
        } else if (type.equalsIgnoreCase("boolean")) {
            type = "bool";
        }
        streamDefinition.append(type);
    }
}
