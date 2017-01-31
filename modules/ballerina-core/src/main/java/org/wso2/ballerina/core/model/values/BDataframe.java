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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.wso2.ballerina.core.model.DataIterator;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.connectors.data.DataTableOMDataSource;

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
    private static OMFactory omFactory;

    static {
        omFactory = OMAbstractFactory.getOMFactory();
    }

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

    public String getObjectAsString(int index) {
        return iterator.getObjectAsString(index);
    }

    public String getObjectAsString(String columnName) {
        return iterator.getObjectAsString(columnName);
    }

    public String[] getAvailableProprtyNames() {
        return properties.keySet().toArray(new String[properties.keySet().size()]);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public BJSON toJSON() {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObj;
        while (this.next()) {
            jsonObj = new JsonObject();
            for (ColumnDefinition col : this.columnDefs) {
                if (col.getType().equals(TypeEnum.STRING)) {
                    jsonObj.addProperty(col.getName(), this.getString(col.getName()));
                } else if (col.getType().equals(TypeEnum.ARRAY)) {
                    //TODO: get array
                    jsonObj.add(col.getName(), this.createJsonArray(col.getElementType(), null));
                } else if (col.getType().equals(TypeEnum.BOOLEAN)) {
                    jsonObj.addProperty(col.getName(), this.getBoolean(col.getName()));
                } else if (col.getType().equals(TypeEnum.BOOLEAN)) {
                    jsonObj.addProperty(col.getName(), this.getBoolean(col.getName()));
                } else if (col.getType().equals(TypeEnum.DOUBLE)) {
                    jsonObj.addProperty(col.getName(), this.getDouble(col.getName()));
                } else if (col.getType().equals(TypeEnum.FLOAT)) {
                    jsonObj.addProperty(col.getName(), this.getFloat(col.getName()));
                } else if (col.getType().equals(TypeEnum.INT)) {
                    jsonObj.addProperty(col.getName(), this.getInt(col.getName()));
                } else if (col.getType().equals(TypeEnum.LONG)) {
                    jsonObj.addProperty(col.getName(), this.getLong(col.getName()));
                } else if (col.getType().equals(TypeEnum.JSON)) {
                    //TODO: get JSON
                    jsonObj.addProperty(col.getName(), 1);
                } else if (col.getType().equals(TypeEnum.XML)) {
                    //TODO: get XML
                    jsonObj.addProperty(col.getName(), 1);
                } else if (col.getType().equals(TypeEnum.MAP)) {
                    jsonObj.add(col.getName(), this.createJsonObject(null));
                }
            }
            jsonArray.add(jsonObj);
        }
        this.close();
        return new BJSON(jsonArray);
    }
        
    @SuppressWarnings("unchecked")
    private JsonArray createJsonArray(TypeEnum elementType, BArray<BValue> arrayValue) {
        JsonArray result = new JsonArray();
        int count = arrayValue.size();
        BValue value;
        for (int i = 0; i < count; i++) {
            value = arrayValue.get(i);
            if (elementType.equals(TypeEnum.STRING)) {
                result.add(value.stringValue());
            } else if (elementType.equals(TypeEnum.BOOLEAN)) {
                result.add(((BBoolean) value).booleanValue());
            } else if (elementType.equals(TypeEnum.DOUBLE)) {
                result.add(((BDouble) value).doubleValue());
            } else if (elementType.equals(TypeEnum.FLOAT)) {
                result.add(((BFloat) value).floatValue());
            } else if (elementType.equals(TypeEnum.INT)) {
                result.add(((BInteger) value).intValue());
            } else if (elementType.equals(TypeEnum.LONG)) {
                result.add(((BLong) value).longValue());
            } else if (elementType.equals(TypeEnum.MAP)) {
                result.add(this.createJsonObject((BMap<BString, BValue>) value));
            } else if (elementType.equals(TypeEnum.JSON)) {
                result.add(((BJSON) value).value());
            } else if (elementType.equals(TypeEnum.XML)) {
                result.add(((BXML) value).stringValue());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private JsonObject createJsonObject(BMap<BString, BValue> map) {
        JsonObject result = new JsonObject();
        BValue value;
        String keyStr;
        for (BString key : map.keySet()) {
            value = map.get(key);
            keyStr = key.stringValue();
            if (value instanceof BString) {
                result.addProperty(keyStr, value.stringValue());
            } else if (value instanceof BJSON) {
                result.add(keyStr, ((BJSON) value).value());
            } else if (value instanceof BXML) {
                result.addProperty(keyStr, ((BXML) value).stringValue());
            } else if (value instanceof BInteger) {
                result.addProperty(keyStr, ((BInteger) value).intValue());
            } else if (value instanceof BLong) {
                result.addProperty(keyStr, ((BLong) value).longValue());
            } else if (value instanceof BFloat) {
                result.addProperty(keyStr, ((BFloat) value).floatValue());
            } else if (value instanceof BDouble) {
                result.addProperty(keyStr, ((BDouble) value).doubleValue());
            } else if (value instanceof BBoolean) {
                result.addProperty(keyStr, ((BBoolean) value).booleanValue());
            } else if (value instanceof BMap) {
                result.add(keyStr, this.createJsonObject((BMap<BString, BValue>) value));
            } 
        }
        return result;
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
        return new BXML(omFactory.createOMElement(new DataTableOMDataSource(this)));
    }

    public List<ColumnDefinition> getColumnDefs() {
        return columnDefs;
    }
}
