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

import org.apache.axiom.om.DeferredParsingException;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.log4j.Logger;
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

import javax.xml.namespace.QName;
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
        description = "XML to Event input mapper"
)
public class XmlInputMapper extends InputMapper {

    private static final Logger log = Logger.getLogger(XmlInputMapper.class);
    private static final String PARENT_SELECTOR_XPATH = "enclosing.element";
    private static final String NAMESPACES = "namespaces";
    private static final String EVENTS_PARENT_ELEMENT = "events";
    private static final String EVENT_ELEMENT = "event";
    private static final String FAIL_ON_UNKNOWN_ATTRIBUTE = "fail.on.unknown.attribute";

    /**
     * Indicates whether custom mapping is enabled or not.
     */
    private boolean isCustomMappingEnabled = false;
    private StreamDefinition streamDefinition;
    private AXIOMXPath enclosingElementSelectorPath = null;
    private Map<String, String> namespaceMap;
    private Map<String, AXIOMXPath> xPathMap = new HashMap<>();
    private boolean failOnUnknownAttribute;
    private AttributeConverter attributeConverter = new AttributeConverter();
    private List<Attribute> attributeList;
    private Map<String, Attribute.Type> attributeTypeMap = new HashMap<>();
    private Map<String, Integer> attributePositionMap = new HashMap<>();

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
        String enclosingElementSelectorXPath;
        this.streamDefinition = streamDefinition;
        attributeList = streamDefinition.getAttributeList();
        attributeTypeMap = new HashMap<>(attributeList.size());
        attributePositionMap = new HashMap<>(attributeList.size());
        namespaceMap = new HashMap<>();
        for (Attribute attribute : attributeList) {
            attributeTypeMap.put(attribute.getName(), attribute.getType());
            attributePositionMap.put(attribute.getName(), streamDefinition.getAttributePosition(attribute.getName()));
        }
        failOnUnknownAttribute = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(FAIL_ON_UNKNOWN_ATTRIBUTE,
                "true"));

        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            isCustomMappingEnabled = true;
            if (streamDefinition.getAttributeList().size() < attributeMappingList.size()) {
                throw new ExecutionPlanValidationException("Stream: '" + streamDefinition.getId() + "' has "
                        + streamDefinition.getAttributeList().size() + " attributes, but " + attributeMappingList.size()
                        + " attribute mappings found. Each attribute should have one and only one mapping.");
            }

            String namespaces = optionHolder.validateAndGetStaticValue(NAMESPACES, null);
            if (namespaces != null) {
                buildNamespaceMap(namespaces);
            }

            for (AttributeMapping attributeMapping : attributeMappingList) {
                if (attributeTypeMap.containsKey(attributeMapping.getRename())) {
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
                } else {
                    throw new ExecutionPlanValidationException("No attribute with name " + attributeMapping.getRename()
                            + " available in stream. Hence halting Execution plan deployment");
                }
            }
            enclosingElementSelectorXPath = optionHolder.validateAndGetStaticValue(PARENT_SELECTOR_XPATH, null);
            if (enclosingElementSelectorXPath != null) {
                try {
                    enclosingElementSelectorPath = new AXIOMXPath(enclosingElementSelectorXPath);
                    for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                        try {
                            enclosingElementSelectorPath.addNamespace(entry.getKey(), entry.getValue());
                        } catch (JaxenException e) {
                            throw new ExecutionPlanValidationException("Error occurred when adding namespace: " + entry.getKey()
                                    + ":" + entry.getValue() + " to XPath element:" + enclosingElementSelectorXPath.toString());
                        }
                    }
                } catch (JaxenException e) {
                    throw new ExecutionPlanRuntimeException("Could not get XPath from expression: " +
                            enclosingElementSelectorXPath, e);
                }
            }


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
        Event[] result;
        try {
            result = convertToEvents(eventObject);
            if (result.length > 0) {
                inputHandler.send(result);
            }
        } catch (Throwable t) { //stringToOM does not throw the exception immediately due to streaming. Hence need this.
            log.error("Exception occurred when converting XML message to Siddhi Event", t);
        }

    }

    /**
     * Converts an event from an XML string to {@link Event}
     *
     * @param eventObject The input event, given as an XML string
     * @return the constructed {@link Event} object
     */
    private Event[] convertToEvents(Object eventObject) {
        List<Event> eventList = new ArrayList<>();
        OMElement rootOMElement;
        if (eventObject instanceof String) {
            try {
                rootOMElement = AXIOMUtil.stringToOM((String) eventObject);
            } catch (XMLStreamException | DeferredParsingException e) {
                log.warn("Error parsing incoming XML event : " + eventObject + ". Reason: " + e.getMessage() +
                        ". Hence dropping message chunk");
                return new Event[0];
            }
        } else if (eventObject instanceof OMElement) {
            rootOMElement = (OMElement) eventObject;
        } else {
            log.warn("Event object is invalid. Expected String/OMElement, but found " +
                    eventObject.getClass().getCanonicalName());
            return new Event[0];
        }
        if (isCustomMappingEnabled) {   //custom mapping case
            if (enclosingElementSelectorPath != null) {  //has multiple events
                List enclosingNodeList;
                try {
                    enclosingNodeList = enclosingElementSelectorPath.selectNodes(rootOMElement);
                    if (enclosingNodeList.size() == 0) {
                        log.warn("Provided enclosing element did not match any xml node. Hence dropping the event :" +
                                rootOMElement.toString());
                        return new Event[0];
                    }
                } catch (JaxenException e) {
                    throw new ExecutionPlanRuntimeException("Error occurred when selecting nodes from XPath: "
                            + enclosingElementSelectorPath.toString(), e);
                }
                for (Object enclosingNode : enclosingNodeList) {
                    Iterator iterator = ((OMElement) enclosingNode).getChildElements();
                    while (iterator.hasNext()) {
                        OMElement eventOMElement = (OMElement) iterator.next();
                        Event event = buildEvent(eventOMElement);
                        if (event != null) {
                            eventList.add(event);
                        }
                    }
                }

            } else {    //input XML string has only one event in it.
                Event event = buildEvent(rootOMElement);
                if (event != null) {
                    eventList.add(event);
                }
            }
        } else {    //default mapping case
            if (EVENTS_PARENT_ELEMENT.equals(rootOMElement.getLocalName())) {
                Iterator iterator = rootOMElement.getChildrenWithName(QName.valueOf(EVENT_ELEMENT));
                while (iterator.hasNext()) {
                    boolean isMalformedEvent = false;
                    OMElement eventOMElement = (OMElement) iterator.next();
                    Event event = new Event(attributeList.size());
                    Object[] data = event.getData();
                    Iterator eventIterator = eventOMElement.getChildElements();
                    while (eventIterator.hasNext()) {
                        OMElement attrOMElement = (OMElement) eventIterator.next();
                        String attributeName = attrOMElement.getLocalName();
                        Attribute.Type type;
                        if ((type = attributeTypeMap.get(attributeName)) != null) {
                            try {
                                data[attributePositionMap.get(attributeName)] = attributeConverter.getPropertyValue(
                                        attrOMElement.getText(), type);
                            } catch (ExecutionPlanRuntimeException | NumberFormatException e) {
                                log.warn("Error occurred when extracting attribute value. Cause: " + e.getMessage() +
                                        ". Hence dropping the event: " + eventOMElement.toString());
                                isMalformedEvent = true;
                                break;
                            }
                        } else {
                            log.warn("Attribute : " + attributeName + "was not found in given stream definition. " +
                                    "Hence ignoring this attribute");
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == null && failOnUnknownAttribute) {
                            log.warn("No attribute with name: " + streamDefinition.getAttributeNameArray()[i] +
                                    " found in input event: " + eventOMElement.toString() + ". Hence dropping the" +
                                    " event.");
                            isMalformedEvent = true;
                        }
                    }
                    if (!isMalformedEvent) {
                        eventList.add(event);
                    }
                }
            } else {
                log.warn("Incoming XML message should adhere to pre-defined format" +
                        "when using default mapping. Root element name should be " + EVENTS_PARENT_ELEMENT + ". But " +
                        "found " + rootOMElement.getLocalName() + ". Hence dropping XML message : " + rootOMElement.toString());
            }
        }
        return eventList.toArray(new Event[0]);
    }

    private void buildNamespaceMap(String namespace) {
        String[] namespaces = namespace.split(",");
        for (String ns : namespaces) {
            String[] splits = ns.split("=");
            if (splits.length != 2) {
                log.warn("Malformed namespace mapping found: " + ns + ". Each namespace has to have format: <prefix>=<uri>");
            }
            namespaceMap.put(splits[0].trim(), splits[1].trim());
        }
    }

    private Event buildEvent(OMElement eventOMElement) {
        Event event = new Event(streamDefinition.getAttributeList().size());
        Object[] data = event.getData();
        for (int i = 0; i < attributeList.size(); i++) {
            Attribute attribute = attributeList.get(i);
            String attributeName = attribute.getName();
            AXIOMXPath axiomXPath = xPathMap.get(attributeName);
            try {
                List selectedNodes = axiomXPath.selectNodes(eventOMElement);
                if (selectedNodes.size() == 0) {
                    if (failOnUnknownAttribute) {
                        log.warn("Xpath: '" + axiomXPath.toString() + " did not yield any results. Hence dropping the " +
                                "event : " + eventOMElement.toString());
                        return null;
                    } else {
                        continue;
                    }
                }
                //We will by default consider the first node. We are not logging this to get rid of an if condition.
                Object elementObj = selectedNodes.get(0);
                if (elementObj instanceof OMElement) {
                    OMElement element = (OMElement) elementObj;
                    if (element.getFirstElement() != null) {
                        if (attribute.getType().equals(Attribute.Type.STRING)) {
                            data[i] = element.toString();
                        } else {
                            log.warn("XPath: " + axiomXPath.toString() + " did not return a leaf element and stream " +
                                    "definition is not expecting a String attribute. Hence dropping the event: " +
                                    eventOMElement.toString());
                            return null;
                        }
                    } else {
                        String attributeValue = element.getText();
                        try {
                            data[i] = attributeConverter.getPropertyValue(attributeValue, attribute.getType());
                        } catch (ExecutionPlanRuntimeException | NumberFormatException e) {
                            if (failOnUnknownAttribute) {
                                log.warn("Error occurred when extracting attribute value. Cause: " + e.getMessage() +
                                        ". Hence dropping the event: " + eventOMElement.toString());
                                return null;
                            }
                        }
                    }
                } else if (elementObj instanceof OMAttribute) {
                    OMAttribute omAttribute = (OMAttribute) elementObj;
                    try {
                        data[i] = attributeConverter.getPropertyValue(omAttribute.getAttributeValue(), attribute.getType());
                    } catch (ExecutionPlanRuntimeException | NumberFormatException e) {
                        log.warn("Error occurred when extracting attribute value. Cause: " + e.getMessage() +
                                ". Hence dropping the event: " + eventOMElement.toString());
                        return null;
                    }
                }
            } catch (JaxenException e) {
                log.warn("Error occurred when selecting attribute: " + attributeName
                        + " in the input event, using the given XPath: " + xPathMap.get(attributeName).toString());
                return null;
            }
        }
        return event;
    }
}
