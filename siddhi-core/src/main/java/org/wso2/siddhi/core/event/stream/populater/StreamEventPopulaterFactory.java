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

package org.wso2.siddhi.core.event.stream.populater;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.siddhi.core.util.SiddhiConstants.BEFORE_WINDOW_DATA_INDEX;
import static org.wso2.siddhi.core.util.SiddhiConstants.ON_AFTER_WINDOW_DATA_INDEX;
import static org.wso2.siddhi.core.util.SiddhiConstants.OUTPUT_DATA_INDEX;

/**
 * The StateEventPopulaterFactory that populates StreamEventPopulater according to MetaStreamEvent and to be mapped
 * attributes
 */
public class StreamEventPopulaterFactory {
    /**
     * Constructs StreamEventPopulater according to MetaStateEvent and to be mapped attributes
     *
     * @param metaStreamEvent       info for populating the StreamEvent
     * @param streamEventChainIndex StreamEvent chain index
     * @param attributes            mapped attributes
     * @return StateEventPopulater
     */
    public static ComplexEventPopulater constructEventPopulator(MetaStreamEvent metaStreamEvent, int
            streamEventChainIndex, List<Attribute> attributes) {

        List<StreamMappingElement> streamMappingElements = new ArrayList<StreamMappingElement>();
        for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++) {
            Attribute attribute = attributes.get(i);
            StreamMappingElement streamMappingElement = new StreamMappingElement();
            streamMappingElement.setFromPosition(i);
            int index = metaStreamEvent.getOutputData().indexOf(attribute);
            if (index > -1) {
                streamMappingElement.setToPosition(new int[]{streamEventChainIndex, 0, OUTPUT_DATA_INDEX, index});
            } else {
                index = metaStreamEvent.getOnAfterWindowData().indexOf(attribute);
                if (index > -1) {
                    streamMappingElement.setToPosition(new int[]{streamEventChainIndex, 0,
                            ON_AFTER_WINDOW_DATA_INDEX, index});
                } else {
                    index = metaStreamEvent.getBeforeWindowData().indexOf(attribute);
                    if (index > -1) {
                        streamMappingElement.setToPosition(new int[]{streamEventChainIndex, 0,
                                BEFORE_WINDOW_DATA_INDEX, index});
                    } else {
                        streamMappingElement.setToPosition(null);
                    }
                }
            }
            streamMappingElements.add(streamMappingElement);
        }

        return new SelectiveComplexEventPopulater(streamMappingElements);

    }

}
