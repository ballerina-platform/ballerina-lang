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
package org.wso2.ballerina.core.model;

import com.fasterxml.jackson.core.JsonGenerator;

import org.wso2.ballerina.core.model.values.BDataframe;
import org.wso2.ballerina.core.model.values.BDataframe.ColumnDefinition;
import org.wso2.ballerina.core.model.values.BJSON.JSONDataSource;

import java.io.IOException;

/**
 * {@link JSONDataSource} implementation for DataTable.
 */
public class DataTableJSONDataSource implements JSONDataSource {

    private BDataframe df;
    
    public DataTableJSONDataSource(BDataframe df) {
        this.df = df;
    }
    
    @Override
    public void serialize(JsonGenerator gen) throws IOException {
        String name;
        gen.writeStartArray();
        while (this.df.next()) {
            gen.writeStartObject();
            for (ColumnDefinition col : this.df.getColumnDefs()) {
                name = col.getName();
                gen.writeFieldName(name);
                switch (col.getType()) {
                case STRING:
                    gen.writeString(this.df.getString(name));
                    break;
                case INT:
                    gen.writeNumber(this.df.getInt(name));
                    break;
                case LONG:
                    gen.writeNumber(this.df.getLong(name));
                    break;
                case DOUBLE:
                    gen.writeNumber(this.df.getDouble(name));
                    break;
                case FLOAT:
                    gen.writeNumber(this.df.getFloat(name));
                    break;
                case BOOLEAN:
                    gen.writeBoolean(this.df.getBoolean(name));
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
                    gen.writeString(this.df.getString(name));
                    break;                    
                }
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
        this.df.close();
    }
    
}
