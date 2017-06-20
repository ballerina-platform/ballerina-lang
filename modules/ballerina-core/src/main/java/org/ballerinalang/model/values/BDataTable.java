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

import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.types.TypeTags;

import java.sql.Types;
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
    private int columnCount;
    private BStruct bStruct;

    public BDataTable(DataIterator dataIterator, List<ColumnDefinition> columnDefs) {
        this.iterator = dataIterator;
        this.columnDefs = columnDefs;
        this.columnCount = columnDefs.size();
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

    public boolean next() {
        return iterator.next();
    }

    public void close(boolean isInTransaction) {
        iterator.close(isInTransaction);
    }

    public BStruct getNext() {
        BValue[] dataArray = new BValue[this.columnCount];
        int index = 0;
        for (ColumnDefinition columnDef : columnDefs) {
            BValue value;
            String columnName = columnDef.getName();
            int sqlType = columnDef.getSQLType();
            switch (sqlType) {
            case Types.ARRAY:
                value = getDataArray(columnName);
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                value = new BString(iterator.getString(columnName));
                break;
            case Types.CLOB:
            case Types.NCLOB:
            case Types.BLOB:
            case Types.BINARY:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                value = iterator.get(columnName, sqlType);
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                value = new BInteger(iterator.getInt(columnName));
                break;
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                value = new BFloat(iterator.getFloat(columnName));
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                value = new BBoolean(iterator.getBoolean(columnName));
                break;
            default:
                value = null;
            }
            dataArray[index] = value;
            ++index;
        }
        bStruct.setMemoryBlock(dataArray);
        BLangVM.prepareStructureTypeFromNativeAction(bStruct);
        return bStruct;
    }

    private BMap<BString, BValue> getDataArray(String columnName) {
        Map<String, Object> arrayMap = iterator.getArray(columnName);
        BMap<BString, BValue> returnMap = new BMap<>();
        if (arrayMap != null && !arrayMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : arrayMap.entrySet()) {
                BString key = new BString(entry.getKey());
                Object obj = entry.getValue();
                if (obj instanceof String) {
                    returnMap.put(key, new BString(String.valueOf(obj)));
                } else if (obj instanceof Boolean) {
                    returnMap.put(key, new BBoolean(Boolean.valueOf(obj.toString())));
                } else if (obj instanceof Integer) {
                    returnMap.put(key, new BInteger(Integer.parseInt(obj.toString())));
                } else if (obj instanceof Long) {
                    returnMap.put(key, new BInteger(Long.parseLong(obj.toString())));
                } else if (obj instanceof Float) {
                    returnMap.put(key, new BFloat(Float.parseFloat(obj.toString())));
                } else if (obj instanceof Double) {
                    returnMap.put(key, new BFloat(Double.parseDouble(obj.toString())));
                }
            }
        }
        return returnMap;
    }

    private void generateStruct() {
        BStructType structType = new BStructType("RS", null);
        BStruct bStruct = new BStruct(structType);

        BType[] structTypes = new BType[columnDefs.size()];
        BStructType.StructField[] structFields = new BStructType.StructField[columnDefs.size()];
        int typeIndex  = 0;
        for (ColumnDefinition columnDef : columnDefs) {
            BType type;
            switch (columnDef.getSQLType()) {
            case Types.ARRAY:
                type = BTypes.typeMap;
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.BINARY:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                type = BTypes.typeString;
                break;
            case Types.BLOB:
                type = BTypes.typeBlob;
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                type = BTypes.typeInt;
                break;
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                type = BTypes.typeFloat;
                break;
            case Types.BIT:
            case Types.BOOLEAN:
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
        bStruct.init(fieldCount);
        bStruct.setFieldTypes(structTypes);
        ((BStructType) bStruct.getType()).setStructFields(structFields);
        this.bStruct = bStruct;
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
        private TypeEnum mappedType;
        private int sqlType;

        public ColumnDefinition(String name, TypeEnum mappedType, int sqlType) {
            this.name = name;
            this.mappedType = mappedType;
            this.sqlType = sqlType;
        }

        public String getName() {
            return name;
        }

        public TypeEnum getType() {
            return mappedType;
        }

        public int getSQLType() {
            return sqlType;
        }

    }

    @Override
    public BValue copy() {
        return null;
    }
}
