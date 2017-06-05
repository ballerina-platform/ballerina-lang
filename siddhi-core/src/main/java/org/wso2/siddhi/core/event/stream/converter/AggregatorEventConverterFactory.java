/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

public class AggregatorEventConverterFactory {

    public static StreamEventConverter constructEventConverter(MetaStreamEvent metaStreamEvent, MetaStreamEvent originalMetaStreamEvent) {

        int beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        int onAfterWindowDataSize = metaStreamEvent.getOnAfterWindowData().size();
        int size = beforeWindowDataSize + onAfterWindowDataSize + metaStreamEvent.getOutputData().size();
        List<StreamEventConverter.ConversionMapping> conversionMappings = getConversionElements(metaStreamEvent, originalMetaStreamEvent, size);

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
            MetaStreamEvent metaStreamEvent, MetaStreamEvent originalMetaStreamEvent, int size) {

//        AbstractDefinition inputDefinition = metaStreamEvent.getInputDefinitions().get(0);
        AbstractDefinition originalInputDefinition = originalMetaStreamEvent.getLastInputDefinition();
        List<StreamEventConverter.ConversionMapping> conversionMappings = new ArrayList<StreamEventConverter.ConversionMapping>(size);

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
                    } else if (!originalInputDefinition.getAttributeList().contains(attribute)) {
                        if (attribute.getName().startsWith("sum")) {
                            Attribute tempAttribute =
                                    // TODO: 6/2/17 have a pattern to get base name
                                    new Attribute(attribute.getName().replaceFirst("sum",""), attribute.getType());
                            if (!originalInputDefinition.getAttributeList().contains(tempAttribute)) {
                                i++;
                            } else {
                                int fromPosition = originalInputDefinition.getAttributePosition(tempAttribute.getName());
                                StreamEventConverter.ConversionMapping conversionMapping = new StreamEventConverter.ConversionMapping();
                                conversionMapping.setFromPosition(fromPosition);
                                int[] toPosition = new int[2];
                                toPosition[0] = j;
                                toPosition[1] = i;
                                conversionMapping.setToPosition(toPosition);
                                conversionMappings.add(conversionMapping);
                                i++;
                            }
                        } else if (attribute.getName().startsWith("count")){
                            Attribute tempAttribute =
                                    new Attribute(attribute.getName().replaceFirst("count",""), attribute.getType());
                            if (!originalInputDefinition.getAttributeList().contains(tempAttribute)) {
                                i++;
                            } else {// TODO: 6/2/17 get this from composite
                                int fromPosition = -1; //If from position is -1 in convertData method of SelectiveStreamEventConverter, set value to 1
                                StreamEventConverter.ConversionMapping conversionMapping = new StreamEventConverter.ConversionMapping();
                                conversionMapping.setFromPosition(fromPosition);
                                int[] toPosition = new int[2];
                                toPosition[0] = j;
                                toPosition[1] = i;
                                conversionMapping.setToPosition(toPosition);
                                conversionMappings.add(conversionMapping);
                                i++;
                            }
                        } else {
                            i++; // TODO: 6/1/17 include other else conditions for min, max, etc
                        }
                    } else {
                            int fromPosition = originalInputDefinition.getAttributePosition(attribute.getName());
                            StreamEventConverter.ConversionMapping conversionMapping = new StreamEventConverter.ConversionMapping();
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
