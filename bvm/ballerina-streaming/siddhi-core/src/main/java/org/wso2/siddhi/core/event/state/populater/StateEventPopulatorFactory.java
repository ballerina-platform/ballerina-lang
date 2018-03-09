/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.event.state.populater;

import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.MetaStateEventAttribute;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The StateEventPopulaterFactory that constructs StateEventPopulater according to MetaStateEvent
 */
public class StateEventPopulatorFactory {
    /**
     * Constructs StateEventPopulator according to MetaStateEvent
     *
     * @param metaComplexEvent info for populating the StateEvents
     * @return StateEventPopulator
     */
    public static StateEventPopulator constructEventPopulator(MetaComplexEvent metaComplexEvent) {
        if (metaComplexEvent instanceof MetaStreamEvent) {
            return new SkipStateEventPopulator();
        } else {
            List<StateMappingElement> stateMappingElements = getMappingElements((MetaStateEvent) metaComplexEvent);
            return new SelectiveStateEventPopulator(stateMappingElements);
        }
    }

    private static List<StateMappingElement> getMappingElements(MetaStateEvent metaStateEvent) {

        List<StateMappingElement> stateMappingElements = new ArrayList<StateMappingElement>(
                metaStateEvent.getOutputDataAttributes().size());

        List<MetaStateEventAttribute> currentDataList = metaStateEvent.getOutputDataAttributes();
        if (currentDataList != null) {
            int i = 0;
            for (MetaStateEventAttribute metaStateEventAttribute : currentDataList) {           //Only variable slots
                // will be filled.
                if (metaStateEventAttribute == null) {
                    i++;
                } else {
                    StateMappingElement stateMappingElement = new StateMappingElement();
                    stateMappingElement.setFromPosition(metaStateEventAttribute.getPosition());
                    int toPosition = i;
                    stateMappingElement.setToPosition(toPosition);
                    stateMappingElements.add(stateMappingElement);
                    i++;
                }
            }
        }
        return stateMappingElements;
    }
}
