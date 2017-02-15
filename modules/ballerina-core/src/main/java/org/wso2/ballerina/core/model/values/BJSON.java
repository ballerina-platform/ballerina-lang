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
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@code BJSON} represents a JSON value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BJSON extends BallerinaMessageDataSource implements BRefType<JsonElement> {

    // GSON json object model associated with this JSONType object
    private JsonElement value;

    // Schema of this JSONType object model
    private JsonElement schema;

    // Output stream to write message out to the socket
    private OutputStream outputStream;

    /**
     * Initialize a {@link BJSON} from a {@link com.google.gson.JsonElement} object.
     *
     * @param json json object
     */
    public BJSON(JsonElement json) {
        this.value = json;
    }

    /**
     * Initialize a {@link BJSON} from a JSON string.
     *
     * @param jsonString A JSON string
     */
    public BJSON(String jsonString) {
        this(jsonString, null);
    }

    /**
     * Initialize a {@link BJSON} from a string, with a specified schema.
     * JSON will not be validated against the given schema.
     *
     * @param jsonString JSON String
     * @param schema     Schema of the provided JSON, as a string
     */
    public BJSON(String jsonString, String schema) {
        JsonParser parser = new JsonParser();
        if (jsonString == null || jsonString.isEmpty()) {
            throw new IllegalArgumentException("cannot parse an empty string to json");
        }
        
        try {
            this.value = parser.parse(jsonString);
            if (schema != null) {
                this.schema = parser.parse(schema);
            }
        } catch (Throwable t) {
            handleJsonException("failed to create json: ", t);
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
     * @param in InputStream
     */
    public BJSON(InputStream in, String schema) {
        JsonParser parser = new JsonParser();
        try {
            this.value = parser.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
            if (schema != null) {
                this.schema = parser.parse(schema);
            }
        } catch (Throwable t) {
            handleJsonException("failed to create json: ", t);
        }
    }

    /**
     * Return the string representation of this json object.
     */
    public String toString() {
        return this.value.toString();
    }

    /**
     * Set the value associated with this {@link BJSON} object.
     *
     * @param value Value associated with this {@link BJSON} object.
     */
    public void setValue(JsonElement value) {
        this.value = value;
    }

    /**
     * Get the schema associated with this {@link BJSON} object.
     *
     * @return Schema associated with this {@link BJSON} object
     */
    public JsonElement getSchema() {
        return this.schema;
    }

    /**
     * Set the schema associated with this {@link BJSON} object.
     *
     * @param schema Schema associated with this {@link BJSON} object.
     */
    public void setSchema(JsonElement schema) {
        this.schema = schema;
    }

    @Override
    public void serializeData() {
        try {
            this.outputStream.write(this.value.toString().getBytes());
        } catch (Throwable t) {
            handleJsonException("error occurred during writing the message to the output stream: ", t);
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Get value associated with this {@link BJSON} object.
     *
     * @return JSON object associated with this {@link BJSON} object
     */
    @Override
    public JsonElement value() {
        return value;
    }

    @Override
    public String stringValue() {
        try {
            return this.value.toString();
        } catch (Throwable t) {
            handleJsonException("failed to get json as string: ", t);
        }
        return null;
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
        String elementVal = this.getMessageAsString();
        if (elementVal != null) {
            return new BJSON(new JsonParser().parse(elementVal));
        }
        return new BJSON("{}");
    }
}
