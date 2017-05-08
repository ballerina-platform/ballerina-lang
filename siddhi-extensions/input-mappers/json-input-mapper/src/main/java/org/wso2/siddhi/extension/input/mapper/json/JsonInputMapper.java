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
package org.wso2.siddhi.extension.input.mapper.json;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceMapper;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;

/**
 * This mapper converts JSON string input to {@link ComplexEventChunk}. This extension accepts optional mapping to
 * select
 * specific attributes from the stream.
 * For example, <pre>{@code subscription.map(Mapping.format("json"));}</pre> converts a given JSON string to Java
 * objects and select the stream attributes based on the output stream's attributes.
 * If custom mapping is given like <pre>{@code subscription.map(Mapping.format("json").map("$.country").map("$
 * .price").map("$.volume", "volume"));}</pre>, it will select the user defined attributes only.
 */
@Extension(
        name = "json",
        namespace = "sourceMapper",
        description = ""
)
public class JsonInputMapper extends SourceMapper {

    private static final Logger log = Logger.getLogger(JsonInputMapper.class);

    public static final String DEFAULT_JSON_MAPPING_PREFIX = "$.";
    public static final String DEFAULT_JSON_EVENT_IDENTIFIER = "event";
    public static final String FAIL_ON_UNKNOWN_ATTRIBUTE_IDENTIFIER = "fail.on.unknown.attribute";
    private final String ENCLOSING_ELEMENT_INDENTIFIER = "enclosing.element";

    private StreamDefinition streamDefinition;
    private MappingPositionData[] mappingPositions;
    private List<Attribute> streamAttributes;
    private boolean isCustomMappingEnabled = false;
    private boolean failOnUnknownAttribute = false;
    private String enclosingElement = null;
    private AttributeConverter attributeConverter = new AttributeConverter();

    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping> attributeMappingList, ConfigReader configReader) {
        this.streamDefinition = streamDefinition;
        this.streamAttributes = this.streamDefinition.getAttributeList();
        int attributesSize = this.streamDefinition.getAttributeList().size();
        this.mappingPositions = new MappingPositionData[attributesSize];
        failOnUnknownAttribute = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(FAIL_ON_UNKNOWN_ATTRIBUTE_IDENTIFIER, "false"));
        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            isCustomMappingEnabled = true;
            enclosingElement = optionHolder.validateAndGetStaticValue(ENCLOSING_ELEMENT_INDENTIFIER, "$");
            for (int i = 0; i < attributeMappingList.size(); i++) {
                AttributeMapping attributeMapping = attributeMappingList.get(i);
                String attributeName = attributeMapping.getRename();
                int position;
                if (attributeName != null) {
                    position = this.streamDefinition.getAttributePosition(attributeName);
                } else {
                    position = i;
                }
                this.mappingPositions[i] = new MappingPositionData(position, attributeMapping.getMapping());
            }
        } else {
            for (int i = 0; i < attributesSize; i++) {
                this.mappingPositions[i] = new MappingPositionData(i, DEFAULT_JSON_MAPPING_PREFIX + this
                        .streamDefinition.getAttributeList().get(i).getName());
            }
        }
    }

    @Override
    protected void mapAndProcess(Object eventObject, InputHandler inputHandler) throws InterruptedException {
        synchronized (this) {
            Object convertedEvent;
            convertedEvent = convertToEvent(eventObject);
            if (convertedEvent != null) {
                if (convertedEvent instanceof Event[]) {
                    inputHandler.send((Event[]) convertedEvent);
                } else {
                    inputHandler.send((Event) convertedEvent);
                }
            }
        }
    }

    /**
     * Convert the given JSON string to {@link Event}
     *
     * @param eventObject JSON string
     * @return the constructed Event object
     */
    private Object convertToEvent(Object eventObject) {
        // Validate the event
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Null object received from the InputTransport to JsonInputMapper");
        }

        if (!(eventObject instanceof String)) {
            throw new ExecutionPlanRuntimeException("Invalid JSON object received. Expected String, but found " +
                    eventObject.getClass()
                            .getCanonicalName());
        }

        if (!isJsonValid(eventObject.toString())) {
            throw new ExecutionPlanRuntimeException("Invalid Json String :" + eventObject.toString());
        }

        Object jsonObj;
        ReadContext readContext = JsonPath.parse(eventObject.toString());
        int index;
        if (isCustomMappingEnabled) {
            jsonObj = readContext.read(enclosingElement);
            if (jsonObj == null) {
                throw new ExecutionPlanRuntimeException("Enclosing element " + enclosingElement + " can not be found.");
            }
            if (jsonObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonObj;
                Event[] newEventArray = new Event[jsonArray.size()];
                index = 0;
                for (int i = 0; i < jsonArray.size(); i++) {
                    Event event = processEvent(JsonPath.parse(jsonArray.get(i)));
                    if (failOnUnknownAttribute && checkForUnknownAttributes(event)) {
                        log.error("Event " + event.toString() + " contains unknown attributes");
                    } else {
                        newEventArray[index++] = event;
                    }
                }
                return Arrays.copyOfRange(newEventArray, 0, index);
            } else {
                Event event = processEvent(JsonPath.parse(jsonObj));
                if (failOnUnknownAttribute && checkForUnknownAttributes(event)) {
                    throw new ExecutionPlanRuntimeException("Event " + event.toString() + " contains unknown attributes");
                }
                return event;
            }
        } else {
            jsonObj = readContext.read("$");
            if (jsonObj instanceof JSONArray) {
                return convertToEventArrayForDefaultMapping(eventObject);
            } else {
                try {
                    Event event = convertToSingleEventForDefaultMapping(eventObject);
                    if (failOnUnknownAttribute && checkForUnknownAttributes(event)) {
                        throw new ExecutionPlanRuntimeException("Event " + event.toString() + " contains unknown attributes");
                    }
                    return event;
                } catch (IOException e) {
                    throw new ExecutionPlanRuntimeException("Invalid JSON string :" + eventObject.toString() + ". Cannot be converted to an event");
                }
            }
        }
    }

    private Event convertToSingleEventForDefaultMapping(Object eventObject) throws IOException {
        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();
        JsonFactory factory = new JsonFactory();
        com.fasterxml.jackson.core.JsonParser parser;
        try {
            parser = factory.createParser(eventObject.toString());
        } catch (IOException e) {
            throw new ExecutionPlanRuntimeException("Initializing a parser failed for the event string." + eventObject.toString());
        }
        int position;
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (DEFAULT_JSON_EVENT_IDENTIFIER.equals(parser.getText())) {
                parser.nextToken();
            } else if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String key = parser.getCurrentName();
                position = findDefaultMappingPosition(key);
                jsonToken = parser.nextToken();
                switch (streamAttributes.get(position).getType()) {
                    case BOOL:
                        if (JsonToken.VALUE_TRUE.equals(jsonToken) || JsonToken.VALUE_FALSE.equals(jsonToken)) {
                            data[position] = parser.getValueAsBoolean();
                            break;
                        }
                    case INT:
                        if (JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = parser.getValueAsInt();
                            break;
                        }
                    case DOUBLE:
                        if (JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = parser.getValueAsDouble();
                            break;
                        }
                    case STRING:
                        if (JsonToken.VALUE_STRING.equals(jsonToken)) {
                            data[position] = parser.getValueAsString();
                            break;
                        }
                    case FLOAT:
                        if (JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = convertAttribute(parser.getValueAsString(), Attribute.Type.FLOAT);
                            break;
                        }
                    case LONG:
                        if (JsonToken.VALUE_NUMBER_INT.equals(jsonToken)) {
                            data[position] = parser.getValueAsLong();
                            break;
                        }
                }
            }
        }
        return event;
    }

    private Event[] convertToEventArrayForDefaultMapping(Object eventObject) {
        Gson gson = new Gson();
        JsonObject[] eventObjects = gson.fromJson(eventObject.toString(), JsonObject[].class);
        Event[] events = new Event[eventObjects.length];
        int index = 0;
        for (int i = 0; i < eventObjects.length; i++) {
            JsonObject eventObj = eventObjects[i].get(DEFAULT_JSON_EVENT_IDENTIFIER).getAsJsonObject();
            Event event = new Event(streamAttributes.size());
            Object[] data = event.getData();
            if (eventObj.size() < streamAttributes.size()) {
                if (failOnUnknownAttribute) {
                    log.error("Event " + eventObj.toString() + " contains unknown attributes");
                    continue;
                }
            }

            int position = 0;
            for (Attribute attribute : streamAttributes) {
                String attribtueName = attribute.getName();
                Attribute.Type type = attribute.getType();
                data[position] = attributeConverter.getPropertyValue(eventObj.get(attribtueName).toString(), type);
                position++;
            }
            events[index++] = event;
        }
        return Arrays.copyOfRange(events, 0, index);
    }

    private Event processEvent(ReadContext readContext) {
        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();
        Object childObject = readContext.read(DEFAULT_JSON_EVENT_IDENTIFIER);
        readContext = JsonPath.parse(childObject.toString());
        for (MappingPositionData mappingPositionData : this.mappingPositions) {
            int position = mappingPositionData.getPosition();
            Object mappedValue = readContext.read(mappingPositionData.getMapping());
            if (mappedValue == null) {
                throw new ExecutionPlanRuntimeException("Can't find an attribute to map the value, " + mappingPositionData.getMapping());
            }
            data[position] = attributeConverter.getPropertyValue(mappedValue.toString(),
                    streamAttributes.get(position).getType());
        }
        return event;
    }

    private int findDefaultMappingPosition(String key) {
        for (int i = 0; i < streamAttributes.size(); i++) {
            String attributeName = streamAttributes.get(i).getName();
            if (attributeName.equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private Object convertAttribute(Object attribute, Attribute.Type type) {
        String value;
        int index;
        switch (type) {
            case INT:
                value = attribute.toString();
                return (int) Double.parseDouble(value);
            case LONG:
                value = attribute.toString();
                return (long) Double.parseDouble(value);
            case DOUBLE:
                return Double.parseDouble(attribute.toString());
            case FLOAT:
                return Float.parseFloat(attribute.toString());
            case BOOL:
                return Boolean.parseBoolean(attribute.toString());
        }
        return attribute;
    }

    private boolean isJsonValid(String jsonInString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private boolean checkForUnknownAttributes(Event event) {
        Object[] data = event.getData();
        if (isCustomMappingEnabled) {
            if (streamAttributes.size() > event.getData().length) {
                return true;
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    return true;
                }
            }
        }
        return false;
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
         * The JSON mapping as defined by the user.
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
