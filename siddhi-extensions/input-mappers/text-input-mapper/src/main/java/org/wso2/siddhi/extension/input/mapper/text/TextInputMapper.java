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
package org.wso2.siddhi.extension.input.mapper.text;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.InputMapper;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This mapper converts TEXT string input to {@link ComplexEventChunk}. This extension accepts optional regex mapping to
 * select specific attributes from the stream.
 */
@Extension(
        name = "text",
        namespace = "inputmapper",
        description = ""
)
public class TextInputMapper extends InputMapper {

    /**
     * Logger to log the events.
     */
    private static final Logger log = Logger.getLogger(TextInputMapper.class);

    /**
     * Default regex assumes that the attributes in the text are separated by comma in order.
     */
    public static final String DEFAULT_MAPPING_REGEX = "([^,;]+)";

    /**
     * Output StreamDefinition of the input mapper.
     */
    private StreamDefinition streamDefinition;

    /**
     * Attributes of the output stream.
     */
    private List<Attribute> streamAttributes;

    /**
     * Array of information about event mapping.
     */
    private MappingPositionData[] mappingPositions;
    private AttributeConverter attributeConverter;


    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition     the  StreamDefinition
     * @param optionHolder         mapping options
     * @param attributeMappingList list of attributes mapping
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping> attributeMappingList) {
        attributeConverter = new AttributeConverter();
        this.streamDefinition = streamDefinition;
        this.streamAttributes = this.streamDefinition.getAttributeList();

        int attributesSize = this.streamDefinition.getAttributeList().size();
        this.mappingPositions = new MappingPositionData[attributesSize];

        // Create the position regex arrays
        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            // Custom regex parameters are given
            for (int i = 0; i < attributeMappingList.size(); i++) {
                // i represents the position of attributes as given by the user in mapping

                AttributeMapping attributeMapping = attributeMappingList.get(i);

                // The optional name given by the user in attribute mapping
                String attributeName = attributeMapping.getRename();

                // The position of the attribute in the output stream definition
                int position;

                if (attributeName != null) {
                    // Use the name to determine the position
                    position = this.streamDefinition.getAttributePosition(attributeName);
                } else {
                    // Use the same order as provided by the user
                    position = i;
                }
                String[] mappingComponents = attributeMapping.getMapping().split("\\[");
                String regex = optionHolder.validateAndGetStaticValue(mappingComponents[0]);
                int index = Integer.parseInt(mappingComponents[1].substring(0, mappingComponents[1].length() - 1));
                this.mappingPositions[i] = new MappingPositionData(position, regex, index);
            }
        } else {
            StringBuilder regexBuilder = new StringBuilder();
            for (int i = 0; i < attributesSize; i++) {
                regexBuilder.append(DEFAULT_MAPPING_REGEX).append(",?");
            }
            //String regex = regexBuilder.toString();
            for (int i = 0; i < attributesSize; i++) {
                this.mappingPositions[i] = new MappingPositionData(i, regexBuilder.toString(), i + 1);
            }
        }
    }


    /**
     * Receive TEXT string from {@link InputTransport}, convert to {@link ComplexEventChunk} and send to the
     * {@link OutputCallback}.
     *
     * @param eventObject  the TEXT string
     * @param inputHandler input handler
     */
    @Override
    protected void mapAndProcess(Object eventObject, InputHandler inputHandler) throws InterruptedException {
        if (eventObject != null) {
            synchronized (this) {
                inputHandler.send(convertToEvent(eventObject));
            }
        }
    }

    /**
     * Convert the given TEXT string to {@link Event}
     *
     * @param eventObject TEXT string
     * @return the constructed Event object
     */
    private Event convertToEvent(Object eventObject) {
        // Validate the event
        // Validate the event
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Null object received from the InputTransport to TextInputMapper");
        }

        if (!(eventObject instanceof String)) {
            throw new ExecutionPlanRuntimeException("Invalid TEXT object received. Expected String, but found " +
                    eventObject.getClass()
                            .getCanonicalName());
        }

        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();

        for (MappingPositionData mappingPositionData : this.mappingPositions) {
            int position = mappingPositionData.getPosition();
            Attribute attribute = streamAttributes.get(position);
            data[position] = attributeConverter.getPropertyValue(mappingPositionData.match((String) eventObject),
                    attribute.getType());
        }

        return event;
    }


    /**
     * A POJO class which holds the attribute position in output stream, regex mapping and gruping index.
     */
    private class MappingPositionData {
        /**
         * Attribute position in the output stream.
         */
        private int position;

        /**
         * User defined regex value.
         * It can be shared across multiple {@link MappingPositionData}.
         */
        private String regex;

        /**
         * The group index to be used for mapping in the given regex.
         */
        private int groupIndex;

        public MappingPositionData(int position, String regex, int groupIndex) {
            this.position = position;
            this.regex = regex;
            this.groupIndex = groupIndex;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public void setGroupIndex(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        /**
         * Match the given text against the regex and return the group defined by the group index.
         *
         * @param text the input text
         * @return matched output
         */
        public String match(String text) {
            String matchedText;
            Pattern pattern = Pattern.compile(this.regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                matchedText = matcher.group(this.groupIndex);
            } else {
                matchedText = null;
            }
            return matchedText;
        }
    }
}
