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

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.InputMapper;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

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
        namespace = "inputmapper",
        description = ""
)
public class JsonInputMapper extends InputMapper {

    private static final Logger log = Logger.getLogger(JsonInputMapper.class);

    public static final String DEFAULT_JSON_MAPPING_PREFIX = "$.";

    public static final String DEFAULT_JSON_PARENT_EVENT = "event";
    public static final String DEFAULT_JSON_EVENT_IDENTIFIER = "event";
    public static final String FAIL_ON_UNKNOWN_ATTRIBUTE_IDENTIFIER = "fail.on.unknown.attribute";
    public static final String ENABLE_JSON_VALIDATION_IDENTIFIER = "validate.json";

    private StreamDefinition streamDefinition;
    private MappingPositionData[] mappingPositions;
    private List<Attribute> streamAttributes;
    private boolean isCustomMappingEnabled = false;
    private boolean isJsonValidationEnabled = false;
    private boolean failOnUnknownAttribute = true;
    private String enclosingElement = null;
    private final String ENCLOSING_ELEMENT_INDENTIFIER = "enclosing.element";

    private AttributeConverter attributeConverter = new AttributeConverter();

    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping> attributeMappingList) {
        this.streamDefinition = streamDefinition;
        this.streamAttributes = this.streamDefinition.getAttributeList();
        int attributesSize = this.streamDefinition.getAttributeList().size();
        this.mappingPositions = new MappingPositionData[attributesSize];
        failOnUnknownAttribute = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(FAIL_ON_UNKNOWN_ATTRIBUTE_IDENTIFIER,"false"));
        isJsonValidationEnabled = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(ENABLE_JSON_VALIDATION_IDENTIFIER,"false"));

        if (attributeMappingList != null && attributeMappingList.size() > 0) {
            isCustomMappingEnabled = true;
            enclosingElement = optionHolder.validateAndGetStaticValue(ENCLOSING_ELEMENT_INDENTIFIER,"$");
            for (int i = 0; i < attributeMappingList.size(); i++) {
                // i represents the position of attributes as given by the user in mapping

                AttributeMapping attributeMapping = attributeMappingList.get(i);

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
                this.mappingPositions[i] = new MappingPositionData(position, attributeMapping.getMapping());
            }
        } else {
            // Use the attribute names of the output stream in order
            for (int i = 0; i < attributesSize; i++) {
                this.mappingPositions[i] = new MappingPositionData(i, DEFAULT_JSON_MAPPING_PREFIX + this
                        .streamDefinition.getAttributeList().get(i).getName());
            }
        }
    }

    @Override
    protected void mapAndProcess(Object eventObject, InputHandler inputHandler) throws InterruptedException {
        synchronized (this) {
            Object convertedEvent = null;
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
        if(isJsonValidationEnabled && !isJsonValid(eventObject.toString())){
            throw new ExecutionPlanRuntimeException("Invalid Json String :"+eventObject.toString());
        }

        Object jsonObj;
        ReadContext readContext = JsonPath.parse(eventObject.toString());
        if(isCustomMappingEnabled){
            jsonObj = readContext.read(enclosingElement);
            if(jsonObj == null){
                throw new ExecutionPlanRuntimeException("Enclosing element "+ enclosingElement +" can not be found.");
            }
            if (jsonObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonObj;
                Event[] newEventArray = new Event[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    Event event = processEvent(JsonPath.parse(jsonArray.get(i)));
                    if(!failOnUnknownAttribute){
                        newEventArray[i] = event;
                    }else if(!checkForUnknownAttributes(event)){
                        newEventArray[i] = event;
                    }
                }
                return newEventArray;
            } else {
                Event event = processEvent(JsonPath.parse(jsonObj));
                if(failOnUnknownAttribute){
                    throw new ExecutionPlanRuntimeException("Event "+event.toString()+" contains unknown attributes");
                }
                return processEvent(JsonPath.parse(jsonObj));
            }
        }
        else{
            jsonObj = readContext.read("$");
            if(jsonObj instanceof JSONArray){
                return convertToEventArrayForDefaultMapping(eventObject);
            }else {
                try {
                   return convertToSingleEventForDefaultMapping(eventObject);
                } catch (IOException e) {
                    throw new ExecutionPlanRuntimeException("Invalid JSON string :"+eventObject.toString()+". Cannot be converted to an event");
                }
            }
        }
    }

    private Object processForCustomMapping(Object eventObject){
        Object jsonObj;

        ReadContext readContext = JsonPath.parse(eventObject.toString());
        jsonObj = readContext.read(enclosingElement);
        if(jsonObj == null){
            throw new ExecutionPlanRuntimeException("Enclosing element "+ enclosingElement +" can not be found.");
        }

        if (jsonObj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObj;
            Event[] newEventArray = new Event[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                newEventArray[i] = processEvent(JsonPath.parse(jsonArray.get(i)));
            }
            return newEventArray;
        } else {
            return processEvent(JsonPath.parse(jsonObj));
        }
    }

    private Event convertToSingleEventForDefaultMapping(Object eventObject) throws IOException{
        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();
        JsonFactory factory = new JsonFactory();
        com.fasterxml.jackson.core.JsonParser parser = null;
        try {
            parser  = factory.createParser(eventObject.toString());
        } catch (IOException e) {
            throw new ExecutionPlanRuntimeException("Initializing a parser failed for the event string."+eventObject.toString());
        }

        int position = 0;

        while(!parser.isClosed()){
            JsonToken jsonToken = parser.nextToken();
            if(DEFAULT_JSON_EVENT_IDENTIFIER.equals(parser.getText())){
                parser.nextToken();
            }
            else if(JsonToken.FIELD_NAME.equals(jsonToken)){
                String key = parser.getCurrentName();
                position = findMappingPosition(streamAttributes,key);
                jsonToken = parser.nextToken();
                switch(streamAttributes.get(position).getType()){
                    case BOOL:
                            if(JsonToken.VALUE_TRUE.equals(jsonToken) || JsonToken.VALUE_FALSE.equals(jsonToken)) {
                                 data[position] = parser.getValueAsBoolean();
                                 break;
                            }

                    case INT:
                        if(JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = parser.getValueAsInt();
                            break;
                        }
                    case DOUBLE:
                        if(JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = parser.getValueAsDouble();
                            break;
                        }
                    case STRING:
                        if(JsonToken.VALUE_STRING.equals(jsonToken)) {
                            data[position] = parser.getValueAsString();
                            break;
                        }
                    case FLOAT:
                        if(JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken)) {
                            data[position] = convertAttribute(parser.getValueAsString(), Attribute.Type.FLOAT);
                            break;
                        }
                    case LONG:
                        if(JsonToken.VALUE_NUMBER_INT.equals(jsonToken)) {
                            data[position] = parser.getValueAsLong();
                            break;
                        }
                }
            }
        }
        return event;
    }

    private Event[] convertToEventArrayForDefaultMapping(Object eventObject){
        Gson gson = new Gson();
        JsonObject[] objects = gson.fromJson(eventObject.toString(), JsonObject[].class);
        Event[] events = new Event[objects.length];
        for(int i=0; i<objects.length; i++){
            JsonObject eventObj = objects[i].get(DEFAULT_JSON_EVENT_IDENTIFIER).getAsJsonObject();
            Event event = new Event(streamAttributes.size());
            Object[] data = event.getData();
            for(Attribute attribute : streamAttributes){
                String attribtueName = attribute.getName();
                Attribute.Type type = attribute.getType();
                int position = findMappingPosition(streamAttributes,attribtueName);
                if(position != -1){
                    throw new ExecutionPlanRuntimeException("Can not find a mapping for " + attribtueName + " in the stream " + streamDefinition);
                }
                data[position] = attributeConverter.getPropertyValue(eventObj.get(attribtueName).toString(),type);
            }
            events[i] = event;
        }
        return events;
    }

    private Event processEvent(ReadContext readContext) {
        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();
        Object childObject = readContext.read(DEFAULT_JSON_EVENT_IDENTIFIER);
        readContext = JsonPath.parse(childObject.toString());
        for (MappingPositionData mappingPositionData : this.mappingPositions) {
            int position = mappingPositionData.getPosition();
            Object mappedValue = readContext.read(mappingPositionData.getMapping());
            if(mappedValue == null){
                throw new ExecutionPlanRuntimeException("Can't find an attribute to map the value, "+mappingPositionData.getMapping());
            }
            data[position] = attributeConverter.getPropertyValue(mappedValue.toString(),
                    streamAttributes.get(position).getType());
        }
        return event;
    }

    private int findMappingPosition(List<Attribute> streamAttributes,String key){
        for(int i=0; i<streamAttributes.size(); i++){
            String attributeName = streamAttributes.get(i).getName();
            if(attributeName.equals(key)){
                return i;
            }
        }
        return -1;
    }
    private Object convertAttribute(Object attribute,Attribute.Type type){
        String value = "";
        int index = 0;
        switch (type){
            case INT:
                value = attribute.toString();
                index = value.indexOf('.');
                return Integer.parseInt(value.substring(0,index));
            case LONG:
                value = attribute.toString();
                index = value.indexOf('.');
                return Long.parseLong(value.substring(0,index));
            case DOUBLE:
                return Double.parseDouble(attribute.toString());
            case FLOAT:
                return Float.parseFloat(attribute.toString());
            case BOOL:
                return Boolean.parseBoolean(attribute.toString());
        }
        return attribute;
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
    private boolean isJsonValid(String jsonInString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private boolean checkForUnknownAttributes(Event event){
        Object[] data = event.getData();
        for(Object value : data){
            if(value == null){
               return true;
            }
        }
        return false;
    }
}
