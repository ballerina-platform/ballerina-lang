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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.util.exceptions.BallerinaException;

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
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        for (ColumnDefinition columnDef : columnDefs) {
            String columnName = columnDef.getName();
            int sqlType = columnDef.getSQLType();
            switch (sqlType) {
            case Types.ARRAY:
                BMap bMapvalue = getDataArray(columnName);
                bStruct.setRefField(++refRegIndex, bMapvalue);
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                String sValue = iterator.getString(columnName);
                bStruct.setStringField(++stringRegIndex, sValue);
                break;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                BValue bValue = iterator.get(columnName, sqlType);
                bStruct.setBlobField(++blobRegIndex, ((BBlob) bValue).blobValue());
                break;
            case Types.CLOB:
            case Types.NCLOB:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.ROWID:
                BValue strValue = iterator.get(columnName, sqlType);
                bStruct.setStringField(++stringRegIndex, strValue.stringValue());
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                long lValue = iterator.getInt(columnName);
                bStruct.setIntField(++longRegIndex, lValue);
                break;
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                double fValue = iterator.getFloat(columnName);
                bStruct.setFloatField(++doubleRegIndex, fValue);
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                boolean boolValue = iterator.getBoolean(columnName);
                bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                break;
            default:
                throw new BallerinaException("unsupported sql type " + sqlType + " found for the column " + columnName);
            }
        }
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
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.ROWID:
                type = BTypes.typeString;
                break;
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.BINARY:
            case Types.VARBINARY:
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
