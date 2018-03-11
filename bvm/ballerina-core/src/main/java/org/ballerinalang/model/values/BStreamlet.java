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

package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BStreamletType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;

import java.util.Map;

/**
 * The {@code BStreamlet} represents a Streamlet in Ballerina.
 *
 * @since @since 0.955.0
 */
public final class BStreamlet implements BRefType {

    private String siddhiApp;
    private String streamIdsAsString;
    private BStreamletType streamletType;

    private Map<String, InputHandler> streamSpecificInputHandlerMap;
    private SiddhiAppRuntime siddhiAppRuntime;

    public BStreamlet(BType streamletType) {
        this.streamletType = (BStreamletType) streamletType;
    }

    public BType getStreamletType() {
        return streamletType;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }

    public String getStreamIdsAsString() {
        return streamIdsAsString;
    }

    public void setStreamIdsAsString(String streamIdsAsString) {
        this.streamIdsAsString = streamIdsAsString;
    }

    public SiddhiAppRuntime getSiddhiAppRuntime() {
        return siddhiAppRuntime;
    }

    public void setSiddhiAppRuntime(SiddhiAppRuntime siddhiAppRuntime) {
        this.siddhiAppRuntime = siddhiAppRuntime;
    }

    public Map<String, InputHandler> getStreamSpecificInputHandlerMap() {
        return streamSpecificInputHandlerMap;
    }

    public void setStreamSpecificInputHandlerMap(Map<String, InputHandler> streamSpecificInputHandlerMap) {
        this.streamSpecificInputHandlerMap = streamSpecificInputHandlerMap;
    }

    public void stopRuntime() {
        siddhiAppRuntime.shutdown();
    }

    @Override
    public BStreamlet value() {
        return null;
    }

    @Override
    public String stringValue() {
        return "";
    }

    @Override
    public BType getType() {
        return BTypes.typeStreamlet;
    }

    @Override
    public BValue copy() {
        return null;
    }

}
