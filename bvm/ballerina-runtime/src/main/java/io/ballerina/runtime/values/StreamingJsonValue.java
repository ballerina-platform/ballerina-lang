/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.values;

import io.ballerina.runtime.JSONDataSource;
import io.ballerina.runtime.JSONGenerator;
import io.ballerina.runtime.JSONUtils;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BStreamingJson;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BMapType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * <p>
 * {@link StreamingJsonValue} represent a JSON array generated from a {@link JSONDataSource}.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @since 0.981.0
 */
public class StreamingJsonValue extends ArrayValueImpl implements BStreamingJson {

    JSONDataSource datasource;

    @Deprecated
    public StreamingJsonValue(JSONDataSource datasource) {
        super(new BArrayType(new BMapType(PredefinedTypes.TYPE_JSON)));
        this.datasource = datasource;
    }

    @Override
    public void add(long index, Object value) {
        // If the the index is larger than the size, and data-source has more content,
        // then read data from data-source until the index, or until the end of the data-source.
        while (index >= size && datasource.hasNext()) {
            appendToCache(datasource.next());
        }

        super.add(index, value);
    }

    @Override
    public void append(Object value) {
        if (datasource.hasNext()) {
            buildDatasource();
        }

        super.append(value);
    }

    @Override
    public Object getRefValue(long index) {
        // If the the index is larger than the size, and datasource has more content,
        // then read data from data-source until the index, or until the end of the data-source.
        while (index >= size && datasource.hasNext()) {
            appendToCache(datasource.next());
        }

        return super.getRefValue(index);
    }

    /**
     * Serialize to the given {@code JSONGenerator}.
     * @param gen {@code JSONGenerator} to use
     */
    public void serialize(JSONGenerator gen) {
        /*
         * Below order is important, where if the value is generated from a streaming data source,
         * it should be able to serialize the data out again using the value
         */
        try {
            gen.writeStartArray();

            // First serialize the values loaded to memory
            for (int i = 0; i < size; i++) {
                gen.serialize(refValues[i]);
            }

            // Then serialize remaining data in the data-source
            while (datasource.hasNext()) {
                gen.serialize(datasource.next());
            }
            gen.writeEndArray();
            gen.flush();
        } catch (IOException e) {
            throw JSONUtils.createJsonConversionError(e, "error occurred while serializing data");
        }
    }

    /**
     * Serialize the value to given {@code Writer}.
     * @param writer {@code Writer} to be used
     */
    public void serialize(Writer writer) {
        serialize(new JSONGenerator(writer));
    }

    @Override
    public void serialize(OutputStream outputStream) {
        serialize(new JSONGenerator(outputStream));
    }

    @Override
    public Object[] getValues() {
        if (datasource.hasNext()) {
            buildDatasource();
        }
        return refValues;
    }

    @Override
    public String toString() {
        if (datasource.hasNext()) {
            buildDatasource();
        }

        return super.toString();
    }

    @Override
    public String stringValue(BLink parent) {
        if (datasource.hasNext()) {
            buildDatasource();
        }

        return super.stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public int size() {
        if (datasource.hasNext()) {
            buildDatasource();
        }
        return size;
    }

    void appendToCache(Object value) {
        super.add(size, value);
    }

    @Override
    public String getJSONString() {
        // Consume and materialize the stream.
        buildDatasource();
        return super.getJSONString();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        // Consume and materialize the stream.
        buildDatasource();
        return super.copy(refs);
    }

    private void buildDatasource() {
        try {
            while (datasource.hasNext()) {
                appendToCache(datasource.next());
            }
        } catch (Throwable t) {
            throw JSONUtils.createJsonConversionError(t, "error occurred while building JSON");
        }
    }

    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

    /**
     * {@code {@link StreamingJsonIterator}} provides iterator implementation for Ballerina array values.
     *
     * @since 0.995.0
     */
    static class StreamingJsonIterator implements IteratorValue {
        StreamingJsonValue array;
        long cursor = 0;

        StreamingJsonIterator(StreamingJsonValue value) {
            this.array = value;
        }

        @Override
        public Object next() {
            Object value;
            // If the current index is loaded in to memory, then read from it
            if (cursor < array.size) {
                value = array.get(cursor);
            } else {
                // Otherwise read the next value from data-source and cache it in memory
                value = array.datasource.next();
                array.appendToCache(value);
            }

            this.cursor++;
            return value;
        }

        @Override
        public boolean hasNext() {
            if (cursor < array.size) {
                return true;
            }

            return array.datasource.hasNext();
        }
    }
}
