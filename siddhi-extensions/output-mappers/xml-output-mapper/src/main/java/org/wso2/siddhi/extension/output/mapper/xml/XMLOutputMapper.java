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
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.namespace.QName;
import java.util.Map;
@Extension(
        name = "xml",
        namespace = "outputmapper",
        description = ""
)
public class XMLOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private static final String EVENTS_PARENT_TAG = "events";
    private static final String EVENT_PARENT_TAG = "event";
    private static final String EVENT_ARBITRARY_DATA_MAP_TAG = "arbitraryDataMap";
    private static final String EVENT_ATTRIBUTE_SEPARATOR = ",";

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param options                Additional mapping options
     * @param unmappedDynamicOptions Unmapped dynamic options
     */
    @Override
    public void init(StreamDefinition streamDefinition, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Convert the given {@link Event} to XML string
     *
     * @param event          Event object
     * @param dynamicOptions Dynamic options
     * @return the constructed XML string in following format:
     * <events><event><AttributeName>AttributeValue</AttributeName></event></events>
     */
    //todo should we support namespaces for output mapping?
    @Override
    public Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions) {
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

        // Get arbitrary data from event
        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
            // Add arbitrary data map to the default template
            OMElement parentPropertyElement = factory.createOMElement(new QName(EVENT_ARBITRARY_DATA_MAP_TAG));

            for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(entry.getKey()));
                propertyElement.setText(entry.getValue().toString());
                parentPropertyElement.addChild(propertyElement);
            }
            compositeEventElement.getFirstElement().addChild(parentPropertyElement);
        }

        return compositeEventElement.toString();
    }

    /**
     * Convert the given Event mapping to XML string
     *
     * @param event            Event object
     * @param mappedAttributes Event mapping string array
     * @param dynamicOptions   Dynamic options
     * @return the mapped XML string
     */
    @Override
    public Object convertToMappedInputEvent(Event event, String[] mappedAttributes, Map<String, String> dynamicOptions) {
        StringBuilder eventText = new StringBuilder();
        for (int i = 0; i < mappedAttributes.length; i++) {
            String mappedAttribute = mappedAttributes[i];
            eventText.append(mappedAttribute).append(EVENT_ATTRIBUTE_SEPARATOR).append("\n");
        }
        eventText.deleteCharAt(eventText.lastIndexOf(EVENT_ATTRIBUTE_SEPARATOR));
        eventText.deleteCharAt(eventText.lastIndexOf("\n"));
        return eventText.toString();
    }
}