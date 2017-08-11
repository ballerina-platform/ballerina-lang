/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.actions.data.sql.client;

import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.data.sql.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.TimeZone;

/**
 * Class contains utility methods for SQL Connector operations.
 *
 * @since 0.8.0
 */
public class SQLDatasourceUtils {

    public static void setIntValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Integer val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for integer: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setInt(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setInt(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set integer to statement: " + e.getMessage(), e);
        }
    }

    public static void setStringValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, value.stringValue());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, value.stringValue());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set string to statement: " + e.getMessage(), e);
        }
    }

    public static void setNStringValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNString(index + 1, value.stringValue());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, value.stringValue());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set string to statement: " + e.getMessage(), e);
        }
    }

    public static void setDoubleValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Double val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Double.parseDouble(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for double: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDouble(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDouble(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set double to statement: " + e.getMessage(), e);
        }
    }

    public static void setNumericValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        BigDecimal val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = new BigDecimal(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for numeric: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBigDecimal(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBigDecimal(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set numeric value to statement: " + e.getMessage(), e);
        }
    }

    public static void setBooleanValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Boolean val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                val = Boolean.valueOf(strValue);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set boolean value to statement: " + e.getMessage(), e);
        }
    }

    public static void setTinyIntValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Byte val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Byte.valueOf(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for byte: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setByte(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setByte(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set TinyInt value to statement: " + e.getMessage(), e);
        }
    }

    public static void setSmallIntValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Short val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Short.parseShort(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for short: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setShort(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setShort(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("Invalid direction for the parameter, index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("Error in set Small Int value to statement." + e.getMessage(), e);
        }
    }

    public static void setBigIntValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Long val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Long.parseLong(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for bigint: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setLong(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setLong(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set Big Int value to statement: " + e.getMessage(), e);
        }
    }

    public static void setRealValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Float val = null;
        if (value != null) {
            String strValue = value.stringValue();
            if (!strValue.isEmpty()) {
                try {
                    val = Float.parseFloat(strValue);
                } catch (NumberFormatException e) {
                    throw new BallerinaException("invalid value for float: " + strValue);
                }
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setFloat(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setFloat(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("Invalid direction for the parameter, index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("Error in set float value to statement." + e.getMessage(), e);
        }
    }

    public static void setDateValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        Date val = null;
        if (value != null) {
            if (value instanceof BInteger) {
                val = new Date(((BInteger) value).intValue());
            } else if (value instanceof BString) {
                val = SQLDatasourceUtils.convertToDate(value.stringValue());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDate(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDate(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set date value to statement: " + e.getMessage(), e);
        }
    }

    public static void setTimeStampValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType,
            Calendar utcCalendar) {
        Timestamp val = null;
        if (value != null) {
            if (value instanceof BInteger) {
                val = new Timestamp(((BInteger) value).intValue());
            } else if (value instanceof BString) {
                val = SQLDatasourceUtils.convertToTimeStamp(value.stringValue());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter, index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set Timestamp value to statement: " + e.getMessage(), e);
        }
    }

    public static void setTimeValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType,
            Calendar utcCalendar) {
        Time val = null;
        if (value != null) {
            if (value instanceof BInteger) {
                val = new Time(((BInteger) value).intValue());
            } else if (value instanceof BString) {
                val = SQLDatasourceUtils.convertToTime(value.stringValue());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set Timestamp value to statement: " + e.getMessage(), e);
        }
    }

    public static void setBinaryValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        byte[] val = null;
        if (value != null) {
            if (value instanceof BBlob) {
                val = ((BBlob) value).blobValue();
            } else if (value instanceof BString) {
                val = getBytesFromBase64String(value.stringValue());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBinaryStream(index + 1, new ByteArrayInputStream(val), val.length);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBinaryStream(index + 1, new ByteArrayInputStream(val), val.length);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set binary value to statement: " + e.getMessage(), e);
        }
    }

    public static void setBlobValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        byte[] val = null;
        if (value != null) {
            if (value instanceof BBlob) {
                val = ((BBlob) value).blobValue();
            } else if (value instanceof BString) {
                val = getBytesFromBase64String(value.stringValue());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBlob(index + 1, new ByteArrayInputStream(val), val.length);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBlob(index + 1, new ByteArrayInputStream(val), val.length);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set binary value to statement: " + e.getMessage(), e);
        }
    }

    public static void setClobValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader(value.stringValue()));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setClob(index + 1, val, value.stringValue().length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setClob(index + 1, val, value.stringValue().length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set binary value to statement: " + e.getMessage(), e);
        }
    }

    public static void setNClobValue(PreparedStatement stmt, BValue value, int index, int direction, int sqlType) {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader(value.stringValue()));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNClob(index + 1, val, value.stringValue().length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNClob(index + 1, val, value.stringValue().length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set binary value to statement: " + e.getMessage(), e);
        }
    }

    public static void setArrayValue(Connection conn, PreparedStatement stmt, BValue value, int index, int direction,
            int sqlType, String structuredSQLType) {
        Object[] val = null;
        if (value != null) {
            val = value.stringValue().split(",");
        }

        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Array array = conn.createArrayOf(structuredSQLType, val);
                    stmt.setArray(index + 1, array);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Array array = conn.createArrayOf(structuredSQLType, val);
                    stmt.setArray(index + 1, array);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set array value to statement: " + e.getMessage(), e);
        }
    }

    public static void setUserDefinedValue(Connection conn, PreparedStatement stmt, BValue value, int index,
            int direction, int sqlType, String structuredSQLType) {
        structuredSQLType = structuredSQLType.toUpperCase(Locale.getDefault());
        Object[] val = null;
        if (value != null) {
            val = value.stringValue().split(",");
        }

        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, val);
                    stmt.setObject(index + 1, struct);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, val);
                    stmt.setObject(index + 1, struct);
                }
                ((CallableStatement) stmt)
                        .registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else {
                throw new BallerinaException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in set struct value to statement: " + e.getMessage(), e);
        }
    }

    /**
     * This will close Database connection, statement and the resultset.
     *
     * @param rs   SQL resultset
     * @param stmt SQL statement
     * @param conn SQL connection
     */
    public static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn, boolean isInTransaction) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
            if (conn != null && !conn.isClosed() && !isInTransaction) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new BallerinaException("error cleaning sql resources: " + e.getMessage(), e);
        }
    }

    /**
     * This method will return equal ballerina data type for SQL type.
     *
     * @param sqlType SQL type in column
     * @return TypeEnum that represent respective ballerina type.
     */
    public static TypeEnum getColumnType(int sqlType) {
        switch (sqlType) {
        case Types.ARRAY:
            return TypeEnum.ARRAY;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.CLOB:
        case Types.NCLOB:
            return TypeEnum.STRING;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return TypeEnum.INT;
        case Types.BIGINT:
            return TypeEnum.INT;
        case Types.REAL:
            return TypeEnum.FLOAT;
        case Types.BIT:
        case Types.BOOLEAN:
            return TypeEnum.BOOLEAN;
        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.FLOAT:
        case Types.DOUBLE:
            return TypeEnum.FLOAT;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
        case Types.TIME_WITH_TIMEZONE:
        case Types.TIMESTAMP_WITH_TIMEZONE:
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return TypeEnum.EMPTY;
        default:
            return TypeEnum.EMPTY;
        }
    }

    /**
     * This will retrieve the string value for the given clob.
     *
     * @param data   clob data
     */
    public static String getString(Clob data) {
        if (data == null) {
            return null;
        }
        try (Reader r = new BufferedReader(data.getCharacterStream())) {
            StringBuilder sb = new StringBuilder();
            int pos;
            while ((pos = r.read()) != -1) {
                sb.append((char) pos);
            }
            return sb.toString();
        } catch (IOException | SQLException e) {
            throw new BallerinaException("error occurred while reading CLOB value: " + e.getMessage(), e);
        }
    }

    /**
     * This will retrieve the string value for the given blob.
     *
     * @param data blob data
     */
    public static String getString(Blob data) {
        // Directly allocating full length arrays for decode byte arrays since anyway we are building
        // new String in memory.
        // Position of the getBytes has to be 1 instead of 0.
        // "pos - the ordinal position of the first byte in the BLOB value to be extracted;
        // the first byte is at position 1"
        // - https://docs.oracle.com/javase/7/docs/api/java/sql/Blob.html#getBytes(long,%20int)
        if (data == null) {
            return null;
        }
        try {
            byte[] encode = getBase64Encode(
                    new String(data.getBytes(1L, (int) data.length()), Charset.defaultCharset()));
            return new String(encode, Charset.defaultCharset());
        } catch (SQLException e) {
            throw new BallerinaException("error occurred while reading BLOB value", e);
        }
    }

    /**
     * This will retrieve the string value for the given binary data.
     *
     * @param data binary data
     */
    public static String getString(byte[] data) {
        if (data == null) {
            return null;
        } else {
            return new String(data, Charset.defaultCharset());
        }
    }

    /**
     * This will retrieve the string value for the given input stream.
     *
     * @param inputStream input stream data
     */
    public static String getString(InputStream inputStream) {
        String value = getStringFromInputStream(inputStream);
        byte[] encode = getBase64Encode(value);
        return new String(encode, Charset.defaultCharset());
    }

    /**
     * This will retrieve the string value for the given date value.
     */
    public static String getString(Date value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(value);
        return getString(calendar, "date");
    }

    /**
     * This will retrieve the string value for the given timestamp value.
     */
    public static String getString(Timestamp value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(value.getTime());
        return getString(calendar, "datetime");
    }

    /**
     * This will retrieve the string value for the given time data.
     */
    public static String getString(Time value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(value.getTime());
        return getString(calendar, "time");
    }

    /**
     * This will retrieve the string value for the given array data.
     */
    public static String getString(Array dataArray) throws SQLException {
        if (dataArray == null) {
            return null;
        }
        StringJoiner sj = new StringJoiner(",", "[", "]");
        ResultSet rs = dataArray.getResultSet();
        while (rs.next()) {
            Object arrayEl = rs.getObject(2);
            String val = String.valueOf(arrayEl);
            sj.add(val);
        }
        return sj.toString();
    }

    /**
     * This will retrieve the string value for the given struct data.
     */
    public static String getString(Struct udt) throws SQLException {
        if (udt.getAttributes() != null) {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            Object[] udtValues = udt.getAttributes();
            for (int i = 0; i < udtValues.length; i++) {
                sj.add(String.valueOf(udtValues[i]));
            }
            return sj.toString();
        }
        return null;
    }

    private static String getString(Calendar calendar, String type) {
        if (!calendar.isSet(Calendar.ZONE_OFFSET)) {
            calendar.setTimeZone(TimeZone.getDefault());
        }
        StringBuffer datetimeString = new StringBuffer(28);
        switch (type) {
        case "date": //'-'? yyyy '-' mm '-' dd zzzzzz?
            appendDate(datetimeString, calendar);
            appendTimeZone(calendar, datetimeString);
            break;
        case "time": //hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
            break;
        case "datetime": //'-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            appendDate(datetimeString, calendar);
            datetimeString.append("T");
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
            break;
        default:
            throw new BallerinaException("invalid type for datetime data: " + type);
        }
        return datetimeString.toString();
    }

    private static byte[] getBytesFromBase64String(String base64Str) {
        try {
            return Base64.getDecoder().decode(base64Str.getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            throw new BallerinaException("error in processing base64 string: " + e.getMessage(), e);
        }
    }

    private static byte[] getBase64Encode(String st) {
        return Base64.getEncoder().encode(st.getBytes(Charset.defaultCharset()));
    }

    private static String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new BallerinaException("failed to read binary as a string: " + e.getMessage(), e);
        }
        return sb.toString();
    }

    private static void appendTimeZone(Calendar calendar, StringBuffer dateString) {
        int timezoneOffSet = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        int timezoneOffSetInMinits = timezoneOffSet / 60000;
        if (timezoneOffSetInMinits < 0) {
            dateString.append("-");
            timezoneOffSetInMinits = timezoneOffSetInMinits * -1;
        } else {
            dateString.append("+");
        }
        int hours = timezoneOffSetInMinits / 60;
        int minits = timezoneOffSetInMinits % 60;
        if (hours < 10) {
            dateString.append("0");
        }
        dateString.append(hours).append(":");
        if (minits < 10) {
            dateString.append("0");
        }
        dateString.append(minits);
    }

    private static void appendTime(Calendar value, StringBuffer dateString) {
        if (value.get(Calendar.HOUR_OF_DAY) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.HOUR_OF_DAY)).append(":");
        if (value.get(Calendar.MINUTE) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.MINUTE)).append(":");
        if (value.get(Calendar.SECOND) < 10) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.SECOND)).append(".");
        if (value.get(Calendar.MILLISECOND) < 10) {
            dateString.append("0");
        }
        if (value.get(Calendar.MILLISECOND) < 100) {
            dateString.append("0");
        }
        dateString.append(value.get(Calendar.MILLISECOND));
    }

    private static void appendDate(StringBuffer dateString, Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        if (year < 1000) {
            dateString.append("0");
        }
        if (year < 100) {
            dateString.append("0");
        }
        if (year < 10) {
            dateString.append("0");
        }
        dateString.append(year).append("-");
        // sql date month is started from 1 and calendar month is
        // started from 0. so have to add one
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 10) {
            dateString.append("0");
        }
        dateString.append(month).append("-");
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
            dateString.append("0");
        }
        dateString.append(calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static Date convertToDate(String source) {
        // the lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.startsWith("-")) {
            source = source.substring(1);
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }
        if (source.length() >= 10) {
            if ((source.charAt(4) != '-') || (source.charAt(7) != '-')) {
                throw new BallerinaException("invalid date format: " + source);
            }
            int year = Integer.parseInt(source.substring(0, 4));
            int month = Integer.parseInt(source.substring(5, 7));
            int day = Integer.parseInt(source.substring(8, 10));
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 10) {
                String restpart = source.substring(10);
                timeZoneOffSet = getTimeZoneOffset(restpart);
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new BallerinaException("invalid date string to parse: " + source);
        }
        return new Date(calendar.getTime().getTime());
    }

    private static Time convertToTime(String source) {
        //lexical representation of the time is hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.length() >= 8) {
            if ((source.charAt(2) != ':') || (source.charAt(5) != ':')) {
                throw new BallerinaException("invalid time format: " + source);
            }
            int hour = Integer.parseInt(source.substring(0, 2));
            int minite = Integer.parseInt(source.substring(3, 5));
            int second = Integer.parseInt(source.substring(6, 8));
            int miliSecond = 0;
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 8) {
                String rest = source.substring(8);
                int[] offsetData = getTimeZoneWithMilliSeconds(rest);
                miliSecond = offsetData[0];
                timeZoneOffSet = offsetData[1];
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minite);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, miliSecond);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new BallerinaException("time string can not be less than 8 characters: " + source);
        }
        return new Time(calendar.getTimeInMillis());
    }

    private static Timestamp convertToTimeStamp(String source) {
        //lexical representation of the date time is '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.startsWith("-")) {
            source = source.substring(1);
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }
        if (source.length() >= 19) {
            if ((source.charAt(4) != '-') || (source.charAt(7) != '-') || (source.charAt(10) != 'T') || (
                    source.charAt(13) != ':') || (source.charAt(16) != ':')) {
                throw new BallerinaException("invalid datetime format: " + source);
            }
            int year = Integer.parseInt(source.substring(0, 4));
            int month = Integer.parseInt(source.substring(5, 7));
            int day = Integer.parseInt(source.substring(8, 10));
            int hour = Integer.parseInt(source.substring(11, 13));
            int minite = Integer.parseInt(source.substring(14, 16));
            int second = Integer.parseInt(source.substring(17, 19));
            long miliSecond = 0;
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 19) {
                String rest = source.substring(19);
                int[] offsetData = getTimeZoneWithMilliSeconds(rest);
                miliSecond = offsetData[0];
                int calculatedtimeZoneOffSet = offsetData[1];
                if (calculatedtimeZoneOffSet != -1) {
                    timeZoneOffSet = calculatedtimeZoneOffSet;
                }
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minite);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, (int) miliSecond);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new BallerinaException("datetime string can not be less than 19 characters: " + source);
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    private static int[] getTimeZoneWithMilliSeconds(String fractionStr) {
        int miliSecond = 0;
        int timeZoneOffSet = -1;
        if (fractionStr.startsWith(".")) {
            int milliSecondPartLength = 0;
            if (fractionStr.endsWith("Z")) { //timezone is given as Z
                timeZoneOffSet = 0;
                String fractionPart = fractionStr.substring(1, fractionStr.lastIndexOf("Z"));
                miliSecond = Integer.parseInt(fractionPart);
                milliSecondPartLength = fractionPart.trim().length();
            } else {
                int lastIndexOfPlus = fractionStr.lastIndexOf("+");
                int lastIndexofMinus = fractionStr.lastIndexOf("-");
                if ((lastIndexOfPlus > 0) || (lastIndexofMinus > 0)) { //timezone +/-hh:mm
                    String timeOffSetStr = null;
                    if (lastIndexOfPlus > 0) {
                        timeOffSetStr = fractionStr.substring(lastIndexOfPlus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexOfPlus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = 1;
                    } else if (lastIndexofMinus > 0) {
                        timeOffSetStr = fractionStr.substring(lastIndexofMinus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexofMinus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = -1;
                    }
                    if (timeOffSetStr != null) {
                        if (timeOffSetStr.charAt(2) != ':') {
                            throw new BallerinaException("invalid time zone format: " + fractionStr);
                        }
                        int hours = Integer.parseInt(timeOffSetStr.substring(0, 2));
                        int minits = Integer.parseInt(timeOffSetStr.substring(3, 5));
                        timeZoneOffSet = ((hours * 60) + minits) * 60000 * timeZoneOffSet;
                    }
                } else { //no timezone
                    miliSecond = Integer.parseInt(fractionStr.substring(1));
                    milliSecondPartLength = fractionStr.substring(1).trim().length();
                }
            }
            if (milliSecondPartLength != 3) {
                // milisecond part represenst the fraction of the second so we have to
                // find the fraction and multiply it by 1000. So if milisecond part
                // has three digits nothing required
                miliSecond = miliSecond * 1000;
                for (int i = 0; i < milliSecondPartLength; i++) {
                    miliSecond = miliSecond / 10;
                }
            }
        } else {
            timeZoneOffSet = getTimeZoneOffset(fractionStr);
        }
        return new int[] {miliSecond, timeZoneOffSet};
    }

    private static int getTimeZoneOffset(String timezoneStr) {
        int timeZoneOffSet;
        if (timezoneStr.startsWith("Z")) { //GMT timezone
            timeZoneOffSet = 0;
        } else if (timezoneStr.startsWith("+") || timezoneStr.startsWith("-")) { //timezone with offset
            if (timezoneStr.charAt(3) != ':') {
                throw new BallerinaException("invalid time zone format:" + timezoneStr);
            }
            int hours = Integer.parseInt(timezoneStr.substring(1, 3));
            int minits = Integer.parseInt(timezoneStr.substring(4, 6));
            timeZoneOffSet = ((hours * 60) + minits) * 60000;
            if (timezoneStr.startsWith("-")) {
                timeZoneOffSet = timeZoneOffSet * -1;
            }
        } else {
            throw new BallerinaException("invalid prefix for timezone: " + timezoneStr);
        }
        return timeZoneOffSet;
    }
}
