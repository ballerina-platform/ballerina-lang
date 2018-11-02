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

package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * This class responsible on holding Siddhi App runtimes and related stream objects.
 *
 * @since 0.965.0
 */
public class StreamingRuntimeManager {

    private static StreamingRuntimeManager streamingRuntimeManager;
    private SiddhiManager siddhiManager = new SiddhiManager();
    private List<SiddhiAppRuntime> siddhiAppRuntimeList = new ArrayList<>();

    private StreamingRuntimeManager() {

    }

    public static StreamingRuntimeManager getInstance() {
        if (streamingRuntimeManager != null) {
            return streamingRuntimeManager;
        }
        synchronized (StreamingRuntimeManager.class) {
            if (streamingRuntimeManager == null) {
                streamingRuntimeManager = new StreamingRuntimeManager();
            }
        }
        return streamingRuntimeManager;
    }

    public SiddhiAppRuntime createSiddhiAppRuntime(String siddhiApp) {
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntimeList.add(siddhiAppRuntime);
        siddhiAppRuntime.start();
        return siddhiAppRuntime;
    }


    public void addCallback(String streamId, BFunctionPointer functionPointer, SiddhiAppRuntime siddhiAppRuntime) {

        BType[] parameters = functionPointer.value().getParamTypes();

        // Create an array list with closure var values
        List<BValue> closureArgs = new ArrayList<>();
        for (BClosure closure : functionPointer.getClosureVars()) {
            closureArgs.add(closure.value());
        }

        BStructureType structType = (BStructureType) ((BArrayType) parameters[parameters.length - 1]).getElementType();
        if (!(parameters[parameters.length - 1] instanceof BArrayType)) {
            throw new BallerinaException("incompatible function: inline function needs to be a function accepting"
                    + " an object array");
        }

        siddhiAppRuntime.addCallback(streamId, new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                BRefValueArray outputArray = new BRefValueArray(new BMapType(structType));
                int j = 0;
                for (Event event : events) {
                    // Here it is assumed that an event data will contain all the fields
                    // of the record. Otherwise, some fields will be missing from the record value.
                    BMap<String, BValue> output = new BMap<String, BValue>(structType);
                    BField[] fields = structType.getFields();
                    int i = 0;
                    for (Object field : event.getData()) {
                        if (field instanceof Long || field instanceof Integer) {
                            output.put(fields[i].fieldName, new BInteger(((Number) field).longValue()));
                        } else if (field instanceof Double || field instanceof Float) {
                            output.put(fields[i].fieldName, new BFloat(((Number) field).doubleValue()));
                        } else if (field instanceof Boolean) {
                            output.put(fields[i].fieldName, new BBoolean(((Boolean) field)));
                        } else if (field instanceof String) {
                            output.put(fields[i].fieldName, new BString((String) field));
                        }
                        i++;
                    }
                    outputArray.add(j, output);
                    j++;
                }
                List<BValue> argsList = new ArrayList<>();
                argsList.addAll(closureArgs);
                argsList.add(outputArray);
                BLangFunctions.invokeCallable(functionPointer.value(), argsList.toArray(new BValue[argsList.size()]));
            }
        });
    }
}
