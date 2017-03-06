/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.output.mapper.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransportCallback;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.namespace.QName;
import java.util.Map;

@Extension(
        name = "xml",
        namespace = "outputmapper",
        description = "Event to XML output mapper"
)
public class XMLOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private static final String EVENTS_PARENT_TAG = "events";
    private static final String EVENT_PARENT_TAG = "event";
    private static final String EVENT_ARBITRARY_DATA_MAP_TAG = "arbitraryDataMap";

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options
     * @param payloadTemplateBuilder Unmapped payload for reference
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Map and publish the given {@link Event} array
     *
     * @param events                  Event object array
     * @param outputTransportCallback output transport callback
     * @param optionHolder            option holder containing static and dynamic options
     * @param payloadTemplateBuilder  Unmapped payload for reference
     */
    @Override
    public void mapAndSend(Event[] events, OutputTransportCallback outputTransportCallback,
                           OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder)
            throws ConnectionUnavailableException {
        if (payloadTemplateBuilder != null) {
            for (Event event : events) {
                outputTransportCallback.publish(payloadTemplateBuilder.build(event), event);
            }
        } else {
            //TODO add support to publish multiple events
            for (Event event : events) {
                outputTransportCallback.publish(constructDefaultMapping(event), event);
            }
        }
    }

    /**
     * Convert the given {@link Event} to XML string
     *
     * @param event Event object
     * @return the constructed XML string
     */
    private Object constructDefaultMapping(Event event) {
        Object[] data = event.getData();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement compositeEventElement = factory.createOMElement(new QName(
                EVENTS_PARENT_TAG));

        OMElement templateEventElement = factory.createOMElement(new QName(EVENT_PARENT_TAG));
        compositeEventElement.addChild(templateEventElement);

        for (int i = 0; i < data.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            Object attributeValue = data[i];

            OMElement propertyElement = factory.createOMElement(new QName(attributeName));
            propertyElement.setText(attributeValue.toString());
            templateEventElement.addChild(propertyElement);
        }

        // TODO: remove if we are not going to support arbitraryDataMap
        // Get arbitrary data from event
/*        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
            // Add arbitrary data map to the default template
            OMElement parentPropertyElement = factory.createOMElement(new QName(EVENT_ARBITRARY_DATA_MAP_TAG));

            for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(entry.getKey()));
                propertyElement.setText(entry.getValue().toString());
                parentPropertyElement.addChild(propertyElement);
            }
            compositeEventElement.getFirstElement().addChild(parentPropertyElement);
        }*/

        return compositeEventElement.toString();
    }


}