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
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class responsible on holding Siddhi App runtimes and related stream objects.
 *
 * @since 0.965.0
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

    public void registerSubscriberForTopics(Map<String, InputHandler> streamSpecificInputHandlerMap,
                                            String streamIdsAsString) {
        String[] streamIds = streamIdsAsString.trim().split(",");
        for (String streamId : streamIds) {
            BStream streamReference = StreamingRuntimeManager.getInstance().getStreamReference(streamId);
            if (streamReference != null) {
                streamReference.subscribe(streamSpecificInputHandlerMap.get(streamId));
            }
        }
    }

    public void removeSiddhiAppRuntime(SiddhiAppRuntime siddhiAppRuntime) {
        siddhiAppRuntimeList.remove(siddhiAppRuntime);
    }

    public List<SiddhiAppRuntime> getStreamSpecificSiddhiAppRuntimes(String streamId) {
        List<SiddhiAppRuntime> siddhiAppRuntimeList = new ArrayList<>();
        for (SiddhiAppRuntime siddhiAppRuntime : this.siddhiAppRuntimeList) {
            if (siddhiAppRuntime != null && siddhiAppRuntime.getStreamDefinitionMap().get(streamId) != null) {
                siddhiAppRuntimeList.add(siddhiAppRuntime);
            }
        }
        return siddhiAppRuntimeList;
    }

    public void addStreamReference(String streamId, BStream bStream) {
        this.streamMap.put(streamId, bStream);
    }

    public BStream getStreamReference(String streamId) {
        return streamMap.get(streamId);
    }
}
