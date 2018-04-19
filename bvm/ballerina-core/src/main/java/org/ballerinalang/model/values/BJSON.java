/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BStructType.StructField;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.JsonNode.Type;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

/**
 * {@code BJSON} represents a JSON value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BJSON extends BallerinaMessageDataSource implements BRefType<JsonNode>, BCollection {

    private BType type = BTypes.typeJSON;

    // The streaming JSON data source object
    private JSONDataSource datasource;

    // json object model associated with this JSONType object
    private JsonNode value;

    /**
     * Initialize a {@link BJSON} from a {@link JsonNode} object.
     *
     * @param json json object
     */
    public BJSON(JsonNode json) {
        this.value = json;
        setType();
    }

    /**
     * Initialize a {@link BJSON} from a JSON string.
     *
     * @param jsonString A JSON string
     * @param type of the JSON
     */
    public BJSON(String jsonString, BType type) {
        this(jsonString);
        this.type = type;
    }

    /**
     * Initialize a {@link BJSON} from a streaming datasource.
     * 
     * @param datasource Datasource of this json
     */
    public BJSON(JSONDataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * Initialize a {@link BJSON} from a string, with a specified schema.
     * JSON will not be validated against the given schema.
     *
     * @param jsonString JSON String
     */
    public BJSON(String jsonString) {
        if (jsonString == null) {
            this.value = new JsonNode(Type.NULL);
            type = BTypes.typeNull;
            return;
        }

        try {
            this.value = JsonParser.parse(jsonString);
            setType();
        } catch (Throwable t) {
            handleJsonException(t);
        } 
    }

    /**
     * Create a {@link BJSON} from a {@link InputStream}.
     *
     * @param in Input Stream
     */
    public BJSON(InputStream in) {
        this(in, null);
    }

    /**
     * Create a {@link BJSON} from a {@link InputStream}.
     *
     * @param in InputStream of the json content
     * @param schema Schema of the json
     */
    public BJSON(InputStream in, String schema) {
        try {
            this.value = JsonParser.parse(in);
        } catch (Throwable t) {
            handleJsonException("failed to create json: ", t);
        }
    }

    /**
     * Return the string representation of this json object.
     */
    public String toString() {
        return this.stringValue();
    }

    /**
     * Set the value associated with this {@link BJSON} object.
     *
     * @param value Value associated with this {@link BJSON} object.
     */
    public void setValue(JsonNode value) {
        this.value = value;
    }

    @Override
    public void serializeData(OutputStream outputStream) {
        try {
            /* the below order is important, where if the value is generated from a streaming data source,
             * it should be able to serialize the data out again using the value */
            if (this.value != null) {
                this.value.serialize(outputStream);
            } else {
                JsonGenerator gen = new JsonGenerator(outputStream);
                this.datasource.serialize(gen);
                gen.flush();
            }
        } catch (Throwable t) {
            handleJsonException("error occurred during writing the message to the output stream: ", t);
        }
    }

    /**
     * Get value associated with this {@link BJSON} object.
     *
     * @return JSON object associated with this {@link BJSON} object
     */
    @Override
    public JsonNode value() {
        if (this.value == null) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                JsonGenerator gen = new JsonGenerator(byteOut);
                this.datasource.serialize(gen);
                gen.flush();
                this.value = JsonParser.parse(new ByteArrayInputStream(byteOut.toByteArray()));
            } catch (Throwable t) {
                handleJsonException("Error in building JSON node: ", t);
            }
        }
        return this.value;
    }

    @Override
    public String stringValue() {
        JsonNode node = this.value();
        if (node.isValueNode()) {
            return this.value().asText();
        } else if (!node.isObject()) {
            return node.toString();
        }

        BStructType constrainedType = (BStructType) ((BJSONType) this.type).getConstrainedType();
        if (constrainedType == null) {
            return node.toString();
        }

        // If constrained JSON, print the only the fields in the constrained type.
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (StructField field : constrainedType.getStructFields()) {
            String key = field.fieldName;
            String stringValue = this.value().get(key).toString();
            sj.add("\"" + key + "\":" + stringValue);
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return this.type;
    }

    public void setType(BType type) {
        this.type = (BJSONType) type;
    }

    @Override
    public String getMessageAsString() {
        try {
            return this.value.toString();
        } catch (Throwable t) {
            handleJsonException("failed to get json as string: ", t);
        }
        return null;
    }

    private static void handleJsonException(Throwable t) {
        if (t.getCause() != null) {
            throw new BallerinaException(t.getCause().getMessage());
        } else {
            throw new BallerinaException(t.getMessage());
        }
    }

    private static void handleJsonException(String message, Throwable t) {
        // Here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (t.getCause() != null) {
            throw new BallerinaException(message + t.getCause().getMessage());
        } else {
            throw new BallerinaException(message + t.getMessage());
        }
    }

    @Override
    public BallerinaMessageDataSource clone() {
        BJSON clonedMessage = new BJSON("{}");
        try {
            String elementString = this.getMessageAsString();
            JsonNode clonedContent = JsonParser.parse(elementString);
            clonedMessage.setValue(clonedContent);
        } catch (Throwable t) {
            handleJsonException("failed to clone the json message: ", t);
        }
        return clonedMessage;
    }

    @Override
    public BIterator newIterator() {
        return new BJSONIterator(this);
    }

    /**
     * {@code {@link BJSONIterator}} provides iterator implementation for json values.
     *
     * @since 0.96.0
     */
    static class BJSONIterator implements BIterator {

        BJSON collection;
        // Fields for JSON Object iteration.
        Iterator<Map.Entry<String, JsonNode>> iterator;
        // Fields for JSON Array iteration.
        boolean isJSONArray;
        int size, cursor = 0;

        BJSONIterator(BJSON value) {
            collection = value;
            if (collection.type.getTag() == TypeTags.ARRAY_TAG || collection.value().isArray()) {
                isJSONArray = true;     // This is a JSON Array. Index will be a int.
                size = collection.value().size();
            } else {
                iterator = collection.value().fields(); // This is a JSON Object or other type.
            }
        }

        @Override
        public BValue[] getNext(int arity) {
            if (isJSONArray) {
                long cursor = this.cursor++;
                if (arity == 1) {
                    return new BValue[] {new BJSON(collection.value.get((int) cursor))};
                } else {
                    return new BValue[] {new BInteger(cursor), new BJSON(collection.value.get((int) cursor))};
                }
            }
            if (arity == 1) {
                return new BValue[] {new BJSON(iterator.next().getValue())};
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return isJSONArray ? cursor < size : iterator.hasNext();
        }
    }

    /**
     * This represents a JSON data source implementation, which should be used for custom JSON
     * streaming implementations.
     */
    public static interface JSONDataSource {

        /**
         * Serializes the current representation of the JSON data source to the given {@link JsonGenerator}.
         * @param gen The {@link JsonGenerator} object to write the data to
         * @throws IOException Error occurs while serializing
         */
        void serialize(JsonGenerator gen) throws IOException;

    }
    
    @Override
    public BValue copy() {
        return new BJSON(this.stringValue());
    }
    
    private void setType() {
        switch (this.value.getType()) {
            case ARRAY:
                this.type = new BArrayType(BTypes.typeJSON);
                break;
            case BOOLEAN:
                this.type =  BTypes.typeBoolean;
                break;
            case DOUBLE:
                this.type =  BTypes.typeFloat;
                break;
            case LONG:
                this.type =  BTypes.typeInt;
                break;
            case NULL:
                this.type =  BTypes.typeNull;
                break;
            case STRING:
                this.type =  BTypes.typeString;
                break;
            default:
                this.type = BTypes.typeJSON;
        }
    }
    
    public BRefType<?> getPrimitiveBoxedValue() {
        if (this.value.isLong()) {
            return new BInteger(this.value.longValue());
        } else if (this.value.isDouble()) {
            return new BFloat(this.value.doubleValue());
        } else if (this.value.isString()) {
            return new BString(this.value.stringValue());
        } else if (this.value.isBoolean()) {
            return new BBoolean(this.value.booleanValue());
        } else {
            return null;
        }
    }
    
}
