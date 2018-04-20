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
package org.ballerinalang.model;

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.JsonNode.Type;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BJSON.JSONDataSource;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * {@link org.ballerinalang.model.values.BJSON.JSONDataSource} implementation for table.
 *
 * @since 0.8.0
 */
public class TableJSONDataSource implements JSONDataSource {

    private BTable df;

    private JSONObjectGenerator objGen;

    private boolean isInTransaction;

    public TableJSONDataSource(BTable df, boolean isInTransaction) {
        this(df, new DefaultJSONObjectGenerator(), isInTransaction);
    }

    private TableJSONDataSource(BTable df, JSONObjectGenerator objGen, boolean isInTransaction) {
        this.df = df;
        this.objGen = objGen;
        this.isInTransaction = isInTransaction;
    }

    @Override
    public void serialize(JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        while (this.df.hasNext(this.isInTransaction)) {
            this.df.next();
            this.objGen.transform(this.df).serialize(gen);
        }
        gen.writeEndArray();
        this.df.close(this.isInTransaction);
    }

    /**
     * Default {@link TableJSONDataSource.JSONObjectGenerator} implementation based
     * on the table's in-built column definition.
     */
    private static class DefaultJSONObjectGenerator implements JSONObjectGenerator {

        @Override
        public JsonNode transform(BTable df) throws IOException {
            JsonNode objNode = new JsonNode(Type.OBJECT);
            BStructType structType = df.getStructType();
            BStructType.StructField[] structFields = null;
            if (structType != null) {
                structFields = structType.getStructFields();
            }
            int index = 0;
            for (ColumnDefinition col : df.getColumnDefs()) {
                String name;
                if (structFields != null) {
                    name = structFields[index].getFieldName();
                } else {
                    name = col.getName();
                }
                constructJsonData(df, objNode, name, col.getType(), index + 1, structFields);
                ++index;
            }

            return objNode;
        }

    }

    private static void constructJsonData(BTable df, JsonNode objNode, String name, TypeKind type, int index,
            BStructType.StructField[] structFields) {
        switch (type) {
        case STRING:
            objNode.set(name, df.getString(index));
            break;
        case INT:
            objNode.set(name, df.getInt(index));
            break;
        case FLOAT:
            objNode.set(name, df.getFloat(index));
            break;
        case BOOLEAN:
            objNode.set(name, df.getBoolean(index));
            break;
        case BLOB:
            objNode.set(name, df.getBlob(index));
            break;
        case ARRAY:
            objNode.set(name, getDataArray(df, index));
            break;
        case JSON:
            objNode.set(name, JsonParser.parse(df.getString(index)));
            break;
        case STRUCT:
            objNode.set(name, getStructData(df.getStruct(index), structFields, index));
            break;
        case XML:
            objNode.set(name, df.getString(index));
            break;
        default:
            objNode.set(name, df.getString(index));
            break;
        }
    }

    private static JsonNode getStructData(Object[] data, BStructType.StructField[] structFields, int index) {
        JsonNode jsonData = null;
        try {
            if (structFields == null) {
                jsonData = new JsonNode(Type.ARRAY);
                if (data != null) {
                    for (Object value : data) {
                        if (value instanceof String) {
                            jsonData.add((String) value);
                        } else if (value instanceof Boolean) {
                            jsonData.add((Boolean) value);
                        } else if (value instanceof Long) {
                            jsonData.add((long) value);
                        } else if (value instanceof Double) {
                            jsonData.add((double) value);
                        } else if (value instanceof Integer) {
                            jsonData.add((int) value);
                        } else if (value instanceof Float) {
                            jsonData.add((float) value);
                        } else if (value instanceof BigDecimal) {
                            jsonData.add(((BigDecimal) value).doubleValue());
                        }
                    }
                }
            } else {
                jsonData = new JsonNode(Type.OBJECT);
                boolean structError = true;
                if (data != null) {
                    int i = 0;
                    for (Object value : data) {
                        BType internaltType = structFields[index - 1].fieldType;
                        if (internaltType.getTag() == TypeTags.STRUCT_TAG) {
                            BStructType.StructField[] interanlStructFields = ((BStructType) internaltType)
                                    .getStructFields();
                            if (interanlStructFields != null) {
                                if (value instanceof String) {
                                    jsonData.set(interanlStructFields[i].fieldName, (String) value);
                                } else if (value instanceof Boolean) {
                                    jsonData.set(interanlStructFields[i].fieldName, (Boolean) value);
                                } else if (value instanceof Long) {
                                    jsonData.set(interanlStructFields[i].fieldName, (long) value);
                                } else if (value instanceof Double) {
                                    jsonData.set(interanlStructFields[i].fieldName, (double) value);
                                } else if (value instanceof Integer) {
                                    jsonData.set(interanlStructFields[i].fieldName, (int) value);
                                } else if (value instanceof Float) {
                                    jsonData.set(interanlStructFields[i].fieldName, (float) value);
                                } else if (value instanceof BigDecimal) {
                                    jsonData.set(interanlStructFields[i].fieldName, ((BigDecimal) value).doubleValue());
                                } else if (value instanceof Struct) {
                                    jsonData.set(interanlStructFields[i].fieldName,
                                            getStructData(((Struct) value).getAttributes(), interanlStructFields,
                                                    i + 1));
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
            }
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in retrieving struct data to construct the inner json object:" + e.getMessage());
        }
        return jsonData;
    }

    private static JsonNode getDataArray(BTable df, int columnIndex) {
        Object[] dataArray = df.getArray(columnIndex);
        int length = dataArray.length;
        JsonNode jsonArray = new JsonNode(Type.ARRAY);
        if (length > 0) {
            Object obj = dataArray[0];
            if (obj instanceof String) {
                for (Object value  : dataArray) {
                    jsonArray.add((String) value);
                }
            } else if (obj instanceof Boolean) {
                for (Object value  : dataArray) {
                    jsonArray.add((Boolean) value);
                }
            } else if (obj instanceof Integer) {
                for (Object value  : dataArray) {
                    jsonArray.add((int) value);
                }
            } else if (obj instanceof Long) {
                for (Object value  : dataArray) {
                    jsonArray.add((long) value);
                }
            } else if (obj instanceof Float) {
                for (Object value  : dataArray) {
                    jsonArray.add((float) value);
                }
            } else if (obj instanceof Double) {
                for (Object value  : dataArray) {
                    jsonArray.add((double) value);
                }
            }
        }
        return  jsonArray;
    }

    /**
     * This represents the logic that will transform the current entry of a
     * data table to a {@link JsonNode}.
     */
    public static interface JSONObjectGenerator {

        /**
         * Converts the current position of the given table to a JSON object.
         *
         * @param table The table that should be used in the current position
         * @return The generated JSON object
         * @throws IOException for json reading/serializing errors
         */
        JsonNode transform(BTable table) throws IOException;

    }

}
