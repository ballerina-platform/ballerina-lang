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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.subscription.InputMapper;
import org.wso2.siddhi.core.subscription.InputTransport;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.io.map.AttributeMapping;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * This mapper converts XML string input to {@link ComplexEventChunk}. This extension accepts optional mapping to
 * select specific attributes from the stream.
 */
public class XmlInputMapper implements InputMapper {

    /**
     * Logger to log the events.
     */
    private static final Logger log = Logger.getLogger(XmlInputMapper.class);

    /**
     * Xpath prefix
     */
    private static final String DEFAULT_XPATH_PREFIX = "//h:";

    /**
     * Output StreamDefinition of the input mapper.
     */
    private StreamDefinition outputStreamDefinition;

    /**
     * OutputCallback to which the converted event must be sent.
     */
    private OutputCallback outputCallback;

    /**
     * StreamEventPool used to borrow a new event.
     */
    private StreamEventPool streamEventPool;

    /**
     * StreamEventConverter to convert {@link Event} to {@link StreamEvent}.
     */
    private StreamEventConverter streamEventConverter;

    /**
     * Array of information about event mapping.
     */
    private MappingPositionData[] mappingPositions;

    /**
     * Attributes of the output stream.
     */
    private List<Attribute> streamAttributes;

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param outputStreamDefinition the output StreamDefinition
     * @param outputCallback         the OutputCallback to which the output has to be sent
     * @param metaStreamEvent        the MetaStreamEvent
     * @param options                additional mapping options
     * @param attributeMappingList   list of attributes mapping
     */
    @Override // TODO: 1/6/17 static -test
    public void init(StreamDefinition outputStreamDefinition, OutputCallback outputCallback, MetaStreamEvent metaStreamEvent, Map<String, String> options, List<AttributeMapping> attributeMappingList) {
        this.outputStreamDefinition = outputStreamDefinition;
        this.outputCallback = outputCallback;
        this.outputStreamDefinition = metaStreamEvent.getOutputStreamDefinition(); // TODO: 1/5/17 why assigned twice?
        this.streamEventConverter = new ZeroStreamEventConverter();
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        this.streamAttributes = this.outputStreamDefinition.getAttributeList();

        int attributesSize = this.outputStreamDefinition.getAttributeList().size();
        this.mappingPositions = new MappingPositionData[attributesSize];

        // Create the position mapping arrays
        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            // Custom mapping parameters are given
            for (int i = 0; i < attributeMappingList.size(); i++) {
                // i represents the position of attributes as given by the user in mapping

                AttributeMapping attributeMapping = attributeMappingList.get(i);

                // The optional name given by the user in attribute mapping
                String attributeName = attributeMapping.getRename();

                // The position of the attribute in the output stream definition
                int position;

                if (attributeName != null) {
                    // Use the name to determine the position
                    position = outputStreamDefinition.getAttributePosition(attributeName);
                } else {
                    // Use the same order as provided by the user
                    position = i;
                }
                this.mappingPositions[i] = new MappingPositionData(position, attributeMapping.getMapping());
            }
        } else {
            // Use the attribute names of the output stream in order
            for (int i = 0; i < attributesSize; i++) {
                this.mappingPositions[i] = new MappingPositionData(i, DEFAULT_XPATH_PREFIX + this
                        .outputStreamDefinition.getAttributeList().get(i).getName());
            }
        }
    }

    /**
     * Receive XML string from {@link InputTransport}, convert to {@link ComplexEventChunk} and send to the
     * {@link OutputCallback}.
     *
     * @param eventObject the XML string
     */
    @Override
    public void onEvent(Object eventObject) {
        synchronized (this) {
            StreamEvent borrowedEvent = streamEventPool.borrowEvent();
            try {
                streamEventConverter.convertEvent(convertToEvent(eventObject), borrowedEvent);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) { // TODO: 1/5/17 catch exceptions
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            outputCallback.send(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, true));
        }
    }

    /**
     * Convert the given JSON string to {@link Event}
     *
     * @param eventObject JSON string
     * @return the constructed Event object
     */
    private Event convertToEvent(Object eventObject) throws ParserConfigurationException, IOException, SAXException {

        // Validate the event
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Null object received from the InputTransport to XmlInputMapper");
        }

        if (!(eventObject instanceof String)) {
            throw new ExecutionPlanRuntimeException("Invalid XML object received. Expected String, but found " +
                    eventObject.getClass()
                            .getCanonicalName());
        }

        Event event = new Event(this.outputStreamDefinition.getAttributeList().size());
        Object[] data = event.getData();

        // Parse the XML string and build the data
        for (MappingPositionData mappingPositionData : this.mappingPositions) {
            int position = mappingPositionData.getPosition();
            Attribute attribute = streamAttributes.get(position);
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(eventObject.toString()));
            Document parsedXml = documentBuilder.parse(inputSource);
            data[position] = AttributeConverter.getPropertyValue(parsedXml.getElementsByTagName(mappingPositionData.getMapping()),
                    attribute.getType());
        }

        return event;
    }

    /**
     * A POJO class which holds the attribute position in output stream and the user defined mapping.
     */
    private class MappingPositionData {
        /**
         * Attribute position in the output stream.
         */
        private int position;

        /**
         * The XML mapping as defined by the user.
         */
        private String mapping;

        public MappingPositionData(int position, String mapping) {
            this.position = position;
            this.mapping = mapping;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }
    }
}
