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
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.io.IOException;
import java.util.Map;

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
            BMap record = ((BMap) tupleValue.get(1));
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
        public Object transform(BMap<?, ?> record) {
            MapValue<BString, Object> objNode = new MapValueImpl<>(new BMapType(PredefinedTypes.TYPE_JSON));
            for (Map.Entry entry : record.entrySet()) {
                Type type = TypeUtils.getReferredType(TypeChecker.getType(entry.getValue()));
                BString keyName = StringUtils.fromString(entry.getKey().toString());
                constructJsonData(record, objNode, keyName, type);
            }
            return objNode;
        }

    }

    private static void constructJsonData(BMap<?, ?> record, MapValue<BString, Object> jsonObject,
                                          BString key, Type type) {
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
                jsonObject.put(key, getDataArray(record.getArrayValue(key)));
                break;
            case TypeTags.JSON_TAG:
                jsonObject.put(key, record.getStringValue(key) == null ? null :
                        JsonParser.parse(record.getStringValue(key).toString()));
                break;
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                MapValue<BString, Object> jsonData = new MapValueImpl<>(new BMapType(PredefinedTypes.TYPE_JSON));
                for (Map.Entry entry : record.getMapValue(key).entrySet()) {
                    Type internalType = TypeUtils.getReferredType(TypeChecker.getType(entry.getValue()));
                    BString internalKeyName = StringUtils.fromString(entry.getKey().toString());
                    constructJsonData(record.getMapValue(key), jsonData, internalKeyName, internalType);
                }
                jsonObject.put(key, jsonData);
                break;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                BString strVal = StringUtils.fromString(StringUtils.getStringValue(record.get(key), null));
                jsonObject.put(key, strVal);
                break;
            default:
                throw new BallerinaException("cannot construct json object from '" + type + "' type data");
        }
    }

    private static Object getDataArray(BArray dataArray) {
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
        Object transform(BMap<?, ?> record) throws IOException;

    }
}
