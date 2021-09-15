/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStructureType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * {@link JsonDataSource} implementation for table.
 *
 * @since 1.0
 */
public class TableJsonDataSource implements JsonDataSource {

    private BTable tableValue;
    private JSONObjectGenerator objGen;

    public TableJsonDataSource(BTable tableValue) {
        this(tableValue, new DefaultJSONObjectGenerator());
    }

    private TableJsonDataSource(BTable tableValue, JSONObjectGenerator objGen) {
        this.tableValue = tableValue;
        this.objGen = objGen;
    }

    @Override
    public void serialize(JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        while (this.hasNext()) {
            gen.serialize(this.next());
        }
        gen.writeEndArray();
    }

    @Override
    public boolean hasNext() {
        return this.tableValue.getIterator().hasNext();
    }

    @Override
    public Object next() {
        return this.tableValue.getIterator().next();
    }

    @Override
    public Object build() {
        ArrayValue values = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_JSON));
        BIterator itr = this.tableValue.getIterator();
        while (itr.hasNext()) {
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            //Retrieve table value from key-value tuple
            MapValueImpl record = ((MapValueImpl) tupleValue.get(1));
            try {
                values.append(this.objGen.transform(record));
            } catch (IOException e) {
                throw new BallerinaException(e);
            }
        }
        return values;
    }

    /**
     * Default {@link TableJsonDataSource.JSONObjectGenerator} implementation based
     * on the table's in-built column definition.
     */
    private static class DefaultJSONObjectGenerator implements JSONObjectGenerator {

        @Override
        public Object transform(MapValueImpl record) {
            MapValue<BString, Object> objNode = new MapValueImpl<>(new BMapType(PredefinedTypes.TYPE_JSON));
            BStructureType structType = (BStructureType) record.getType();
            LinkedList<Field> structFields = new LinkedList<>();
            if (structType != null) {
                structFields.addAll(structType.getFields().values());
                if (record.size() > structFields.size()) {
                    Type restFieldType = ((BRecordType) structType).getRestFieldType();
                    for (int i = structFields.size(); i < record.size(); i++) {
                        structFields.add(TypeCreator.createField(restFieldType, record.getKeys()[i].toString(), 0));
                    }
                }
            }
            if (structFields.size() > 0) {
                Iterator<Field> itr = structFields.iterator();
                for (int i = 0; i < structFields.size(); i++) {
                    Field structField = itr.next();
                    Type type = structField.getFieldType();
                    String fieldName = structField.getFieldName();
                    constructJsonData(record, objNode, fieldName, type, structFields, i);
                }
            }
            return objNode;
        }

    }

    private static void constructJsonData(MapValueImpl record, MapValue<BString, Object> jsonObject, String name,
                                          Type type, LinkedList<Field> structFields, int index) {
        BString key = StringUtils.fromString(name);
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
                jsonObject.put(key, record.getStringValue(key));
                break;
            case TypeTags.INT_TAG:
                Long intVal = record.getIntValue(key);
                jsonObject.put(key, intVal);
                break;
            case TypeTags.BYTE_TAG:
                Integer byteVal = (Integer) record.get(key);
                jsonObject.put(key, byteVal);
                break;
            case TypeTags.FLOAT_TAG:
                Double floatVal = record.getFloatValue(key);
                jsonObject.put(key, floatVal);
                break;
            case TypeTags.DECIMAL_TAG:
                DecimalValue decimalVal = (DecimalValue) record.get(key);
                jsonObject.put(key, decimalVal);
                break;
            case TypeTags.BOOLEAN_TAG:
                Boolean boolVal = record.getBooleanValue(key);
                jsonObject.put(key, boolVal);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                jsonObject.put(key, getDataArray(record, key));
                break;
            case TypeTags.JSON_TAG:
                jsonObject.put(key, record.getStringValue(key) == null ? null :
                        JsonParser.parse(record.getStringValue(key).toString()));
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                jsonObject.put(key, getStructData(record.getMapValue(key), structFields, index, key));
                break;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                BString strVal = StringUtils.fromString(StringUtils.getStringValue(record.get(key), null));
                jsonObject.put(key, strVal);
                break;
            case TypeTags.MAP_TAG:
                jsonObject.put(key, record.getMapValue(key));
                break;
            default:
                throw new BallerinaException("cannot construct the json object from '" + type + "' type data");
        }
    }

    private static Object getStructData(BMap data, LinkedList<Field> structFields, int index, BString key) {
        if (structFields == null) {
            ArrayValue jsonArray = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_JSON));
            if (data != null) {
                BArray dataArray = data.getArrayValue(key);
                for (int i = 0; i < dataArray.size(); i++) {
                    Object value = dataArray.get(i);
                    if (value instanceof String) {
                        jsonArray.append(value);
                    } else if (value instanceof Boolean) {
                        jsonArray.append(value);
                    } else if (value instanceof Long) {
                        jsonArray.append(value);
                    } else if (value instanceof Double) {
                        jsonArray.append(value);
                    } else if (value instanceof Integer) {
                        jsonArray.append(value);
                    } else if (value instanceof Float) {
                        jsonArray.append(value);
                    } else if (value instanceof DecimalValue) {
                        jsonArray.append(((DecimalValue) value).floatValue());
                    }
                }
            }
            return jsonArray;
        } else {
            MapValue<BString, Object> jsonData = new MapValueImpl<>(new BMapType(PredefinedTypes.TYPE_JSON));
            boolean structError = true;
            if (data != null) {
                Type internalType = structFields.get(index).getFieldType();
                if (internalType.getTag() == TypeTags.OBJECT_TYPE_TAG
                        || internalType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    LinkedList<Field> internalStructFields = new LinkedList<>();
                    internalStructFields.addAll(((BStructureType) internalType).getFields().values());
                    for (int i = 0; i < internalStructFields.size(); i++) {
                        BString internalKeyName = StringUtils.fromString(internalStructFields.get(i).getFieldName());
                        Object value = data.get(internalKeyName);
                        if (value instanceof BigDecimal) {
                            jsonData.put(internalKeyName, ((BigDecimal) value).doubleValue());
                        } else if (value instanceof MapValueImpl) {
                            jsonData.put(internalKeyName,
                                         getStructData((MapValueImpl) value, internalStructFields, i, internalKeyName));
                        } else {
                            jsonData.put(internalKeyName, value);
                        }
                        structError = false;
                    }
                }
            }
            if (structError) {
                throw new BallerinaException("error in constructing the json object from struct type data");
            }
            return jsonData;
        }
    }

    private static Object getDataArray(MapValue df, BString key) {
        BArray dataArray = df.getArrayValue(key);
        ArrayValue jsonArray = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_JSON));
        for (int i = 0; i < dataArray.size(); i++) {
            jsonArray.append(dataArray.get(i));
        }
        return jsonArray;
    }

    /**
     * This represents the logic that will transform the current entry of a
     * data table to a JSON.
     */
    public interface JSONObjectGenerator {

        /**
         * Converts the current position of the given table to a JSON.
         *
         * @param record The record that should be used in the current position
         * @return The generated JSON object
         * @throws IOException for JSON reading/serializing errors
         */
        Object transform(MapValueImpl record) throws IOException;

    }
}
