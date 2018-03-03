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

import org.ballerinalang.model.values.BStreamlet;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Siddhi Runtime generation factory class.
 */
public class SiddhiRuntimeFactory {

    private static SiddhiRuntimeFactory siddhiRuntimeFactory;
    private SiddhiManager siddhiManager = new SiddhiManager();

    public static SiddhiRuntimeFactory getInstance() {
        if (siddhiRuntimeFactory == null) {
            siddhiRuntimeFactory = new SiddhiRuntimeFactory();
        }
        return siddhiRuntimeFactory;
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
        siddhiAppRuntime.start();
    }


}
