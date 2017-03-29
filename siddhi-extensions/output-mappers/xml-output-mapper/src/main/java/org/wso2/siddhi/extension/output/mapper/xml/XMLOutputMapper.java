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
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransportCallback;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Extension(
        name = "xml",
        namespace = "outputmapper",
        description = "Event to XML output mapper"
)
public class XMLOutputMapper extends OutputMapper {
    private static final Logger log = Logger.getLogger(XMLOutputMapper.class);

    private static final String EVENTS_PARENT_TAG = "events";
    private static final String EVENT_PARENT_TAG = "event";
    private static final String OPTION_PREFIX_XML = "prefixXml";
    private static final String OPTION_SUFFIX_XML = "suffixXml";
    private static final String OPTION_VALIDATE_XML = "validateXml";

    private StreamDefinition streamDefinition;
    private String prefixXml;
    private String suffixXml;
    private boolean xmlValidationEnabled = false;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

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
        if (payloadTemplateBuilder != null) {
            //Means, this is a custom mapping scenario; In default case, prefix & suffix is ignored.
            Option prefixXmlOption = optionHolder.getOrCreateOption(OPTION_PREFIX_XML, null);
            if (prefixXmlOption != null) {
                prefixXml = prefixXmlOption.getValue();
            }
            Option suffixXmlOption = optionHolder.getOrCreateOption(OPTION_SUFFIX_XML, null);
            if (suffixXmlOption != null) {
                suffixXml = suffixXmlOption.getValue();
            }
            if (prefixXml != null && suffixXml == null) {
                log.warn(OPTION_SUFFIX_XML + " option is not given while " + OPTION_PREFIX_XML + " option has been given. " +
                        "Typically, these options are used together.");
            } else if (suffixXml != null && prefixXml == null) {
                log.warn(OPTION_PREFIX_XML + " option is not given while " + OPTION_SUFFIX_XML + " option has been given. " +
                        "Typically, these options are used together.");
            }
        }
        Option validateXmlOption = optionHolder.getOrCreateOption(OPTION_VALIDATE_XML, null);
        if (validateXmlOption != null) {
            xmlValidationEnabled = Boolean.parseBoolean(validateXmlOption.getValue());
            if (xmlValidationEnabled) {
                factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(true);

                try {
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                   throw new ExecutionPlanCreationException("Error occurred when initializing XML validator", e);
                }
            }
        }
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
        if (events.length < 1) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (payloadTemplateBuilder != null) {   //custom mapping
            if (prefixXml != null) {
                sb.append(prefixXml);
            }
            for (Event event : events) {
                sb.append(payloadTemplateBuilder.build(event));
            }
            if (suffixXml != null) {
                sb.append(suffixXml);
            }
            if (xmlValidationEnabled) {
                try {
                    builder.parse(new ByteArrayInputStream(sb.toString().getBytes()));
                    builder.reset();
                } catch (SAXException e) {
                    log.error("Parse error occurred when validating output XML event for well-formedness. " +
                            "Reason: " + e.getMessage() + "Dropping event: " + sb.toString());
                    return;
                } catch (IOException e) {
                    log.error("IO error occurred when validating output XML event for well-formedness. " +
                            "Reason: " + e.getMessage() + "Dropping event: " + sb.toString());
                    return;
                }
            }
        } else {
            for (Event event : events) {
                sb.append(constructDefaultMapping(event));
            }
        }
        outputTransportCallback.publish(sb.toString(), events[0]);
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