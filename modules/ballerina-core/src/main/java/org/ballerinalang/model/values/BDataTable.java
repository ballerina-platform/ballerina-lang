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
package org.ballerinalang.model.values;

import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.DataTableJSONDataSource;
import org.ballerinalang.model.DataTableOMDataSource;
import org.ballerinalang.model.types.TypeEnum;

import java.util.List;
import java.util.Map;

/**
 * The {@code BDataTable} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BDataTable implements BRefType<Object> {

    private DataIterator iterator;
    private Map<String, Object> properties;
    private List<ColumnDefinition> columnDefs;

    public BDataTable(DataIterator dataIterator, Map<String, Object> properties, List<ColumnDefinition> columnDefs) {
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

    public long getLong(int index) {
        return iterator.getLong(index);
    }

    public long getLong(String columnName) {
        return iterator.getLong(columnName);
    }

    public int getInt(int index) {
        return iterator.getInt(index);
    }

    public int getInt(String columnName) {
        return iterator.getInt(columnName);
    }

    public float getFloat(int index) {
        return iterator.getFloat(index);
    }

    public float getFloat(String columnName) {
        return iterator.getFloat(columnName);
    }

    public double getDouble(int index) {
        return iterator.getDouble(index);
    }

    public double getDouble(String columnName) {
        return iterator.getDouble(columnName);
    }

    public boolean getBoolean(int index) {
        return iterator.getBoolean(index);
    }

    public boolean getBoolean(String columnName) {
        return iterator.getBoolean(columnName);
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

    public Map<String, Object> getArray(int index) {
        return iterator.getArray(index);
    }

    public Map<String, Object> getArray(String columnName) {
        return iterator.getArray(columnName);
    }
    
    public BJSON toJSON() {
        return new BJSON(new DataTableJSONDataSource(this));
    }

    public BXML toXML(String rootWrapper, String rowWrapper) {
        OMSourcedElementImpl omSourcedElement = new OMSourcedElementImpl();
        omSourcedElement.init(new DataTableOMDataSource(this, rootWrapper, rowWrapper));
        return new BXML(omSourcedElement);
    }

    public List<ColumnDefinition> getColumnDefs() {
        return columnDefs;
    }
    
    /**
     * This represents a column definition for a column in a datatable.
     */
    public static class ColumnDefinition {
        
        private String name;
        private TypeEnum type;

        public ColumnDefinition(String name, TypeEnum type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public TypeEnum getType() {
            return type;
        }
    }
}
