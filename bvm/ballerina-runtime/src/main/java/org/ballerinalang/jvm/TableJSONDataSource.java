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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.api.BString;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link JSONDataSource} implementation for table.
 *
 * @since 1.0
 */
public class TableJSONDataSource implements JSONDataSource {

    private TableValueImpl tableValue;
    private JSONObjectGenerator objGen;

    TableJSONDataSource(TableValueImpl tableValue) {
        this(tableValue, new DefaultJSONObjectGenerator());
    }

    private TableJSONDataSource(TableValueImpl tableValue, JSONObjectGenerator objGen) {
        this.tableValue = tableValue;
        this.objGen = objGen;
    }

    @Override
    public void serialize(JSONGenerator gen) throws IOException {
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
        ArrayValue values = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        IteratorValue itr = this.tableValue.getIterator();
        while (itr.hasNext()) {
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            MapValueImpl record = ((MapValueImpl) tupleValue.get(0));
            try {
                values.append(this.objGen.transform(record));
            } catch (IOException e) {
                throw new BallerinaException(e);
            }
        }
        return values;
    }

    /**
     * Default {@link TableJSONDataSource.JSONObjectGenerator} implementation based
     * on the table's in-built column definition.
     */
    private static class DefaultJSONObjectGenerator implements JSONObjectGenerator {

        @Override
        public Object transform(MapValueImpl record) {
            MapValue<BString, Object> objNode = new MapValueImpl<>(new BMapType(BTypes.typeJSON));
            BStructureType structType = (BStructureType) record.getType();
            BField[] structFields = null;
            if (structType != null) {
                structFields = structType.getFields().values().toArray(new BField[0]);
            }
            Map<String, BField> internalStructFields = structType.getFields();
            if (structFields.length > 0) {
                Iterator<Map.Entry<String, BField>> itr = internalStructFields.entrySet().iterator();
                for (int i = 0; i < internalStructFields.size(); i++) {
                    BField internalStructField = itr.next().getValue();
                    int type = internalStructField.getFieldType().getTag();
                    String fieldName = internalStructField.getFieldName();
                    constructJsonData(record, objNode, fieldName, type, structFields, i);
                }
            }
            return objNode;
        }

    }

    private static void constructJsonData(MapValueImpl record, MapValue<BString, Object> jsonObject, String name,
                                          int typeTag, BField[] structFields, int index) {
        BString key = StringUtils.fromString(name);
        switch (typeTag) {
            case TypeTags.STRING_TAG:
                jsonObject.put(StringUtils.fromString(name), record.getStringValue(key));
                break;
            case TypeTags.INT_TAG:
                Long intVal = record.getIntValue(key);
                jsonObject.put(StringUtils.fromString(name), intVal);
                break;
            case TypeTags.FLOAT_TAG:
                Double floatVal = record.getFloatValue(key);
                jsonObject.put(StringUtils.fromString(name), floatVal);
                break;
            case TypeTags.DECIMAL_TAG:
                DecimalValue decimalVal = (DecimalValue) record.get(key);
                jsonObject.put(StringUtils.fromString(name), decimalVal);
                break;
            case TypeTags.BOOLEAN_TAG:
                Boolean boolVal = record.getBooleanValue(key);
                jsonObject.put(StringUtils.fromString(name), boolVal);
                break;
            case TypeTags.ARRAY_TAG:
                jsonObject.put(StringUtils.fromString(name), getDataArray(record, key));
                break;
            case TypeTags.JSON_TAG:
                jsonObject.put(StringUtils.fromString(name), record.getStringValue(key) == null ? null :
                        JSONParser.parse(record.getStringValue(key).toString()));
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                jsonObject.put(StringUtils.fromString(name),
                        getStructData(record.getMapValue(key), structFields, index, key));
                break;
            case TypeTags.XML_TAG:
                jsonObject.put(StringUtils.fromString(name), record.getStringValue(key));
                break;
            default:
                jsonObject.put(StringUtils.fromString(name), record.getStringValue(key));
                break;
        }
    }

    private static Object getStructData(MapValueImpl data, BField[] structFields, int index, BString key) {
        if (structFields == null) {
            ArrayValue jsonArray = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
            if (data != null) {
                ArrayValue dataArray = data.getArrayValue(key);
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
            MapValue<BString, Object> jsonData = new MapValueImpl<>(new BMapType(BTypes.typeJSON));
            boolean structError = true;
            if (data != null) {
                BType internalType = structFields[index].type;
                if (internalType.getTag() == TypeTags.OBJECT_TYPE_TAG
                        || internalType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    BField[] internalStructFields =
                            ((BStructureType) internalType).getFields().values().toArray(new BField[0]);
                    for (int i = 0; i < internalStructFields.length; i++) {
                        BString internalKeyName = StringUtils.fromString(internalStructFields[i].name);
                        Object value = data.get(internalKeyName);
                        if (value instanceof BigDecimal) {
                            jsonData.put(StringUtils.fromString(internalStructFields[i].name),
                                    ((BigDecimal) value).doubleValue());
                        } else if (value instanceof MapValueImpl) {
                            jsonData.put(StringUtils.fromString(internalStructFields[i].name),
                                    getStructData((MapValueImpl) value, internalStructFields, i, internalKeyName));
                        } else {
                            jsonData.put(StringUtils.fromString(internalStructFields[i].name), value);
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
        ArrayValue dataArray = df.getArrayValue(key);
        ArrayValue jsonArray = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
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
