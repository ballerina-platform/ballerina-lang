/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TableValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * {@link JSONDataSource} implementation for table.
 *
 * @since 1.0
 */
public class TableJSONDataSource implements JSONDataSource {

    private TableValue df;

    private JSONObjectGenerator objGen;

    public TableJSONDataSource(TableValue df) {
        this(df, new DefaultJSONObjectGenerator());
    }

    private TableJSONDataSource(TableValue df, JSONObjectGenerator objGen) {
        this.df = df;
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
        return this.df.hasNext();
    }

    @Override
    public Object next() {
        try {
            this.df.moveToNext();
            return this.objGen.transform(this.df);
        } catch (IOException e) {
            throw TableUtils.createTableOperationError(e, "error while geting next data");
        }
    }

    @Override
    public Object build() {
        ArrayValue values = new ArrayValue(new BArrayType(BTypes.typeJSON));
        while (this.hasNext()) {
            values.append(this.next());
        }
        return values;
    }

    /**
     * Default {@link TableJSONDataSource.JSONObjectGenerator} implementation based
     * on the table's in-built column definition.
     */
    private static class DefaultJSONObjectGenerator implements JSONObjectGenerator {

        @Override
        public Object transform(TableValue df) throws IOException {
            MapValue<String, Object> objNode = new MapValueImpl<>(new BMapType(BTypes.typeJSON));
            BStructureType structType = df.getStructType();
            BField[] structFields = null;
            if (structType != null) {
                structFields = structType.getFields().values().toArray(new BField[0]);
            }
            int index = 0;
            for (ColumnDefinition col : df.getColumnDefs()) {
                String name;
                if (structFields != null) {
                    name = structFields[index].getFieldName();
                } else {
                    name = col.getName();
                }
                constructJsonData(df, objNode, name, col.getTypeTag(), index + 1, structFields);
                ++index;
            }

            return objNode;
        }

    }

    private static void constructJsonData(TableValue df, MapValue<String, Object> jsonObject, String name, int typeTag,
                                          int index, BField[] structFields) {
        switch (typeTag) {
            case TypeTags.STRING_TAG:
                jsonObject.put(name, df.getString(index));
                break;
            case TypeTags.INT_TAG:
                Long intVal = df.getInt(index);
                jsonObject.put(name, intVal == null ? null : intVal);
                break;
            case TypeTags.FLOAT_TAG:
                Double floatVal = df.getFloat(index);
                jsonObject.put(name, floatVal == null ? null : floatVal);
                break;
            case TypeTags.DECIMAL_TAG:
                DecimalValue decimalVal = df.getDecimal(index);
                jsonObject.put(name, decimalVal == null ? null : decimalVal);
                break;
            case TypeTags.BOOLEAN_TAG:
                Boolean boolVal = df.getBoolean(index);
                jsonObject.put(name, boolVal == null ? null : boolVal);
                break;
            case TypeTags.BYTE_ARRAY_TAG:
                jsonObject.put(name, df.getBlob(index));
                break;
            case TypeTags.ARRAY_TAG:
                jsonObject.put(name, getDataArray(df, index));
                break;
            case TypeTags.JSON_TAG:
                jsonObject.put(name, df.getString(index) == null ? null : JSONParser.parse(df.getString(index)));
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                jsonObject.put(name, getStructData(df.getStruct(index), structFields, index));
                break;
            case TypeTags.XML_TAG:
                jsonObject.put(name, df.getString(index));
                break;
            default:
                jsonObject.put(name, df.getString(index));
            break;
        }
    }

    private static Object getStructData(Object[] data, BField[] structFields, int index) {
        try {
            if (structFields == null) {
                ArrayValue jsonArray = new ArrayValue(new BArrayType(BTypes.typeJSON));
                if (data != null) {
                    for (Object value : data) {
                        if (value instanceof String) {
                            jsonArray.append(new String((String) value));
                        } else if (value instanceof Boolean) {
                            jsonArray.append((Boolean) value);
                        } else if (value instanceof Long) {
                            jsonArray.append((long) value);
                        } else if (value instanceof Double) {
                            jsonArray.append((double) value);
                        } else if (value instanceof Integer) {
                            jsonArray.append((int) value);
                        } else if (value instanceof Float) {
                            jsonArray.append((float) value);
                        } else if (value instanceof DecimalValue) {
                            jsonArray.append(((DecimalValue) value).floatValue());
                        }
                    }
                }
                return jsonArray;
            } else {
                MapValue<String, Object> jsonData = new MapValueImpl<>();
                boolean structError = true;
                if (data != null) {
                    int i = 0;
                    for (Object value : data) {
                        BType internaltType = structFields[index - 1].type;
                        if (internaltType.getTag() == TypeTags.OBJECT_TYPE_TAG
                                || internaltType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                            BField[] internalStructFields =
                                    ((BStructureType) internaltType).getFields().values().toArray(new BField[0]);
                            if (internalStructFields != null) {
                                if (value instanceof BigDecimal) {
                                    jsonData.put(internalStructFields[i].name, ((BigDecimal) value).doubleValue());
                                } else if (value instanceof Struct) {
                                    jsonData.put(internalStructFields[i].name,
                                            getStructData(((Struct) value).getAttributes(), internalStructFields,
                                                    i + 1));
                                } else {
                                    jsonData.put(internalStructFields[i].name, value);
                                }
                                structError = false;
                            }
                        }
                        ++i;
                    }
                }
                if (structError) {
                    throw new BallerinaException("error in constructing the json object from struct type data");
                }
                
                return jsonData;
            }
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in retrieving struct data to construct the inner json object:" + e.getMessage());
        }
    }

    private static Object getDataArray(TableValue df, int columnIndex) {
        Object[] dataArray = df.getArray(columnIndex);
        int length = dataArray.length;
        ArrayValue jsonArray = new ArrayValue(new BArrayType(BTypes.typeJSON));
        if (length > 0) {
            Object obj = dataArray[0];
            if (obj instanceof String) {
                for (Object value : dataArray) {
                    jsonArray.append(value);
                }
            } else if (obj instanceof Boolean) {
                for (Object value : dataArray) {
                    jsonArray.append(value);
                }
            } else if (obj instanceof Integer) {
                for (Object value : dataArray) {
                    jsonArray.append(((Integer) value).longValue());
                }
            } else if (obj instanceof Long) {
                for (Object value : dataArray) {
                    jsonArray.append(value);
                }
            } else if (obj instanceof Float) {
                for (Object value : dataArray) {
                    jsonArray.append(((Float) value).doubleValue());
                }
            } else if (obj instanceof Double) {
                for (Object value : dataArray) {
                    jsonArray.append(value);
                }
            } else if (obj instanceof BigDecimal) {
                for (Object value : dataArray) {
                    if (value != null) {
                        jsonArray.append(((BigDecimal) value).doubleValue());
                    } else {
                        jsonArray.append(null);
                    }
                }
            }
        }
        return jsonArray;
    }

    /**
     * This represents the logic that will transform the current entry of a
     * data table to a JSON.
     */
    public static interface JSONObjectGenerator {

        /**
         * Converts the current position of the given table to a JSON.
         *
         * @param table The table that should be used in the current position
         * @return The generated JSON object
         * @throws IOException for JSON reading/serializing errors
         */
        Object transform(TableValue table) throws IOException;

    }
}
