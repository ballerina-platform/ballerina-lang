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
package org.wso2.siddhi.core.event.stream.converter;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory to construct {@link StreamEventConverter}
 */
public class StreamEventConverterFactory {

    public static StreamEventConverter constructEventConverter(MetaStreamEvent metaStreamEvent) {

        int beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        int onAfterWindowDataSize = metaStreamEvent.getOnAfterWindowData().size();
        int size = beforeWindowDataSize + onAfterWindowDataSize + metaStreamEvent.getOutputData().size();

        List<StreamEventConverter.ConversionMapping> conversionMappings = getConversionElements(metaStreamEvent, size);

        if (beforeWindowDataSize + onAfterWindowDataSize > 0) {
            return new SelectiveStreamEventConverter(conversionMappings);
        } else {
            if (metaStreamEvent.getLastInputDefinition().getAttributeList().size() == conversionMappings.size()) {
                Boolean isPassThrough = true;
                for (StreamEventConverter.ConversionMapping conversionMapping : conversionMappings) {
                    if (!(conversionMapping.getFromPosition() == conversionMapping.getToPosition()[1])) {
                        isPassThrough = false;
                    }
                }
                if (isPassThrough) {
                    return new ZeroStreamEventConverter();
                }
            }
            return new SimpleStreamEventConverter(conversionMappings);
        }
    }

    private static List<StreamEventConverter.ConversionMapping> getConversionElements(
            MetaStreamEvent metaStreamEvent, int size) {

        AbstractDefinition inputDefinition = metaStreamEvent.getInputDefinitions().get(0);
        List<StreamEventConverter.ConversionMapping> conversionMappings = new ArrayList<StreamEventConverter
                .ConversionMapping>(size);

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
                    } else if (!inputDefinition.getAttributeList().contains(attribute)) {
                        i++;
                    } else {
                        int fromPosition = inputDefinition.getAttributePosition(attribute.getName());
                        StreamEventConverter.ConversionMapping conversionMapping = new StreamEventConverter
                                .ConversionMapping();
                        conversionMapping.setFromPosition(fromPosition);
                        int[] toPosition = new int[2];
                        toPosition[0] = j;
                        toPosition[1] = i;
                        conversionMapping.setToPosition(toPosition);
                        conversionMappings.add(conversionMapping);
                        i++;
                    }
                }
            }
        }
        return conversionMappings;
    }
}
