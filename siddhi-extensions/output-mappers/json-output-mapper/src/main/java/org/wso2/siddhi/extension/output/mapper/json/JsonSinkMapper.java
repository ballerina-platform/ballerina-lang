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
package org.wso2.siddhi.extension.output.mapper.json;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.SinkListener;
import org.wso2.siddhi.core.stream.output.sink.SinkMapper;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

/**
 * Mapper class to convert a Siddhi message to a JSON message. User can provide a JSON template or else we will be
 * using a predefined JSON message format. In some instances
 * coding best practices have been compensated for performance concerns.
 */

@Extension(
        name = "json",
        namespace = "sinkMapper",
        description = "Event to JSON output mapper. Transports which publish  messages can utilize this extension"
                + "to convert the Siddhi event to JSON message. Users can either send a pre-defined JSON "
                + "format or a custom JSON message.",
        parameters = {
                @Parameter(name = "validate.json",
                        description = "This property will enable JSON validation for generated JSON message. By "
                                + "default value of the property will be false. When enabled DAS will validate the "
                                + "generated JSON message and drop the message if it does not adhere to proper JSON "
                                + "standards. ",
                        type = {DataType.BOOL}),
                @Parameter(name = "enclosing.element",
                        description =
                                "Used to specify the enclosing element in case of sending multiple events in same "
                                        + "JSON message. WSO2 DAS will treat the child element of given enclosing "
                                        + "element as events"
                                        + " and execute json expressions on child elements. If enclosing.element "
                                        + "is not provided "
                                        + "multiple event scenario is disregarded and json path will be evaluated "
                                        + "with respect to "
                                        + "root element.",
                        type = {DataType.STRING})
        },
        examples = {
                @Example(
                        syntax = "@sink(type='inMemory', topic='stock', @map(type='json'))\n"
                                + "define stream FooStream (symbol string, price float, volume long);\n",
                        description = "Above configuration will do a default JSON input mapping which will "
                                + "generate below "
                                + "output"
                                + "{\n"
                                + "    \"event\":{\n"
                                + "        \"symbol\":WSO2,\n"
                                + "        \"price\":55.6,\n"
                                + "        \"volume\":100\n"
                                + "    }\n"
                                + "}\n"),
                @Example(
                        syntax = "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', enclosing"
                                + ".element='$.portfolio', validate.json='true', @payload( "
                                + "\"{\"StockData\":{\"Symbol\":\"{{symbol}}\",\"Price\":{{price}}}\")))\n"
                                + "define stream BarStream (symbol string, price float, volume long);",
                        description = "Above configuration will perform a custom JSON mapping which will "
                                + "produce below "
                                + "output JSON message"
                                + "{"
                                + "\"portfolio\":{\n"
                                + "    \"StockData\":{\n"
                                + "        \"Symbol\":WSO2,\n"
                                + "        \"Price\":55.6\n"
                                + "      }\n"
                                + "  }\n"
                                + "}")
        }
)

public class JsonSinkMapper extends SinkMapper {
    private static final Logger log = Logger.getLogger(JsonSinkMapper.class);
    private static final String EVENT_PARENT_TAG = "event";
    private static final String ENCLOSING_ELEMENT_IDENTIFIER = "enclosing.element";
    private static final String DEFAULT_ENCLOSING_ELEMENT = "$";
    private static final String JSON_VALIDATION_IDENTIFIER = "validate.json";
    private static final String JSON_EVENT_SEPERATOR = ",";
    private static final String JSON_KEYVALUE_SEPERATOR = ":";
    private static final String JSON_ARRAY_START_SYMBOL = "[";
    private static final String JSON_ARRAY_END_SYMBOL = "]";
    private static final String JSON_EVENT_START_SYMBOL = "{";
    private static final String JSON_EVENT_END_SYMBOL = "}";
    private static final String UNDEFINED = "undefined";

    private String[] attributeNameArray;
    private String enclosingElement = null;
    private boolean isJsonValidationEnabled = false;


    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options
     * @param payloadTemplateBuilder Unmapped payload for reference
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                     TemplateBuilder payloadTemplateBuilder, ConfigReader mapperConfigReader) {
        attributeNameArray = streamDefinition.getAttributeNameArray();
        enclosingElement = optionHolder.validateAndGetStaticValue(ENCLOSING_ELEMENT_IDENTIFIER, null);
        isJsonValidationEnabled = Boolean.parseBoolean(optionHolder
                .validateAndGetStaticValue(JSON_VALIDATION_IDENTIFIER, "false"));
    }

    @Override
    public void mapAndSend(Event[] events, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder,
                           SinkListener sinkListener, DynamicOptions dynamicOptions)
            throws ConnectionUnavailableException {
        StringBuilder sb = null;
        if (payloadTemplateBuilder == null) {
            String jsonString = constructJsonForDefaultMapping(events);
            if (jsonString != null) {
                sb = new StringBuilder();
                sb.append(jsonString);
            }
        } else {
            sb = new StringBuilder();
            sb.append(constructJsonForCustomMapping(events, payloadTemplateBuilder));
        }

        if (sb != null) {
            if (!isJsonValidationEnabled) {
                sinkListener.publish(sb.toString(), dynamicOptions);
            } else if (isValidJson(sb.toString())) {
                sinkListener.publish(sb.toString(), dynamicOptions);
            } else {
                log.error("Invalid json string : " + sb.toString() + ". Hence dropping the message.");
            }
        }
    }

    @Override
    public void mapAndSend(Event event, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder,
                           SinkListener sinkListener, DynamicOptions dynamicOptions)
            throws ConnectionUnavailableException {
        StringBuilder sb = null;
        if (payloadTemplateBuilder == null) {
            String jsonString = constructJsonForDefaultMapping(event);
            if (jsonString != null) {
                sb = new StringBuilder();
                sb.append(jsonString);
            }
        } else {
            sb = new StringBuilder();
            sb.append(constructJsonForCustomMapping(event, payloadTemplateBuilder));
        }

        if (sb != null) {
            if (!isJsonValidationEnabled) {
                sinkListener.publish(sb.toString(), dynamicOptions);
            } else if (isValidJson(sb.toString())) {
                sinkListener.publish(sb.toString(), dynamicOptions);
            } else {
                log.error("Invalid json string : " + sb.toString() + ". Hence dropping the message.");
            }
        }
    }

    private String constructJsonForDefaultMapping(Object eventObj) {
        StringBuilder sb = new StringBuilder();
        int numberOfOuterObjects = 0;
        if (enclosingElement != null) {
            String[] nodeNames = enclosingElement.split("\\.");
            if (DEFAULT_ENCLOSING_ELEMENT.equals(nodeNames[0])) {
                numberOfOuterObjects = nodeNames.length - 1;
            } else {
                numberOfOuterObjects = nodeNames.length;
            }
            for (String nodeName : nodeNames) {
                if (!DEFAULT_ENCLOSING_ELEMENT.equals(nodeName)) {
                    sb.append(JSON_EVENT_START_SYMBOL).append("\"").append(nodeName).append("\"")
                            .append(JSON_KEYVALUE_SEPERATOR);
                }
            }
            if (eventObj.getClass() == Event.class) {
                Event event = (Event) eventObj;
                JsonObject jsonEvent = constructSingleEventForDefaultMapping(doPartialProcessing(event));
                sb.append(jsonEvent);
            } else if (eventObj.getClass() == Event[].class) {
                JsonArray eventArray = new JsonArray();
                for (Event event : (Event[]) eventObj) {
                    eventArray.add(constructSingleEventForDefaultMapping(doPartialProcessing(event)));
                }
                sb.append(eventArray.toString());
            } else {
                log.error("Invalid object type. " + eventObj.toString() +
                        " cannot be converted to an event or event array. Hence dropping message.");
                return null;
            }
            for (int i = 0; i < numberOfOuterObjects; i++) {
                sb.append(JSON_EVENT_END_SYMBOL);
            }
            return sb.toString();
        } else {
            if (eventObj.getClass() == Event.class) {
                Event event = (Event) eventObj;
                JsonObject jsonEvent = constructSingleEventForDefaultMapping(doPartialProcessing(event));
                return jsonEvent.toString();
            } else if (eventObj.getClass() == Event[].class) {
                JsonArray eventArray = new JsonArray();
                for (Event event : (Event[]) eventObj) {
                    eventArray.add(constructSingleEventForDefaultMapping(doPartialProcessing(event)));
                }
                return (eventArray.toString());
            } else {
                log.error("Invalid object type. " + eventObj.toString() +
                        " cannot be converted to an event or event array.");
                return null;
            }
        }
    }

    private String constructJsonForCustomMapping(Object eventObj, TemplateBuilder payloadTemplateBuilder) {
        StringBuilder sb = new StringBuilder();
        int numberOfOuterObjects = 0;
        if (enclosingElement != null) {
            String[] nodeNames = enclosingElement.split("\\.");
            if (DEFAULT_ENCLOSING_ELEMENT.equals(nodeNames[0])) {
                numberOfOuterObjects = nodeNames.length - 1;
            } else {
                numberOfOuterObjects = nodeNames.length;
            }
            for (String nodeName : nodeNames) {
                if (!DEFAULT_ENCLOSING_ELEMENT.equals(nodeName)) {
                    sb.append(JSON_EVENT_START_SYMBOL).append("\"").append(nodeName).append("\"")
                            .append(JSON_KEYVALUE_SEPERATOR);
                }
            }
            if (eventObj.getClass() == Event.class) {
                Event event = doPartialProcessing((Event) eventObj);
                sb.append(payloadTemplateBuilder.build(event));
            } else if (eventObj.getClass() == Event[].class) {
                String jsonEvent;
                sb.append(JSON_ARRAY_START_SYMBOL);
                for (Event e : (Event[]) eventObj) {
                    jsonEvent = payloadTemplateBuilder.build(doPartialProcessing(e));
                    if (jsonEvent != null) {
                        sb.append(jsonEvent).append(JSON_EVENT_SEPERATOR).append("\n");
                    }
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append(JSON_ARRAY_END_SYMBOL);
            } else {
                log.error("Invalid object type. " + eventObj.toString() +
                        " cannot be converted to an event or event array. Hence dropping message.");
                return null;
            }
            for (int i = 0; i < numberOfOuterObjects; i++) {
                sb.append(JSON_EVENT_END_SYMBOL);
            }
            return sb.toString();
        } else {
            if (eventObj.getClass() == Event.class) {
                return payloadTemplateBuilder.build(doPartialProcessing((Event) eventObj));
            } else if (eventObj.getClass() == Event[].class) {
                String jsonEvent;
                sb.append(JSON_ARRAY_START_SYMBOL);
                for (Event event : (Event[]) eventObj) {
                    jsonEvent = payloadTemplateBuilder.build(doPartialProcessing(event));
                    if (jsonEvent != null) {
                        sb.append(jsonEvent).append(JSON_EVENT_SEPERATOR).append("\n");
                    }
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append(JSON_ARRAY_END_SYMBOL);
                return sb.toString();
            } else {
                // TODO : log error
                log.error("Invalid object type. " + eventObj.toString() +
                        " cannot be converted to an event or event array. Hence dropping message.");
                return null;
            }
        }
    }

    private JsonObject constructSingleEventForDefaultMapping(Event event) {
        Object[] data = event.getData();
        JsonObject jsonEventObject = new JsonObject();
        JsonObject innerParentObject = new JsonObject();
        String attributeName;
        Object attributeValue;
        Gson gson = new Gson();
        for (int i = 0; i < data.length; i++) {
            attributeName = attributeNameArray[i];
            attributeValue = data[i];
            if (attributeValue != null) {
                if (attributeValue.getClass() == String.class) {
                    innerParentObject.addProperty(attributeName, attributeValue.toString());
                } else if (attributeValue instanceof Number) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Boolean) {
                    innerParentObject.addProperty(attributeName, (Boolean) attributeValue);
                } else if (attributeValue instanceof Map) {
                    if (!((Map) attributeValue).isEmpty()) {
                        innerParentObject.add(attributeName, gson.toJsonTree(attributeValue));
                    }
                }
            }
        }
        jsonEventObject.add(EVENT_PARENT_TAG, innerParentObject);
        return jsonEventObject;
    }

    private Event doPartialProcessing(Event event) {
        Object[] data = event.getData();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                data[i] = UNDEFINED;
            }
        }
        return event;
    }

    private static boolean isValidJson(String jsonInString) {
        try {
            new Gson().fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}
