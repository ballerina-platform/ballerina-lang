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
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        BType[] parameters = functionPointer.value().getFunctionInfo().getParamTypes();
        // TODO: 4/6/18 extract all parameters from closure and pass them to the callback
        BStream stream = (BStream) (functionPointer.getClosureVars().get(0)).value();
        BStructType structType = (BStructType) ((BArrayType) parameters[parameters.length - 1]).getElementType();
        if (!(parameters[parameters.length - 1] instanceof BArrayType)) {
            throw new BallerinaException("incompatible function: inline function needs to be a function accepting"
                    + " an object array");
        }

        siddhiAppRuntime.addCallback(streamId, new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    AtomicInteger intVarIndex = new AtomicInteger(-1);
                    AtomicInteger floatVarIndex = new AtomicInteger(-1);
                    AtomicInteger boolVarIndex = new AtomicInteger(-1);
                    AtomicInteger stringVarIndex = new AtomicInteger(-1);
                    BStruct output = new BStruct(structType);
                    for (Object field : event.getData()) {
                        if (field instanceof Long) {
                            output.setIntField(intVarIndex.incrementAndGet(), (Long) field);
                        } else if (field instanceof Double) {
                            output.setFloatField(floatVarIndex.incrementAndGet(), (Double) field);
                        } else if (field instanceof Boolean) {
                            output.setBooleanField(boolVarIndex.incrementAndGet(), (Integer) field);
                        } else if (field instanceof String) {
                            output.setStringField(stringVarIndex.incrementAndGet(), (String) field);
                        }
                    }
                    BValue[] args = {stream, output};
                    BLangFunctions.invokeCallable(functionPointer.value().getFunctionInfo(), args);
                }
            }
        });
    }
}
