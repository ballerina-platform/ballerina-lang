/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.event.state.populater;

import org.wso2.siddhi.core.event.state.MetaStateEventAttribute;
import org.wso2.siddhi.core.event.state.MetaStateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The StateEventPopulaterFactory that constructs StateEventPopulater according to MetaStateEvent
 */
public class StateEventPopulaterFactory {
    /**
     * Constructs StateEventPopulater according to MetaStateEvent
     *
     * @param metaStateEvent info for populating the StateEvents
     * @return StateEventPopulater
     */
    public static StateEventPopulater constructEventPopulator(MetaStateEvent metaStateEvent) {
        if (metaStateEvent.getStreamEventCount() == 1) {
            return new SkipStateEventPopulater();
        } else {
            int preOutputDataSize = metaStateEvent.getPreOutputDataAttributes().size();
            int outputDataSize = metaStateEvent.getOutputDataAttributes().size();
            int size = preOutputDataSize + outputDataSize;

            List<MappingElement> mappingElements = getConversionElements(metaStateEvent, size);

            return new SelectiveStateEventPopulater(mappingElements);
        }
    }

    private static List<MappingElement> getConversionElements(MetaStateEvent metaStateEvent, int size) {

        List<MappingElement> mappingElements = new ArrayList<MappingElement>(size);

        for (int j = 0; j < 2; j++) {
            List<MetaStateEventAttribute> currentDataList = null;
            if (j == 0) {
                currentDataList = metaStateEvent.getPreOutputDataAttributes();
            } else if (j == 1) {
                currentDataList = metaStateEvent.getOutputDataAttributes();
            }
            if (currentDataList != null) {
                int i = 0;
                for (MetaStateEventAttribute metaStateEventAttribute : currentDataList) {           //Only variable slots will be filled.
                    if (metaStateEventAttribute == null) {
                        i++;
                    } else {
                        MappingElement mappingElement = new MappingElement();
                        mappingElement.setFromPosition(metaStateEventAttribute.getPosition());
                        int[] toPosition = new int[2];
                        toPosition[0] = j;
                        toPosition[1] = i;
                        mappingElement.setToPosition(toPosition);
                        mappingElements.add(mappingElement);
                        i++;
                    }
                }
            }
        }
        return mappingElements;
    }
}
