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

import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;

import java.util.List;
import java.util.Map;

/**
 * The {@code BDataTable} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BDataTable implements BRefType<Object> {

    private DataIterator iterator;
    private List<ColumnDefinition> columnDefs;
    private BStruct bStruct;

    public BDataTable(DataIterator dataIterator, List<ColumnDefinition> columnDefs) {
        this.iterator = dataIterator;
        this.columnDefs = columnDefs;
        generateStruct();
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeDatatable;
    }

    public boolean hasNext(boolean isInTransaction) {
        boolean hasNext = iterator.next();
        if (!hasNext) {
            close(isInTransaction);
        }
        return hasNext;
    }

    public void close(boolean isInTransaction) {
        iterator.close(isInTransaction);
    }

    public BStruct getNext() {
        iterator.generateNext(columnDefs, bStruct);
        return bStruct;
    }

    private void generateStruct() {
        BType[] structTypes = new BType[columnDefs.size()];
        BStructType.StructField[] structFields = new BStructType.StructField[columnDefs.size()];
        int typeIndex  = 0;
        for (ColumnDefinition columnDef : columnDefs) {
            BType type;
            switch (columnDef.getType()) {
            case ARRAY:
                type = BTypes.typeMap;
                break;
            case STRING:
                type = BTypes.typeString;
                break;
            case BLOB:
                type = BTypes.typeBlob;
                break;
            case INT:
                type = BTypes.typeInt;
                break;
            case FLOAT:
                type = BTypes.typeFloat;
                break;
            case BOOLEAN:
                type = BTypes.typeBoolean;
                break;
            default:
                type = BTypes.typeNull;
            }
            structTypes[typeIndex] = type;
            structFields[typeIndex] = new BStructType.StructField(type, columnDef.getName());
            ++typeIndex;
        }

        int[] fieldCount = populateMaxSizes(structTypes);
        BStructType structType = new BStructType("RS", null);
        structType.setStructFields(structFields);
        structType.setFieldTypeCount(fieldCount);

        this.bStruct = new BStruct(structType);
    }

    private static int[] populateMaxSizes(BType[] paramTypes) {
        int[] maxSizes = new int[6];
        for (int i = 0; i < paramTypes.length; i++) {
            BType paramType = paramTypes[i];
            switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                ++maxSizes[0];
                break;
            case TypeTags.FLOAT_TAG:
                ++maxSizes[1];
                break;
            case TypeTags.STRING_TAG:
                ++maxSizes[2];
                break;
            case TypeTags.BOOLEAN_TAG:
                ++maxSizes[3];
                break;
            case TypeTags.BLOB_TAG:
                ++maxSizes[4];
                break;
            default:
                ++maxSizes[5];
            }
        }
        return maxSizes;
    }


    public String getString(String columnName) {
        return iterator.getString(columnName);
    }

    public long getInt(String columnName) {
        return iterator.getInt(columnName);
    }

    public double getFloat(String columnName) {
        return iterator.getFloat(columnName);
    }

    public boolean getBoolean(String columnName) {
        return iterator.getBoolean(columnName);
    }

    public String getObjectAsString(String columnName) {
        return iterator.getObjectAsString(columnName);
    }

    public Map<String, Object> getArray(String columnName) {
        return iterator.getArray(columnName);
    }

    public List<ColumnDefinition> getColumnDefs() {
        return columnDefs;
    }

    /**
     * This represents a column definition for a column in a datatable.
     */
    public static class ColumnDefinition {

        private String name;
        private TypeKind mappedType;

        public ColumnDefinition(String name, TypeKind mappedType) {
            this.name = name;
            this.mappedType = mappedType;
        }

        public String getName() {
            return name;
        }

        public TypeKind getType() {
            return mappedType;
        }
    }

    @Override
    public BValue copy() {
        return null;
    }
}
