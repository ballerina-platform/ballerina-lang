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
package org.wso2.siddhi.extension.input.mapper.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.InputMapper;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This mapper converts XML string input to {@link ComplexEventChunk}. This extension accepts optional xpath expressions to
 * select specific attributes from the stream.
 */
@Extension(
        name = "xml",
        namespace = "inputmapper",
        description = ""
)
public class XmlInputMapper extends InputMapper {

    private static final String PARENT_SELECTOR_XPATH = "parentSelector";
    private static final String NAMESPACES = "namespaces";
    private static final String EVENTS_PARENT_ELEMENT = "events";

    /**
     * Indicates whether custom mapping is enabled or not.
     */
    private boolean isCustomMappingEnabled = false;
    private StreamDefinition streamDefinition;
    private String parentSelectorXPath;
    private Map<String, String> namespaceMap;
    private Map<String, AXIOMXPath> xPathMap = new HashMap<>();

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition     the  StreamDefinition
     * @param optionHolder         mapping options
     * @param attributeMappingList list of attributes mapping
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping>
            attributeMappingList) {
        this.streamDefinition = streamDefinition;
        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            isCustomMappingEnabled = true;
            if (streamDefinition.getAttributeList().size() != attributeMappingList.size()) {
                throw new ExecutionPlanValidationException("Stream: '" + streamDefinition.getId() + "' has "
                        + streamDefinition.getAttributeList().size() + " attributes, but " + attributeMappingList.size()
                        + " attribute mappings found. Each attribute should have one and only one mapping.");
            }

            String namespaces = optionHolder.validateAndGetStaticValue(NAMESPACES, null);
            if (namespaces != null) {
                buildNamespaceMap(namespaces);
            }

            for (AttributeMapping attributeMapping : attributeMappingList) {
                AXIOMXPath axiomxPath;
                try {
                    axiomxPath = new AXIOMXPath(attributeMapping.getMapping());
                } catch (JaxenException e) {
                    throw new ExecutionPlanValidationException("Error occurred when building XPath from: " +
                            attributeMapping.getMapping() + ", mapped to attribute: " + attributeMapping.getRename());
                }
                for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                    try {
                        axiomxPath.addNamespace(entry.getKey(), entry.getValue());
                    } catch (JaxenException e) {
                        throw new ExecutionPlanValidationException("Error occurred when adding namespace: " + entry.getKey()
                                + ":" + entry.getValue() + " to XPath element: " + attributeMapping.getMapping());
                    }
                }
                xPathMap.put(attributeMapping.getRename(), axiomxPath);
            }
            parentSelectorXPath = optionHolder.validateAndGetStaticValue(PARENT_SELECTOR_XPATH, null);
        }
    }

    /**
     * Receives an event as an XML string from {@link InputTransport}, converts it to a {@link ComplexEventChunk}
     * and send to the {@link OutputCallback}.
     *
     * @param eventObject  the input event, given as an XML string
     * @param inputHandler input handler
     */
    @Override
    protected void mapAndProcess(Object eventObject, InputHandler inputHandler) throws InterruptedException {
        if (eventObject != null) {
            synchronized (this) {
                for (Event event : convertToEvents(eventObject)) {
                    inputHandler.send(event);
                }
            }
        }
    }

    /**
     * Converts an event from an XML string to {@link Event}
     *
     * @param eventObject The input event, given as an XML string
     * @return the constructed {@link Event} object
     */
    private List<Event> convertToEvents(Object eventObject) {
        // Validate the event
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Could not map the input event as the given event is null");
        }

        if (!(eventObject instanceof String)) {
            throw new ExecutionPlanRuntimeException("Event object is of invalid type. Expected String, but found " +
                    eventObject.getClass()
                            .getCanonicalName());
        }

        OMElement parentOMElement;
        String inputEventStr = (String) eventObject;
        try {
            parentOMElement = AXIOMUtil.stringToOM(inputEventStr);
        } catch (XMLStreamException e) {
            throw new ExecutionPlanRuntimeException("Error parsing incoming XML event : " + inputEventStr
                    + ". Reason: " + e.getMessage(), e);
        }

        List<Event> eventList = new ArrayList<>();

        if (isCustomMappingEnabled) {   //custom mapping case
            if (parentSelectorXPath != null) {  //has multiple events
                AXIOMXPath parentSelectorPath;
                try {
                    parentSelectorPath = new AXIOMXPath(parentSelectorXPath);
                } catch (JaxenException e) {
                    throw new ExecutionPlanRuntimeException("Could not get XPath from expression: " +
                            parentSelectorXPath, e);
                }
                List eventsNodes;
                try {
                    eventsNodes = parentSelectorPath.selectNodes(parentOMElement);
                } catch (JaxenException e) {
                    throw new ExecutionPlanRuntimeException("Error occurred when selecting nodes from XPath: "
                            + parentSelectorXPath, e);
                }
                if (eventsNodes.size() != 1) {
                    throw new ExecutionPlanRuntimeException("Input XML event can have only one \"" + parentSelectorXPath
                            + "\" element. Found " + eventsNodes.size() + ". Input event: " + inputEventStr);
                } else {
                    OMElement eventsElement = (OMElement) eventsNodes.get(0);
                    Iterator iterator = eventsElement.getChildElements();
                    while (iterator.hasNext()) {
                        OMElement eventOMElement = (OMElement) iterator.next();
                        Event event = buildEvent(eventOMElement);
                        eventList.add(event);
                    }
                }
            } else {    //input XML string has only one event in it.
                Event event = buildEvent(parentOMElement);
                eventList.add(event);
            }
        } else {    //default mapping case
            AXIOMXPath parentSelectorPath;
            try {
                parentSelectorPath = new AXIOMXPath("//" + EVENTS_PARENT_ELEMENT);
            } catch (JaxenException e) {
                throw new ExecutionPlanRuntimeException("Could not get XPath from expression: //" + EVENTS_PARENT_ELEMENT, e);
            }
            List eventsNodes;
            try {
                eventsNodes = parentSelectorPath.selectNodes(parentOMElement);
            } catch (JaxenException e) {
                throw new ExecutionPlanRuntimeException("Error occurred when selecting nodes from XPath: //"
                        + EVENTS_PARENT_ELEMENT, e);
            }
            if (eventsNodes.size() != 1) {
                throw new ExecutionPlanRuntimeException("Input XML event can have only one \"" + EVENTS_PARENT_ELEMENT
                        + "\" element. " + "Found " + eventsNodes.size() + ". Input event: " + inputEventStr);
            } else {
                OMElement eventsElement = (OMElement) eventsNodes.get(0);
                Iterator iterator = eventsElement.getChildElements();
                while (iterator.hasNext()) {
                    OMElement eventOMElement = (OMElement) iterator.next();

                    Event event = new Event(this.streamDefinition.getAttributeList().size());
                    Object[] data = event.getData();

                    List<Attribute> attributeList = streamDefinition.getAttributeList();
                    for (int i = 0; i < attributeList.size(); i++) {
                        Attribute attribute = attributeList.get(i);
                        String attributeName = attribute.getName();
                        Iterator attributeItr = eventOMElement.getChildrenWithLocalName(attributeName);
                        if (attributeItr.hasNext()) {
                            OMElement attrOMElement = (OMElement) attributeItr.next();
                            data[i] = AttributeConverter.getPropertyValue(attrOMElement.getText(), attribute.getType());
                            if (attributeItr.hasNext()) {
                                throw new ExecutionPlanRuntimeException("Input event has more than one attributes " +
                                        "with the same name: " + attributeName + ". Input event: " + inputEventStr);
                            }
                        } else {
                            throw new ExecutionPlanRuntimeException("No attribute with name: " + attribute +
                                    " found in input event: " + inputEventStr);
                        }
                    }
                    eventList.add(event);
                }
            }
        }
        return eventList;
    }

    private void buildNamespaceMap(String namespace) {
        namespaceMap = new HashMap<>();
        String[] namespaces = namespace.split(",");
        for (String ns : namespaces) {
            String[] splits = ns.split("=");
            if (splits.length != 2) {
                throw new ExecutionPlanValidationException("Malformed namespace mapping found: " + ns
                        + ". Each namespace has to have format: <prefix>=<uri>");
            }
            namespaceMap.put(splits[0].trim(), splits[1].trim());
        }
    }

    private Event buildEvent(OMElement eventOMElement) {
        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();

        List<Attribute> attributeList = streamDefinition.getAttributeList();
        for (int i = 0; i < attributeList.size(); i++) {
            Attribute attribute = attributeList.get(i);
            String attributeName = attribute.getName();

            AXIOMXPath axiomXPath = xPathMap.get(attributeName);
            try {
                List<Object> selectedNodes = axiomXPath.selectNodes(eventOMElement);
                if (selectedNodes.size() != 1) {
                    throw new ExecutionPlanRuntimeException("XPath: '" + xPathMap.get(attributeName).toString()
                            + "' yields " + selectedNodes.size() + " elements from input event, for attribute: "
                            + attributeName + ". The XPath should yield exactly one element.");
                }
                Object elementObj = selectedNodes.get(0);
                if (elementObj instanceof OMElement) {
                    OMElement element = (OMElement) elementObj;
                    if (element.getFirstElement() != null) {
                        data[i] = element.toString();
                    } else {
                        String attributeValue = element.getText();
                        data[i] = AttributeConverter.getPropertyValue(attributeValue, attribute.getType());
                    }
                } else if (elementObj instanceof OMAttribute) {
                    OMAttribute omAttribute = (OMAttribute) elementObj;
                    data[i] = AttributeConverter.getPropertyValue(omAttribute.getAttributeValue(), attribute.getType());
                }
            } catch (JaxenException e) {
                throw new ExecutionPlanRuntimeException("Error occurred when selecting attribute: " + attributeName
                        + " in the input event, using the given XPath: " + xPathMap.get(attributeName).toString());
            }
        }
        return event;
    }
}
