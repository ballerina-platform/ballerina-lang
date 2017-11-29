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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.data.sql.client.SQLDatasourceUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            List<ColumnDefinition> columnDefs, BStructType structType) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        this.utcCalendar = utcCalendar;
        this.columnDefs = columnDefs;
        this.bStructType = structType;
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
        if (rs == null) {
            return false;
        }
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

    @Override
    public String getBlob(String columnName) {
        try {
            Blob bValue = rs.getBlob(columnName);
            return SQLDatasourceUtils.getString(bValue);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getArray(String columnName) {
        try {
            return generateArrayDataResult(rs.getArray(columnName));
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private Map<String, Object> getArray(int columnIndex) {
        try {
            return generateArrayDataResult(rs.getArray(columnIndex));
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private Map<String, Object> generateArrayDataResult(Array array) throws SQLException {
        Map<String, Object> resultMap = new HashMap<>();
        if (!rs.wasNull()) {
            Object[] objArray = (Object[]) array.getArray();
            for (int i = 0; i < objArray.length; i++) {
                resultMap.put(String.valueOf(i), objArray[i]);
            }
        }
        return resultMap;
    }

    @Override
    public BStruct generateNext() {
        if (bStructType == null) {
            throw new BallerinaException("expected struct type is not specified");
        }
        BStruct bStruct = new BStruct(bStructType);
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        int index = 0;
        try {
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    String columnName = def.getName();
                    int sqlType = def.getSqlType();
                    ++index;
                    switch (sqlType) {
                    case Types.ARRAY:
                        BMap<String, BValue> bMapvalue = getDataArray(index);
                        bStruct.setRefField(++refRegIndex, bMapvalue);
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                        String sValue = rs.getString(index);
                        bStruct.setStringField(++stringRegIndex, sValue);
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        Blob value = rs.getBlob(index);
                        if (value != null) {
                            bStruct.setBlobField(++blobRegIndex, value.getBytes(1L, (int) value.length()));
                        } else {
                            bStruct.setBlobField(++blobRegIndex, new byte[0]);
                        }
                        break;
                    case Types.CLOB:
                        String clobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        bStruct.setStringField(++stringRegIndex, clobValue);
                        break;
                    case Types.NCLOB:
                        String nClobValue = SQLDatasourceUtils.getString(rs.getNClob(index));
                        bStruct.setStringField(++stringRegIndex, nClobValue);
                        break;
                    case Types.DATE:
                        String dateValue = SQLDatasourceUtils.getString(rs.getDate(index));
                        bStruct.setStringField(++stringRegIndex, dateValue);
                        break;
                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        String timeValue = SQLDatasourceUtils.getString(rs.getTime(index, utcCalendar));
                        bStruct.setStringField(++stringRegIndex, timeValue);
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        String timestmpValue = SQLDatasourceUtils.getString(rs.getTimestamp(index, utcCalendar));
                        bStruct.setStringField(++stringRegIndex, timestmpValue);
                        break;
                    case Types.ROWID:
                        BValue strValue = new BString(new String(rs.getRowId(index).getBytes(), "UTF-8"));
                        bStruct.setStringField(++stringRegIndex, strValue.stringValue());
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                        long iValue = rs.getInt(index);
                        bStruct.setIntField(++longRegIndex, iValue);
                        break;
                    case Types.INTEGER:
                    case Types.BIGINT:
                        long lValue = rs.getLong(index);
                        bStruct.setIntField(++longRegIndex, lValue);
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        double fValue = rs.getFloat(index);
                        bStruct.setFloatField(++doubleRegIndex, fValue);
                        break;
                    case Types.DOUBLE:
                        double dValue = rs.getDouble(index);
                        bStruct.setFloatField(++doubleRegIndex, dValue);
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        double decimalValue = 0;
                        BigDecimal bigDecimalValue = rs.getBigDecimal(index);
                        if (bigDecimalValue != null) {
                            decimalValue = bigDecimalValue.doubleValue();
                        }
                        bStruct.setFloatField(++doubleRegIndex, decimalValue);
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        boolean boolValue = rs.getBoolean(index);
                        bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                        break;
                    default:
                        throw new BallerinaException(
                                "unsupported sql type " + sqlType + " found for the column " + columnName + " index:"
                                        + index);
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

    private BMap<String, BValue> getDataArray(int columnIndex) {
        Map<String, Object> arrayMap = getArray(columnIndex);
        BMap<String, BValue> returnMap = new BMap<>();
        if (!arrayMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : arrayMap.entrySet()) {
                String key = entry.getKey();
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
