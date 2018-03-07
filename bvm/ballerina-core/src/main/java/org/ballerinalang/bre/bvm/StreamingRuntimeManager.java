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

import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BStreamlet;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class responsible on holding Siddhi App runtimes and related stream objects.
 */
public class StreamingRuntimeManager {

    private static StreamingRuntimeManager streamingRuntimeManager;
    private SiddhiManager siddhiManager = new SiddhiManager();
    private List<SiddhiAppRuntime> siddhiAppRuntimeList = new ArrayList<>();
    private Map<String, BStream> streamMap = new HashMap<>();

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


    public void createSiddhiAppRuntime(BStreamlet streamlet) {
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streamlet.getSiddhiApp());
        Set<String> streamIds = siddhiAppRuntime.getStreamDefinitionMap().keySet();
        Map<String, InputHandler> streamSpecificInputHandlerMap = new HashMap<>();
        for (String streamId : streamIds) {
            streamSpecificInputHandlerMap.put(streamId, siddhiAppRuntime.getInputHandler(streamId));
        }

        streamlet.setStreamSpecificInputHandlerMap(streamSpecificInputHandlerMap);
        streamlet.setSiddhiAppRuntime(siddhiAppRuntime);
        siddhiAppRuntimeList.add(siddhiAppRuntime);
        siddhiAppRuntime.start();
    }

    public List<InputHandler> getStreamSpecificInputHandlerList(String streamId) {
        List<InputHandler> inputHandlerList = new ArrayList<>();
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeList) {
            if (siddhiAppRuntime != null) {
                InputHandler inputHandler = siddhiAppRuntime.getInputHandler(streamId);
                inputHandlerList.add(inputHandler);
            }
        }
        return inputHandlerList;
    }

    public List<SiddhiAppRuntime> getSiddhiAppRuntimeList() {
        return siddhiAppRuntimeList;
    }

    public void addStreamReference(String streamId, BStream bStream) {
        this.streamMap.put(streamId, bStream);
    }

    public BStream getStreamReference(String streamId) {
        return streamMap.get(streamId);
    }
}
