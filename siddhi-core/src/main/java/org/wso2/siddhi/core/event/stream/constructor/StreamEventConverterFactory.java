/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.event.stream.constructor;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventFactory;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class StreamEventConverterFactory {
    public static EventConstructor getConverter(MetaStreamEvent metaStreamEvent) {
        StreamDefinition defaultDefinition = (StreamDefinition) metaStreamEvent.getInputDefinition();
        int beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        int onAfterWindowDataSize = metaStreamEvent.getAfterWindowData().size();
        int outputDataSize = metaStreamEvent.getOutputData().size();

        StreamEventFactory eventFactory = new StreamEventFactory(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize);
        int defaultPoolSize = 5;
        StreamEventPool streamEventPool = new StreamEventPool(eventFactory, defaultPoolSize);
        int size = metaStreamEvent.getBeforeWindowData().size() + metaStreamEvent.getAfterWindowData().size() + metaStreamEvent.getOutputData().size();
        List<ConverterElement> converterElements = new ArrayList<ConverterElement>(size);

        for (int j = 0; j < 3; j++) {
            List<Attribute> currentDataList = null;
            if (j == 0) {
                currentDataList = metaStreamEvent.getBeforeWindowData();
            } else if (j == 1) {
                currentDataList = metaStreamEvent.getAfterWindowData();
            } else if (j == 2) {
                currentDataList = metaStreamEvent.getOutputData();
            }
            if (!currentDataList.isEmpty()) {
                int i = 0;
                for (Attribute attribute : currentDataList) {           //Only variable slots will be filled.
                    if (attribute == null) {
                        i++;
                    } else {
                        ConverterElement converterElement = new ConverterElement();
                        int[] position = new int[2];
                        converterElement.setFromPosition(defaultDefinition.getAttributePosition(attribute.getName()));
                        position[0] = j;
                        position[1] = i;
                        converterElement.setToPosition(position);
                        converterElements.add(converterElement);
                        i++;
                    }
                }
            }
        }
        if (beforeWindowDataSize + onAfterWindowDataSize > 0) {
            return new SelectiveStreamEventConstructor(streamEventPool, converterElements);
        } else {
            if (metaStreamEvent.getInputDefinition().getAttributeList().size() == converterElements.size()) {
                Boolean isPassThrough = true;
                for (int k = 0; k < converterElements.size(); k++) {
                    if (!(converterElements.get(k).getFromPosition() == converterElements.get(k).getToPosition()[1])) {
                        isPassThrough = false;
                    }
                }
                if (isPassThrough) {
                    return new PassThroughStreamEventConstructor(streamEventPool);
                }
            }
            return new SimpleStreamEventConstructor(streamEventPool, converterElements);
        }
    }
}
