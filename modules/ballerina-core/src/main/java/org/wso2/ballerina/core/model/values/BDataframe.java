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
package org.wso2.ballerina.core.model.values;

import com.fasterxml.jackson.core.JsonGenerator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.ballerina.core.model.DataIterator;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BJSON.JSONDataSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The {@code BDataframe} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BDataframe implements BRefType<Object> {

    private DataIterator iterator;
    private Map<String, Object> properties;
    private List<ColumnDefinition> columnDefs;

    public BDataframe(DataIterator dataIterator, Map<String, Object> properties, 
            List<ColumnDefinition> columnDefs) {
        this.iterator = dataIterator;
        this.properties = properties;
        this.columnDefs = columnDefs;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    public boolean next() {
        return iterator.next();
    }

    public void close() {
        iterator.close();
    }

    public String getString(int index) {
        return iterator.getString(index);
    }

    public String getString(String columnName) {
        return iterator.getString(columnName);
    }

    public String[] getStringArray(int index) {
        return iterator.getStringArray(index);
    }

    public String[] getStringArray(String columnName) {
        return iterator.getStringArray(columnName);
    }

    public long getLong(int index) {
        return iterator.getLong(index);
    }

    public long getLong(String columnName) {
        return iterator.getLong(columnName);
    }

    public long[] getLongArray(int index) {
        return iterator.getLongArray(index);
    }

    public long[] getLongArray(String columnName) {
        return iterator.getLongArray(columnName);
    }

    public int getInt(int index) {
        return iterator.getInt(index);
    }

    public int getInt(String columnName) {
        return iterator.getInt(columnName);
    }

    public int[] getIntArray(int index) {
        return iterator.getIntArray(index);
    }

    public int[] getIntArray(String columnName) {
        return iterator.getIntArray(columnName);
    }

    public float getFloat(int index) {
        return iterator.getFloat(index);
    }

    public float getFloat(String columnName) {
        return iterator.getFloat(columnName);
    }

    public float[] getFloatArray(int index) {
        return iterator.getFloatArray(index);
    }

    public float[] getFloatArray(String columnName) {
        return iterator.getFloatArray(columnName);
    }

    public double getDouble(int index) {
        return iterator.getDouble(index);
    }

    public double getDouble(String columnName) {
        return iterator.getDouble(columnName);
    }

    public double[] getDoubleArray(int index) {
        return iterator.getDoubleArray(index);
    }

    public double[] getDoubleArray(String columnName) {
        return iterator.getDoubleArray(columnName);
    }

    public boolean getBoolean(int index) {
        return iterator.getBoolean(index);
    }

    public boolean getBoolean(String columnName) {
        return iterator.getBoolean(columnName);
    }

    public boolean[] getBooleanArray(int index) {
        return iterator.getBooleanArray(index);
    }

    public boolean[] getBooleanArray(String columnName) {
        return iterator.getBooleanArray(columnName);
    }

    public BValue get(int index, String type) {
        return iterator.get(index, type);
    }

    public BValue get(String columnName, String type) {
        return iterator.get(columnName, type);
    }

    public String[] getAvailableProprtyNames() {
        return properties.keySet().toArray(new String[properties.keySet().size()]);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public BJSON toJSON() {
        return new BJSON(new DataTableJSONDataSource());
    }
    
    /**
     * This represents a column definition for a column in a dataframe.
     */
    public static class ColumnDefinition {
        
        private String name;
        
        private TypeEnum type;
        
        private TypeEnum elementType;
        
        public ColumnDefinition(String name, TypeEnum type) {
            this(name, type, null);
        }
        
        public ColumnDefinition(String name, TypeEnum type, TypeEnum elementType) {
            this.name = name;
            this.type = type;
            this.elementType = elementType;
        }

        public String getName() {
            return name;
        }

        public TypeEnum getType() {
            return type;
        }

        public TypeEnum getElementType() {
            return elementType;
        }
        
    }

    public BXML toXML() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement("results", null);
        while (this.next()) {
            OMElement resultElement = factory.createOMElement("result", null);
            for (ColumnDefinition col : this.columnDefs) {
                String value = null;
                switch (col.getType()) {
                case BOOLEAN:
                    value = String.valueOf(this.getBoolean(col.getName()));
                    break;
                case STRING:
                    value = this.getString(col.getName());
                    break;
                case INT:
                    value = String.valueOf(this.getInt(col.getName()));
                    break;
                case LONG:
                    value = String.valueOf(this.getLong(col.getName()));
                    break;
                case FLOAT:
                    value = String.valueOf(this.getFloat(col.getName()));
                    break;
                case DOUBLE:
                    value = String.valueOf(this.getDouble(col.getName()));
                    break;
                }
                OMElement element = factory.createOMElement(col.getName(), null);
                element.addChild(factory.createOMText(value));
                resultElement.addChild(element);
            }
            root.addChild(resultElement);
        }
        this.close();
        return new BXML(root);
    }
    
    /**
     * {@link JSONDataSource} implementation for DataTable.
     */
    private class DataTableJSONDataSource implements JSONDataSource {

        @Override
        public void serialize(JsonGenerator gen) throws IOException {
            String name;
            gen.writeStartArray();
            while (next()) {
                gen.writeStartObject();
                for (ColumnDefinition col : columnDefs) {
                    name = col.getName();
                    gen.writeFieldName(name);
                    switch (col.getType()) {
                    case STRING:
                        gen.writeString(getString(name));
                        break;
                    case INT:
                        gen.writeNumber(getInt(name));
                        break;
                    case LONG:
                        gen.writeNumber(getLong(name));
                        break;
                    case DOUBLE:
                        gen.writeNumber(getDouble(name));
                        break;
                    case FLOAT:
                        gen.writeNumber(getFloat(name));
                        break;
                    case BOOLEAN:
                        gen.writeBoolean(getBoolean(name));
                        break;
                    case ARRAY:
                        gen.writeStartArray();
                        //TODO: ARRAY
                        gen.writeEndArray();
                        break;
                    case JSON:
                        //TODO: JSON                        
                        break;
                    case MAP:
                        gen.writeStartObject();
                        //TODO: MAP
                        gen.writeEndObject();
                        break;
                    case XML:
                        //TODO: get XML
                        gen.writeString("");
                        break;
                    default:
                        gen.writeString(getString(name));
                        break;                    
                    }
                }
                gen.writeEndObject();
            }
            gen.writeEndArray();
            close();
        }
        
    }
    
}
