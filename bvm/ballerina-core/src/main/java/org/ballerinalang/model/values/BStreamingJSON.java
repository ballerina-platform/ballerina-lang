/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.util.JsonGenerator.JSONDataSource;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link BStreamingJSON} represent a JSON array generated from a {@link JSONDataSource}.
 * 
 * @since 0.981.0
 */
public class BStreamingJSON extends BRefValueArray {

    private JSONDataSource datasource;

    public BStreamingJSON(JSONDataSource datasource) {
        this.datasource = datasource;
        this.values = null;
        this.arrayType = new BArrayType(BTypes.typeJSON);
    }

    public void add(long index, BRefType<?> value) {
        if (values == null) {
            buildDatasource();
        }

        super.add(index, value);
    }

    public BRefType<?> get(long index) {
        if (values == null) {
            buildDatasource();
        }

        return super.get(index);
    }

    @Override
    public void serialize(OutputStream outputStream) {
        /*
         * Below order is important, where if the value is generated from a streaming data source,
         * it should be able to serialize the data out again using the value
         */
        try {
            JsonGenerator gen = new JsonGenerator(outputStream);
            if (this.values != null) {
                gen.serialize(this);
            } else {
                this.datasource.serialize(gen);
            }
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("error occurred while serializing data", e);
        }
    }

    @Override
    public BRefType<?>[] getValues() {
        if (values == null) {
            buildDatasource();
        }
        return values;
    }

    private void buildDatasource() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            JsonGenerator gen = new JsonGenerator(byteOut);
            this.datasource.serialize(gen);
            gen.flush();
            BRefValueArray json = (BRefValueArray) JsonParser.parse(new ByteArrayInputStream(byteOut.toByteArray()));
            this.values = json.getValues();
            this.size = json.size;
        } catch (Throwable t) {
            throw new BallerinaException("error occurred while building JSON: ", t);
        }
    }
    
    @Override
    public String stringValue() {
        if (values == null) {
            buildDatasource();
        }

        return super.stringValue();
    }
}
