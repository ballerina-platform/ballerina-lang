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
package org.ballerinalang.nativeimpl.actions.data.sql;

import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.data.sql.client.SQLDatasourceUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This iterator mainly wrap java.sql.ResultSet. This will provide datatable operations
 * related to ballerina.data.actions.sql connector.
 *
 * @since 0.8.0
 */
public class SQLDataIterator implements DataIterator {

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;
    private Calendar utcCalendar;
    private List<ColumnDefinition> columnDefs;
    private BStructType bStructType;

    public SQLDataIterator(Connection conn, Statement stmt, ResultSet rs, Calendar utcCalendar,
            List<ColumnDefinition> columnDefs) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        this.utcCalendar = utcCalendar;
        this.columnDefs = columnDefs;
        generateStructType();
    }

    @Override
    public void close(boolean isInTransaction) {
        SQLDatasourceUtils.cleanupConnection(rs, stmt, conn, isInTransaction);
        rs = null;
        stmt = null;
        conn = null;
    }

    @Override
    public boolean next() {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getString(String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public long getInt(String columnName) {
        try {
            return rs.getLong(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public double getFloat(String columnName) {
        try {
            return rs.getDouble(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public boolean getBoolean(String columnName) {
        try {
            return rs.getBoolean(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private String getString(Object object) throws SQLException {
        String value;
        if (object instanceof Blob) {
            value = SQLDatasourceUtils.getString((Blob) object);
        } else if (object instanceof Timestamp) {
            value = SQLDatasourceUtils.getString((Timestamp) object);
        } else if (object instanceof Clob) {
            value = SQLDatasourceUtils.getString((Clob) object);
        } else if (object instanceof Date) {
            value = SQLDatasourceUtils.getString((Date) object);
        } else if (object instanceof Time) {
            value = SQLDatasourceUtils.getString((Time) object);
        } else if (object instanceof InputStream) {
            value = SQLDatasourceUtils.getString((InputStream) object);
        } else {
            value = String.valueOf(object);
        }
        return value;
    }

    @Override
    public String getObjectAsString(String columnName) {
        try {
            Object object = rs.getObject(columnName);
            if (object != null) {
                return getString(object);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getArray(String columnName) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Array array = rs.getArray(columnName);
            if (!rs.wasNull()) {
                Object[] objArray = (Object[]) array.getArray();
                for (int i = 0; i < objArray.length; i++) {
                    resultMap.put(String.valueOf(i), objArray[i]);
                }
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        return resultMap;
    }

    @Override
    public BStruct generateNext() {
        BStruct bStruct = new BStruct(bStructType);
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        try {
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    String columnName = def.getName();
                    int sqlType = def.getSqlType();
                    switch (sqlType) {
                    case Types.ARRAY:
                        BMap<BString, BValue> bMapvalue = getDataArray(columnName);
                        bStruct.setRefField(++refRegIndex, bMapvalue);
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                        String sValue = rs.getString(columnName);
                        bStruct.setStringField(++stringRegIndex, sValue);
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        Blob value = rs.getBlob(columnName);
                        BBlob bValue = new BBlob(value.getBytes(1L, (int) value.length()));
                        bStruct.setBlobField(++blobRegIndex, (bValue).blobValue());
                        break;
                    case Types.CLOB:
                        String clobValue = SQLDatasourceUtils.getString((rs.getClob(columnName)));
                        bStruct.setStringField(++stringRegIndex, clobValue);
                        break;
                    case Types.NCLOB:
                        String nClobValue = SQLDatasourceUtils.getString(rs.getNClob(columnName));
                        bStruct.setStringField(++stringRegIndex, nClobValue);
                        break;
                    case Types.DATE:
                        String dateValue = SQLDatasourceUtils.getString(rs.getDate(columnName));
                        bStruct.setStringField(++stringRegIndex, dateValue);
                        break;
                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        String timeValue = SQLDatasourceUtils.getString(rs.getTime(columnName, utcCalendar));
                        bStruct.setStringField(++stringRegIndex, timeValue);
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        String timestmpValue = SQLDatasourceUtils.getString(rs.getTimestamp(columnName, utcCalendar));
                        bStruct.setStringField(++stringRegIndex, timestmpValue);
                        break;
                    case Types.ROWID:
                        BValue strValue = new BString(new String(rs.getRowId(columnName).getBytes(), "UTF-8"));
                        bStruct.setStringField(++stringRegIndex, strValue.stringValue());
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                        long iValue = rs.getInt(columnName);
                        bStruct.setIntField(++longRegIndex, iValue);
                        break;
                    case Types.BIGINT:
                        long lValue = rs.getLong(columnName);
                        bStruct.setIntField(++longRegIndex, lValue);
                        break;
                    case Types.REAL:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                    case Types.FLOAT:
                        double fValue = rs.getFloat(columnName);
                        bStruct.setFloatField(++doubleRegIndex, fValue);
                        break;
                    case Types.DOUBLE:
                        double dValue = rs.getDouble(columnName);
                        bStruct.setFloatField(++doubleRegIndex, dValue);
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        boolean boolValue = rs.getBoolean(columnName);
                        bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                        break;
                    default:
                        throw new BallerinaException(
                                "unsupported sql type " + sqlType + " found for the column " + columnName);
                    }
                }
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in retrieving next value: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("error in retrieving next value for rowid type: " + e.getCause().getMessage());
        }
        return bStruct;
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return this.columnDefs;
    }

    private BMap<BString, BValue> getDataArray(String columnName) {
        Map<String, Object> arrayMap = getArray(columnName);
        BMap<BString, BValue> returnMap = new BMap<>();
        if (!arrayMap.isEmpty()) {
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

    private void generateStructType() {
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
        bStructType = new BStructType("RS", null);
        bStructType.setStructFields(structFields);
        bStructType.setFieldTypeCount(fieldCount);
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

    /**
     * This represents a column definition for a column in a datatable.
     */
    public static class SQLColumnDefinition extends ColumnDefinition {

        private int sqlType;

        public SQLColumnDefinition(String name, TypeKind mappedType, int sqlType) {
            super(name, mappedType);
            this.sqlType = sqlType;
        }

        public String getName() {
            return name;
        }

        public TypeKind getType() {
            return mappedType;
        }

        public int getSqlType() {
            return sqlType;
        }
    }
}
