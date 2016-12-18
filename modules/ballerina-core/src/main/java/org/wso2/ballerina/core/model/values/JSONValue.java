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
package org.wso2.ballerina.core.model.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@code JSONValue} represents a JSON value in Ballerina.
 *
 * @since 1.0.0
 */
public class JSONValue extends BallerinaMessageDataSource implements BValue<JsonElement> {
    
    // GSON json object model associated with this JSONType object
    private JsonElement value;
    
    // Schema of this JSONType object model
    private JsonElement schema;

    // Output stream to write message out to the socket
    private OutputStream outputStream;

    /**
     * Initialize a {@link JSONValue} from a {@link com.google.gson.JsonElement} object.
     * 
     * @param json  json object 
     */
    public JSONValue(JsonElement json) {
        this.value = json;
    }
    
    /**
     * Initialize a {@link JSONValue} from a JSON string.
     * 
     * @param jsonString    A JSON string
     */
    public JSONValue(String jsonString) {
        this(jsonString, null);
    }
    
    /**
     * Initialize a {@link JSONValue} from a string, with a specified schema.
     * JSON will not be validated against the given schema.
     * 
     * @param jsonString    JSON String
     * @param schema        Schema of the provided JSON, as a string
     */
    public JSONValue(String jsonString, String schema) {
        JsonParser parser = new JsonParser();
        if (jsonString != null && !jsonString.isEmpty()) {
            this.value = parser.parse(jsonString);
        } else {
            throw new IllegalArgumentException("Cannot parse an empty string to json.");
        }
        if (schema != null) {
            this.schema = parser.parse(schema);
        }
    }

    /**
     * Create a {@link JSONValue} from a {@link InputStream}.
     * 
     * @param in    Input Stream
     */
    public JSONValue(InputStream in) {
        this(in, null);
    }
    
    /**
     * Create a {@link JSONValue} from a {@link InputStream}.
     * 
     * @param in    InputStream
     */
    public JSONValue(InputStream in, String schema) {
        JsonParser parser = new JsonParser();
        this.value = parser.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        if (schema != null) {
            this.schema = parser.parse(schema);
        }
    }
    
    /**
     * Return the string representation of this json object
     */
    public String toString() {
        return this.value.toString();
    }
    
    /**
     * Get value associated with this {@link JSONValue} object.
     * 
     * @return  JSON object associated with this {@link JSONValue} object
     */
    public JsonElement getValue() {
        return this.value;
    }
    
    /**
     * Set the value associated with this {@link JSONValue} object.
     * 
     * @param value     Value associated with this {@link JSONValue} object.
     */
    public void setValue(JsonElement value) {
        this.value = value;
    }
    
    /**
     * Get the schema associated with this {@link JSONValue} object.
     * 
     * @return  Schema associated with this {@link JSONValue} object
     */
    public JsonElement getSchema() {
        return this.schema;
    }
    
    /**
     * Set the schema associated with this {@link JSONValue} object.
     * 
     * @param schema    Schema associated with this {@link JSONValue} object.
     */
    public void setSchema(JsonElement schema) {
        this.schema = schema;
    }

    @Override
    public String getValueAsString(String path) {
        return null;
    }

    @Override
    public String getValueAsString(String path, Map<String, String> properties) {
        return null;
    }

    @Override
    public Object getValue(String path) {
        return null;
    }

    @Override
    public Object getDataObject() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void serializeData() {
        try {
            this.outputStream.write(this.value.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error occurred during writing the message to the output stream");
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
