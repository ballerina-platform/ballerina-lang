/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.util;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a JSON node. This can be either an object, array, 
 * number, string or null.
 * 
 * @since 0.95.5
 */
public class JsonNode {

    private String stringValue;
    
    private long longValue;
    
    private double doubleValue;
    
    private boolean booleanValue;
    
    private Type type;
    
    private Map<String, JsonNode> fields;
    
    private List<JsonNode> arrayElements;
    
    protected JsonNode parentNode;
    
    protected String fieldName;
    
    public JsonNode() {
        this(Type.OBJECT);
    }
    
    public JsonNode(Type type) {
        this.type = type;
    }
    
    public JsonNode(String stringValue) {
        this.setString(stringValue);
    }
    
    public JsonNode(long longValue) {
        this.setNumber(longValue);
    }
    
    public JsonNode(double doubleValue) {
        this.setNumber(doubleValue);
    }
    
    public JsonNode(boolean booleanValue) {
        this.setBooleanValue(booleanValue);
    }
    
    public Type getType() {
        return type;
    }
    
    public void add(JsonNode arrayElement) {
        if (arrayElements == null) {
            arrayElements = new LinkedList<>();
        }
        this.arrayElements.add(arrayElement);
    }
    
    public void add(long value) {
        this.add(new JsonNode(value));
    }
    
    public void add(double value) {
        this.add(new JsonNode(value));
    }
    
    public void add(boolean value) {
        this.add(new JsonNode(value));
    }
    
    public void add(String value) {
        this.add(new JsonNode(value));
    }
    
    public void addNull() {
        this.add(new JsonNode(Type.NULL));
    }
    
    public void setString(String stringValue) {
        if (stringValue != null) {
            this.stringValue = stringValue;
            this.type = Type.STRING;
        } else {
            this.type = Type.NULL;
        }
    }
    
    public void setNumber(long longValue) {
        this.longValue = longValue;
        this.type = Type.LONG;
    }
    
    public void setNumber(double doubleValue) {
        this.doubleValue = doubleValue;
        this.type = Type.DOUBLE;
    }
    
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.type = Type.BOOLEAN;
    }
    
    public void setNull() {
        this.type = Type.NULL;
    }
    
    public boolean isNull() {
        return this.type == Type.NULL;
    }
    
    public boolean isObject() {
        return this.type == Type.OBJECT;
    }
    
    public boolean isArray() {
        return this.type == Type.ARRAY;
    }
    
    public boolean isValueNode() {
        return this.isString() || this.isLong() || this.isDouble() || this.isBoolean() || this.isNull();
    }
    
    public boolean isLong() {
        return this.type == Type.LONG;
    }
    
    public boolean isDouble() {
        return this.type == Type.DOUBLE;
    }
    
    public boolean isString() {
        return this.type == Type.STRING;
    }
    
    public boolean isBoolean() {
        return this.type == Type.BOOLEAN;
    }
    
    public int size() {
        if (this.arrayElements != null) {
            return this.arrayElements.size();
        } else {
            return 0;
        }
    }
    
    public String stringValue() {
        return stringValue;
    }
    
    public long longValue() {
        return longValue;
    }
    
    public double doubleValue() {
        return doubleValue;
    }
    
    public boolean booleanValue() {
        return booleanValue;
    }
    
    public void set(String name, String stringValue) {
        if (this.fields == null) {
            this.fields = new LinkedHashMap<>();
        }
        if (stringValue == null) {
            this.fields.put(name, new JsonNode(Type.NULL));
        } else {
            this.fields.put(name, new JsonNode(stringValue));
        }
    }
    
    public void set(String name, long longValue) {
        if (this.fields == null) {
            this.fields = new LinkedHashMap<>();
        }
        this.fields.put(name, new JsonNode(longValue));
    }
    
    public void set(String name, double doubleValue) {
        if (this.fields == null) {
            this.fields = new LinkedHashMap<>();
        }
        this.fields.put(name, new JsonNode(doubleValue));
    }
    
    public void set(String name, boolean booleanValue) {
        if (this.fields == null) {
            this.fields = new LinkedHashMap<>();
        }
        this.fields.put(name, new JsonNode(booleanValue));
    }
    
    public void set(String name, JsonNode jsonNode) {
        if (this.fields == null) {
            this.fields = new LinkedHashMap<>();
        }
        if (jsonNode != null) {
            this.fields.put(name, jsonNode);
        } else {
            this.fields.put(name, new JsonNode(Type.NULL));
        }
    }
    
    public void set(int index, JsonNode jsonNode) {
        if (this.arrayElements == null) {
            throw new BallerinaException("JSON array is empty to set values");
        }
        this.arrayElements.set(index, jsonNode);
    }
    
    public boolean has(String name) {
        return this.get(name) != null;
    }
    
    public JsonNode get(String name) {
        if (this.fields != null) {
            return this.fields.get(name);
        } else {
            return null;
        }
    }
    
    public JsonNode get(int index) {
        if (this.arrayElements != null) {
            return this.arrayElements.get(index);
        } else {
            return null;
        }
    }
    
    public String asText() {
        switch (this.type) {
        case BOOLEAN:
            return Boolean.toString(this.booleanValue());
        case DOUBLE:
            return Double.toString(this.doubleValue());
        case LONG:
            return Long.toString(this.longValue());
        case NULL:
            return "null";
        case STRING:
            return this.stringValue();
        default:
            return "";
        }
    }
    
    public Iterator<String> fieldNames() {
        if (this.fields != null) {
            return this.fields.keySet().iterator();
        } else {
            return new HashSet<String>(0).iterator();
        }
    }
    
    public Iterator<Entry<String, JsonNode>> fields() {
        if (this.fields != null) {
            return this.fields.entrySet().iterator();
        } else {
            return new LinkedHashMap<String, JsonNode>(0).entrySet().iterator();
        }
    }
    
    public Iterator<JsonNode> elements() {
        if (this.arrayElements != null) {
            return this.arrayElements.iterator();
        } else {
            return new ArrayList<JsonNode>(0).iterator();
        }
    }
    
    public void remove(String fieldName) {
        if (this.fields != null) {
            this.fields.remove(fieldName);
        }
    }
    
    public String toString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
        try {
            this.serialize(gen);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error in converting JSON to a string: " + e.getMessage(), e);
        }
        return new String(byteOut.toByteArray());
    }
    
    /**
     * This represents the {@link JsonNode} types.
     */
    public static enum Type {
        OBJECT,
        ARRAY,
        STRING,
        LONG,
        DOUBLE,
        BOOLEAN,
        NULL
    }
    
    public void serialize(OutputStream out) throws IOException {
        JsonGenerator gen = new JsonGenerator(out);
        this.serialize(gen);
        gen.flush();
    }
    
    public void serialize(JsonGenerator gen) throws IOException {
        switch (this.type) {
        case ARRAY:
            gen.writeStartArray();
            if (this.arrayElements != null) {
                for (JsonNode node : this.arrayElements) {
                    if (node != null) {
                        node.serialize(gen);
                    } else {
                        gen.writeNull();
                    }
                }
            }
            gen.writeEndArray();
            break;
        case BOOLEAN:
            gen.writeBoolean(this.booleanValue);
            break;
        case DOUBLE:
            gen.writeNumber(this.doubleValue);
            break;
        case LONG:
            gen.writeNumber(this.longValue);
            break;
        case NULL:
            gen.writeNull();
            break;
        case OBJECT:
            gen.startObject();
            JsonNode value;
            if (this.fields != null) {
                for (Entry<String, JsonNode> entry : this.fields.entrySet()) {
                    gen.writeFieldName(entry.getKey());
                    value = entry.getValue();
                    if (value != null) {
                        entry.getValue().serialize(gen);
                    } else {
                        gen.writeNull();
                    }
                }
            }
            gen.endObject();
            break;
        case STRING:
            gen.writeString(this.stringValue);
            break;
        default:
            break;
        }
    }
        
}
