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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BDataTable.ColumnDefinition;
import org.ballerinalang.model.values.BJSON.JSONDataSource;

import java.io.IOException;

/**
 * {@link org.ballerinalang.model.values.BJSON.JSONDataSource} implementation for DataTable.
 *
 * @since 0.8.0
 */
public class DataTableJSONDataSource implements JSONDataSource {

    private BDataTable df;

    private JSONObjectGenerator objGen;

    public DataTableJSONDataSource(BDataTable df) {
        this(df, new DefaultJSONObjectGenerator());
    }

    public DataTableJSONDataSource(BDataTable df, JSONObjectGenerator objGen) {
        this.df = df;
        this.objGen = objGen;
    }

    @Override
    public void serialize(JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        while (this.df.next()) {
            this.objGen.transform(this.df).serialize(gen, null);
        }
        gen.writeEndArray();
        this.df.close();
    }

    /**
     * Default {@link DataTableJSONDataSource.JSONObjectGenerator} implementation based
     * on the datatable's in-built column definition.
     */
    private static class DefaultJSONObjectGenerator implements JSONObjectGenerator {

        @Override
        public JsonNode transform(BDataTable df) throws IOException {
            JsonNodeFactory fac = JsonNodeFactory.instance;
            ObjectNode objNode = fac.objectNode();
            String name;
            for (ColumnDefinition col : df.getColumnDefs()) {
                name = col.getName();
                switch (col.getType()) {
                case STRING:
                    objNode.put(name, df.getString(name));
                    break;
                case INT:
                    objNode.put(name, df.getInt(name));
                    break;
                case LONG:
                    objNode.put(name, df.getLong(name));
                    break;
                case DOUBLE:
                    objNode.put(name, df.getDouble(name));
                    break;
                case FLOAT:
                    objNode.put(name, df.getFloat(name));
                    break;
                case BOOLEAN:
                    objNode.put(name, df.getBoolean(name));
                    break;
                case ARRAY:
                    //TODO: ARRAY
                    break;
                case JSON:
                    objNode.set(name, new ObjectMapper().readTree(""));
                    break;
                case MAP:
                    //TODO: MAP
                    break;
                case XML:
                    objNode.put(name, "");
                    //TODO: get XML
                    break;
                default:
                    objNode.put(name, df.getString(name));
                    break;
                }
            }
            return objNode;
        }

    }

    /**
     * This represents the logic that will transform the current entry of a
     * data table to a {@link com.fasterxml.jackson.databind.JsonNode}.
     */
    public static interface JSONObjectGenerator {

        /**
         * Converts the current position of the given datatable to a JSON object.
         *
         * @param dataframe The datatable that should be used in the current position
         * @return The generated JSON object
         * @throws java.io.IOException
         */
        JsonNode transform(BDataTable dataframe) throws IOException;

    }

}
