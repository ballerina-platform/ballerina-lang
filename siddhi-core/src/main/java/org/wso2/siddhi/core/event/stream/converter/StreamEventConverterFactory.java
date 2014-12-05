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
package org.wso2.siddhi.core.event.stream.converter;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class StreamEventConverterFactory {

    public static EventConverter constructEventConverter(MetaStreamEvent metaStreamEvent) {

        int beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        int onAfterWindowDataSize = metaStreamEvent.getOnAfterWindowData().size();
        int size = beforeWindowDataSize + onAfterWindowDataSize + metaStreamEvent.getOutputData().size();

        List<ConversionElement> conversionElements = getConversionElements(metaStreamEvent, size);

        if (beforeWindowDataSize + onAfterWindowDataSize > 0) {
            return new SelectiveStreamEventConverter(conversionElements);
        } else {
            if (metaStreamEvent.getInputDefinition().getAttributeList().size() == conversionElements.size()) {
                Boolean isPassThrough = true;
                for (ConversionElement conversionElement : conversionElements) {
                    if (!(conversionElement.getFromPosition() == conversionElement.getToPosition()[1])) {
                        isPassThrough = false;
                    }
                }
                if (isPassThrough) {
                    return new PassThroughStreamEventConverter();
                }
            }
            return new SimpleStreamEventConverter(conversionElements);
        }
    }

    private static List<ConversionElement> getConversionElements(MetaStreamEvent metaStreamEvent, int size) {

        StreamDefinition defaultDefinition = (StreamDefinition) metaStreamEvent.getInputDefinition();
        List<ConversionElement> conversionElements = new ArrayList<ConversionElement>(size);

        for (int j = 0; j < 3; j++) {
            List<Attribute> currentDataList = null;
            if (j == 0) {
                currentDataList = metaStreamEvent.getBeforeWindowData();
            } else if (j == 1) {
                currentDataList = metaStreamEvent.getOnAfterWindowData();
            } else if (j == 2) {
                currentDataList = metaStreamEvent.getOutputData();
            }
            if (currentDataList != null) {
                int i = 0;
                for (Attribute attribute : currentDataList) {           //Only variable slots will be filled.
                    if (attribute == null) {
                        i++;
                    } else {
                        ConversionElement conversionElement = new ConversionElement();
                        int[] position = new int[2];
                        conversionElement.setFromPosition(defaultDefinition.getAttributePosition(attribute.getName()));
                        position[0] = j;
                        position[1] = i;
                        conversionElement.setToPosition(position);
                        conversionElements.add(conversionElement);
                        i++;
                    }
                }
            }
        }
        return conversionElements;
    }
}
