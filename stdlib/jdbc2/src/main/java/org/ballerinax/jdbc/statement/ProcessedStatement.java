/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinax.jdbc.statement;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.exceptions.ApplicationException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.ballerinax.jdbc.Constants.PARAMETER_VALUE_FIELD;

/**
 * Represents a processed statement.
 *
 * @since 1.0.0
 */
class ProcessedStatement {
    private Connection conn;
    private PreparedStatement stmt;
    private ArrayValue params;
    private String databaseProductName;

    private Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_UTC));
    private static final String POSTGRES_DOUBLE = "float8";
    private static final int ORACLE_CURSOR_TYPE = -10;

    ProcessedStatement(Connection conn, PreparedStatement stmt, ArrayValue generatedParams,
            String databaseProductName) {
        this.conn = conn;
        this.stmt = stmt;
        this.params = generatedParams;
        this.databaseProductName = databaseProductName;
    }

    PreparedStatement prepare() throws ApplicationException, SQLException {
        if (params == null) {
            return null;
        }
        int paramCount = params.size();
        int currentOrdinal = 0;
        for (int index = 0; index < paramCount; index++) {
            MapValue<String, Object> paramRecord = (MapValue<String, Object>) params.getRefValue(index);
            if (paramRecord != null) {
                String sqlType = StatementProcessUtils.getSQLType(paramRecord);
                Object value = paramRecord.get(PARAMETER_VALUE_FIELD);
                BType type = TypeChecker.getType(value);
                int direction = StatementProcessUtils.getParameterDirection(paramRecord);
                //If the parameter is an array and sql type is not "array" then treat it as an array of parameters
                if (value != null && (type.getTag() == TypeTags.ARRAY_TAG
                        && ((BArrayType) type).getElementType().getTag() != TypeTags.BYTE_TAG)
                        && !Constants.SQLDataTypes.ARRAY.equalsIgnoreCase(sqlType)) {
                    int arrayLength = ((ArrayValue) value).size();
                    int typeTagOfArrayElement = ((BArrayType) type).getElementType().getTag();
                    for (int i = 0; i < arrayLength; i++) {
                        Object paramValue;
                        switch (typeTagOfArrayElement) {
                        case TypeTags.INT_TAG:
                        case TypeTags.BYTE_TAG:
                        case TypeTags.FLOAT_TAG:
                        case TypeTags.STRING_TAG:
                        case TypeTags.DECIMAL_TAG:
                        case TypeTags.BOOLEAN_TAG:
                            paramValue = ((ArrayValue) value).get(i);
                            break;
                        // The value parameter of the struct is an array of arrays. Only possibility that should be
                        // supported is, this being an array of byte arrays (blob)
                        // eg: [blob1, blob2, blob3] == [byteArray1, byteArray2, byteArray3]
                        case TypeTags.ARRAY_TAG:
                            Object array = ((ArrayValue) value).get(i);
                            BType arrayElementType = TypeChecker.getType(array);
                            // array cannot be null because the type tag is not union
                            if (((BArrayType) arrayElementType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                                paramValue = array;
                                break;
                            } else {
                                throw new ApplicationException("unsupported array type specified as a parameter " +
                                        "at index " + index + " and array element type being an array is supported " +
                                        "only when the inner array element type is BYTE");
                            }
                        default:
                            throw new ApplicationException("unsupported array type specified as a parameter at index "
                                    + index);
                        }
                        if (Constants.SQLDataTypes.REFCURSOR.equals(sqlType) || Constants.SQLDataTypes.BLOB
                                .equals(sqlType)) {
                            setParameter(conn, stmt, sqlType, paramValue, direction, currentOrdinal,
                                    databaseProductName);
                        } else {
                            setParameter(conn, stmt, sqlType, paramValue, direction, currentOrdinal);
                        }
                        currentOrdinal++;
                    }
                } else {
                    if (Constants.SQLDataTypes.REFCURSOR.equals(sqlType) || Constants.SQLDataTypes.ARRAY
                            .equals(sqlType) || Constants.SQLDataTypes.BLOB.equals(sqlType)) {
                        setParameter(conn, stmt, sqlType, value, direction, currentOrdinal, databaseProductName);
                    } else {
                        setParameter(conn, stmt, sqlType, value, direction, currentOrdinal);
                    }
                    currentOrdinal++;
                }
            } else {
                setNullObject(stmt, index);
                currentOrdinal++;
            }
        }
        return stmt;
    }

    private void setParameter(Connection conn, PreparedStatement stmt, String sqlType, Object value, int direction,
            int index) throws SQLException, ApplicationException {
        setParameter(conn, stmt, sqlType, value, direction, index, null);
    }

    private void setParameter(Connection conn, PreparedStatement stmt, String sqlType, Object value, int direction,
            int index, String databaseProductName) throws ApplicationException, SQLException {
        if (sqlType == null || sqlType.isEmpty()) {
            setStringValue(stmt, value, index, direction, Types.VARCHAR);
        } else {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
            case Constants.SQLDataTypes.SMALLINT:
            case Constants.SQLDataTypes.TINYINT:
                setSmallIntValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.VARCHAR:
                setStringValue(stmt, value, index, direction, Types.VARCHAR);
                break;
            case Constants.SQLDataTypes.CHAR:
                setStringValue(stmt, value, index, direction, Types.CHAR);
                break;
            case Constants.SQLDataTypes.LONGVARCHAR:
                setStringValue(stmt, value, index, direction, Types.LONGVARCHAR);
                break;
            case Constants.SQLDataTypes.NCHAR:
                setNStringValue(stmt, value, index, direction, Types.NCHAR);
                break;
            case Constants.SQLDataTypes.NVARCHAR:
                setNStringValue(stmt, value, index, direction, Types.NVARCHAR);
                break;
            case Constants.SQLDataTypes.LONGNVARCHAR:
                setNStringValue(stmt, value, index, direction, Types.LONGNVARCHAR);
                break;
            case Constants.SQLDataTypes.DOUBLE:
                setDoubleValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.NUMERIC:
                setNumericValue(stmt, value, index, direction, Types.NUMERIC);
                break;
            case Constants.SQLDataTypes.DECIMAL:
                setNumericValue(stmt, value, index, direction, Types.DECIMAL);
                break;
            case Constants.SQLDataTypes.BIT:
            case Constants.SQLDataTypes.BOOLEAN:
                setBooleanValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.BIGINT:
                setBigIntValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.INTEGER:
                setIntValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.REAL:
                setRealValue(stmt, value, index, direction, Types.REAL);
                break;
            case Constants.SQLDataTypes.FLOAT:
                setRealValue(stmt, value, index, direction, Types.FLOAT);
                break;
            case Constants.SQLDataTypes.DATE:
                setDateValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.TIMESTAMP:
            case Constants.SQLDataTypes.DATETIME:
                setTimeStampValue(stmt, value, index, direction, utcCalendar);
                break;
            case Constants.SQLDataTypes.TIME:
                setTimeValue(stmt, value, index, direction, utcCalendar);
                break;
            case Constants.SQLDataTypes.BINARY:
                setBinaryValue(stmt, value, index, direction, Types.BINARY);

                break;
            case Constants.SQLDataTypes.BLOB:
                setBlobValue(stmt, value, index, direction, Types.BLOB);
                break;
            case Constants.SQLDataTypes.LONGVARBINARY:
                setBlobValue(stmt, value, index, direction, Types.LONGVARBINARY);
                break;
            case Constants.SQLDataTypes.VARBINARY:
                setBinaryValue(stmt, value, index, direction, Types.VARBINARY);
                break;
            case Constants.SQLDataTypes.CLOB:
                setClobValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.NCLOB:
                setNClobValue(stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.ARRAY:
                setArrayValue(conn, stmt, value, index, direction, databaseProductName);
                break;
            case Constants.SQLDataTypes.STRUCT:
                setUserDefinedValue(conn, stmt, value, index, direction);
                break;
            case Constants.SQLDataTypes.REFCURSOR:
                setRefCursorValue(stmt, index, direction, databaseProductName);
                break;
            default:
                throw new ApplicationException("unsupported data type " + sqlType + " specified as a parameter" +
                        " at index " + index);
            }
        }
    }

    private void setIntValue(PreparedStatement stmt, Object value, int index, int direction)
            throws SQLException, ApplicationException {
        Integer val = obtainIntegerValue(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.INTEGER);
                } else {
                    stmt.setInt(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.INTEGER);
                } else {
                    stmt.setInt(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.INTEGER);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.INTEGER);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting integer value to statement. " + e.getMessage(),
                    e.getSQLState(), e.getErrorCode());
        }
    }

    private void setSmallIntValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        Integer val = obtainIntegerValue(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.SMALLINT);
                } else {
                    stmt.setShort(index + 1, val.shortValue());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.SMALLINT);
                } else {
                    stmt.setShort(index + 1, val.shortValue());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.SMALLINT);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.SMALLINT);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting integer value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setStringValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, (String) value);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, (String) value);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting string value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setNStringValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNString(index + 1, (String) value);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNString(index + 1, (String) value);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting string value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setDoubleValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        Double val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.FLOAT_TAG:
                val = (Double) value;
                break;
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
                val = ((Long) value).doubleValue();
                break;
            case TypeTags.DECIMAL_TAG:
                val = ((DecimalValue) value).value().doubleValue();
                break;
            case TypeTags.STRING_TAG:
                val = Double.parseDouble((String) value);
                break;
            default:
                throw new ApplicationException("invalid input value \"" + value.toString()
                        + "\" specified for double");

            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.DOUBLE);
                } else {
                    stmt.setDouble(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.DOUBLE);
                } else {
                    stmt.setDouble(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.DOUBLE);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.DOUBLE);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting double value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setNumericValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        BigDecimal val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.DECIMAL_TAG:
                val = ((DecimalValue) value).value();
                break;
            case TypeTags.INT_TAG:
                val = BigDecimal.valueOf((Long) value);
                break;
            case TypeTags.FLOAT_TAG:
                val = (BigDecimal.valueOf((Double) value));
                break;
            case TypeTags.STRING_TAG:
                val = new BigDecimal((String) value);
                break;
            default:
                throw new ApplicationException("invalid input value \"" + value.toString()
                        + "\" specified for numeric type");
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
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting numeric value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setBooleanValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        Boolean val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.BOOLEAN_TAG:
                val = (Boolean) value;
                break;
            case TypeTags.STRING_TAG:
                val = Boolean.valueOf((String) value);
                break;
            case TypeTags.INT_TAG:
                Long lVal = (Long) value;
                if (lVal == 0 || lVal == 1) {
                    val = lVal == 1;
                } else {
                    throw new ApplicationException("invalid integer value \"" + lVal + "\" specified for boolean");
                }
                break;
            default:
                throw new ApplicationException("invalid input value \"" + value.toString()
                        + "\" specified for boolean");
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.BIT);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.BIT);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.BIT);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.BIT);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting boolean value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setBigIntValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        Long val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                val = (Long) value;
                break;
            case TypeTags.STRING_TAG:
                val = Long.parseLong((String) value);
                break;
            default:
                throw new ApplicationException("invalid input value \"" + value.toString() + "\" specified for bigint");
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.BIGINT);
                } else {
                    stmt.setLong(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.BIGINT);
                } else {
                    stmt.setLong(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.BIGINT);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.BIGINT);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting bigint value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setRealValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        Float val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.FLOAT_TAG:
                val = ((Double) value).floatValue();
                break;
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                val =  ((Long) value).floatValue();
                break;
            case TypeTags.STRING_TAG:
                val = Float.parseFloat((String) value);
                break;
            default:
                throw new ApplicationException("invalid input value \"" + value.toString()
                        + "\" specified for float ");
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
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting float value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setDateValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        Date val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                long timeVal = ((MapValue<String, Object>) value).getIntValue(Constants.STRUCT_TIME_FIELD);
                val = new Date(timeVal);
            } else if (type.getTag() == TypeTags.INT_TAG) {
                val = new Date((Long) value);
            } else if (type.getTag() == TypeTags.STRING_TAG) {
                val = convertToDate((String) value);
            } else {
                throw new ApplicationException("invalid input type for date parameter at index " + index);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.DATE);
                } else {
                    stmt.setDate(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.DATE);
                } else {
                    stmt.setDate(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.DATE);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.DATE);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting date value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private Date convertToDate(String source) throws ApplicationException {
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
                throw new ApplicationException("invalid date format " + source + " specified");
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
            throw new ApplicationException("invalid date string " + source + " to parse");
        }
        return new Date(calendar.getTime().getTime());
    }

    private void setTimeStampValue(PreparedStatement stmt, Object value, int index, int direction, Calendar utcCalendar)
            throws ApplicationException, SQLException {
        Timestamp val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                Object timeVal = ((MapValue<String, Object>) value).get(Constants.STRUCT_TIME_FIELD);
                long time = (Long) timeVal;
                val = new Timestamp(time);
            } else if (value instanceof Long) {
                val = new Timestamp((Long) value);
            } else if (value instanceof String) {
                val = convertToTimeStamp((String) value);
            } else {
                throw new ApplicationException("invalid input type specified for timestamp parameter at index "
                        + index);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.TIMESTAMP);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.TIMESTAMP);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting timestamp value to statement. " + e.getMessage(),
                    e.getSQLState(), e.getErrorCode());
        }
    }

    private Timestamp convertToTimeStamp(String source) throws ApplicationException {
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
                throw new ApplicationException("invalid datetime format " + source + " specified");
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
            throw new ApplicationException("datetime string of " + source + " can not be less than 19 characters");
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    private int[] getTimeZoneWithMilliSeconds(String fractionStr) throws ApplicationException {
        int miliSecond = 0;
        int timeZoneOffSet = -1;
        if (fractionStr.startsWith(".")) {
            int milliSecondPartLength;
            if (fractionStr.endsWith("Z")) { //timezone is given as Z
                timeZoneOffSet = 0;
                String fractionPart = fractionStr.substring(1, fractionStr.lastIndexOf("Z"));
                miliSecond = Integer.parseInt(fractionPart);
                milliSecondPartLength = fractionPart.trim().length();
            } else {
                int lastIndexOfPlus = fractionStr.lastIndexOf("+");
                int lastIndexofMinus = fractionStr.lastIndexOf("-");
                if ((lastIndexOfPlus > 0) || (lastIndexofMinus > 0)) { //timezone +/-hh:mm
                    String timeOffSetStr;
                    if (lastIndexOfPlus > 0) {
                        timeOffSetStr = fractionStr.substring(lastIndexOfPlus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexOfPlus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = 1;
                    } else {
                        timeOffSetStr = fractionStr.substring(lastIndexofMinus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexofMinus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = -1;
                    }
                    if (timeOffSetStr.charAt(2) != ':') {
                        throw new ApplicationException("invalid time zone format " + fractionStr + " specified");
                    }
                    int hours = Integer.parseInt(timeOffSetStr.substring(0, 2));
                    int minits = Integer.parseInt(timeOffSetStr.substring(3, 5));
                    timeZoneOffSet = ((hours * 60) + minits) * 60000 * timeZoneOffSet;

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
        return new int[] { miliSecond, timeZoneOffSet };
    }

    private static int getTimeZoneOffset(String timezoneStr) throws ApplicationException {
        int timeZoneOffSet;
        if (timezoneStr.startsWith("Z")) { //GMT timezone
            timeZoneOffSet = 0;
        } else if (timezoneStr.startsWith("+") || timezoneStr.startsWith("-")) { //timezone with offset
            if (timezoneStr.charAt(3) != ':') {
                throw new ApplicationException("invalid time zone format " + timezoneStr + " specified");
            }
            int hours = Integer.parseInt(timezoneStr.substring(1, 3));
            int minits = Integer.parseInt(timezoneStr.substring(4, 6));
            timeZoneOffSet = ((hours * 60) + minits) * 60000;
            if (timezoneStr.startsWith("-")) {
                timeZoneOffSet = timeZoneOffSet * -1;
            }
        } else {
            throw new ApplicationException("invalid prefix of " + timezoneStr + " specified for timezone");
        }
        return timeZoneOffSet;
    }

    private void setTimeValue(PreparedStatement stmt, Object value, int index, int direction, Calendar utcCalendar)
            throws ApplicationException, SQLException {
        Time val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                Object timeVal = (((MapValue<String, Object>) value).get(Constants.STRUCT_TIME_FIELD));
                long time = (Long) timeVal;
                val = new Time(time);
            } else if (value instanceof Long) {
                val = new Time((Long) value);
            } else if (value instanceof String) {
                val = convertToTime((String) value);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.TIME);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.TIME);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.TIME);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.TIME);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting timestamp value to statement. " + e.getMessage(),
                    e.getSQLState(), e.getErrorCode());
        }
    }

    private Time convertToTime(String source) throws ApplicationException {
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
                throw new ApplicationException("invalid time format " + source + " specified");
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
            throw new ApplicationException("time string of " + source + " can not be less than 8 characters");
        }
        return new Time(calendar.getTimeInMillis());
    }

    private void setBinaryValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        byte[] val = getByteArray(value);
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
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting binary value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setBlobValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, SQLException {
        byte[] val = getByteArray(value);
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
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting binary value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private static Integer obtainIntegerValue(Object value) throws ApplicationException {
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                return  ((Long) value).intValue();
            case TypeTags.STRING_TAG:
                return Integer.parseInt((String) value);
            default:
                throw new ApplicationException("invalid value \"" + value.toString() + "\" specified for integer ");
            }
        }
        return null;
    }

    private byte[] getByteArray(Object value) throws ApplicationException {
        byte[] val = null;
        if (value instanceof ArrayValue) {
            val = ((ArrayValue) value).getBytes();
            val = Arrays.copyOfRange(val, 0, ((ArrayValue) value).size());
        } else if (value instanceof String) {
            val = getBytesFromBase64String((String) value);
        }
        return val;
    }

    private byte[] getBytesFromBase64String(String base64Str) throws ApplicationException {
        try {
            return Base64.getDecoder().decode(base64Str.getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            throw new ApplicationException("error while processing base64 string", e.getMessage());
        }
    }

    private void setClobValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader((String) value));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.CLOB);
                } else {
                    stmt.setClob(index + 1, val, ((String) value).length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.CLOB);
                } else {
                    stmt.setClob(index + 1, val, ((String) value).length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.CLOB);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.CLOB);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting binary value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setNClobValue(PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader((String) value));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.NCLOB);
                } else {
                    stmt.setNClob(index + 1, val,  ((String) value).length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, Types.NCLOB);
                } else {
                    stmt.setNClob(index + 1, val,  ((String) value).length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.NCLOB);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.NCLOB);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting binary value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void setRefCursorValue(PreparedStatement stmt, int index, int direction, String databaseProductName)
            throws ApplicationException, SQLException {
        try {
            if (Constants.QueryParamDirection.OUT == direction) {
                if (Constants.DatabaseNames.ORACLE.equals(databaseProductName)) {
                    // Since oracle does not support general java.sql.Types.REF_CURSOR in manipulating ref cursors it
                    // is required to use oracle.jdbc.OracleTypes.CURSOR here. In order to avoid oracle driver being
                    // a runtime dependency always, we have directly used the value(-10) of general oracle.jdbc
                    // .OracleTypes.CURSOR here.
                    ((CallableStatement) stmt).registerOutParameter(index + 1, ORACLE_CURSOR_TYPE);
                } else {
                    ((CallableStatement) stmt).registerOutParameter(index + 1, Types.REF_CURSOR);
                }
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting ref cursor value to statement. " + e.getMessage(),
                    e.getSQLState(), e.getErrorCode());
        }
    }

    private void setArrayValue(Connection conn, PreparedStatement stmt, Object value, int index, int direction,
            String databaseProductName) throws ApplicationException, SQLException {
        Object[] arrayData = getArrayData(value);
        Object[] arrayValue = (Object[]) arrayData[0];
        String structuredSQLType = (String) arrayData[1];
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                setArrayValue(arrayValue, conn, stmt, index, databaseProductName, structuredSQLType);
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                setArrayValue(arrayValue, conn, stmt, index, databaseProductName, structuredSQLType);
                registerArrayOutParameter(stmt, index, structuredSQLType, databaseProductName);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                registerArrayOutParameter(stmt, index, structuredSQLType, databaseProductName);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting array value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private void registerArrayOutParameter(PreparedStatement stmt, int index, String structuredSQLType,
            String databaseProductName) throws SQLException {
        if (databaseProductName.equals(Constants.DatabaseNames.POSTGRESQL)) {
            ((CallableStatement) stmt).registerOutParameter(index + 1, Types.ARRAY);
        } else {
            ((CallableStatement) stmt).registerOutParameter(index + 1, Types.ARRAY, structuredSQLType);
        }
    }

    private void setArrayValue(Object[] arrayValue, Connection conn, PreparedStatement stmt, int index,
            String databaseProductName, String structuredSQLType) throws SQLException {
        if (arrayValue == null) {
            stmt.setNull(index + 1, Types.ARRAY);
        } else {
            // In POSTGRESQL, need to pass "float8" to indicate DOUBLE value.
            if (Constants.SQLDataTypes.DOUBLE.equals(structuredSQLType) && Constants.DatabaseNames.POSTGRESQL
                    .equals(databaseProductName)) {
                structuredSQLType = POSTGRES_DOUBLE;
            }
            Array array = conn.createArrayOf(structuredSQLType, arrayValue);
            stmt.setArray(index + 1, array);
        }
    }

    private void setNullObject(PreparedStatement stmt, int index) throws SQLException {
        try {
            stmt.setObject(index + 1, null);
        } catch (SQLException e) {
            throw new SQLException("error while setting null value to the parameter at index " + index + ". " +
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
        }
    }

    private static Object[] getArrayData(Object value) throws ApplicationException {
        BType type = TypeChecker.getType(value);
        if (value == null || type.getTag() != TypeTags.ARRAY_TAG) {
            return new Object[] { null, null };
        }
        BType elementType = ((BArrayType) type).getElementType();
        int typeTag = elementType.getTag();
        Object[] arrayData;
        int arrayLength;
        switch (typeTag) {
        case TypeTags.INT_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new Long[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getInt(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.BIGINT };
        case TypeTags.FLOAT_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new Double[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getFloat(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.DOUBLE };
        case TypeTags.DECIMAL_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new BigDecimal[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((DecimalValue) ((ArrayValue) value).getRefValue(i)).value();
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.DECIMAL };
        case TypeTags.STRING_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new String[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getString(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.VARCHAR };
        case TypeTags.BOOLEAN_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new Boolean[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getBoolean(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.BOOLEAN };
        case TypeTags.ARRAY_TAG:
            BType elementTypeOfArrayElement = ((BArrayType) elementType)
                    .getElementType();
            if (elementTypeOfArrayElement.getTag() == TypeTags.BYTE_TAG) {
                arrayLength = ((ArrayValue) value).size();
                arrayData = new Blob[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((ArrayValue) value).getByte(i);
                }
                return new Object[] { arrayData, Constants.SQLDataTypes.BLOB };
            } else {
                throw new ApplicationException("unsupported data type specified as an array parameter");
            }
        default:
            throw new ApplicationException("unsupported data type specified as an array parameter");
        }
    }

    private void setUserDefinedValue(Connection conn, PreparedStatement stmt, Object value, int index, int direction)
            throws ApplicationException, SQLException {
        try {
            Object[] structData = getStructData(value, conn);
            Object[] dataArray = (Object[]) structData[0];
            String structuredSQLType = (String) structData[1];
            if (Constants.QueryParamDirection.IN == direction) {
                if (dataArray == null) {
                    stmt.setNull(index + 1, Types.STRUCT);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, dataArray);
                    stmt.setObject(index + 1, struct);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (dataArray == null) {
                    stmt.setNull(index + 1, Types.STRUCT);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, dataArray);
                    stmt.setObject(index + 1, struct);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.STRUCT, structuredSQLType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, Types.STRUCT, structuredSQLType);
            } else {
                throw new ApplicationException("invalid direction specified in the jdbc:Parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while setting struct value to statement. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        }
    }

    private Object[] getStructData(Object value, Connection conn) throws SQLException, ApplicationException {
        BType type = TypeChecker.getType(value);
        if (value == null || (type.getTag() != TypeTags.OBJECT_TYPE_TAG
                && type.getTag() != TypeTags.RECORD_TYPE_TAG)) {
            return new Object[] { null, null };
        }
        String structuredSQLType = type.getName().toUpperCase(Locale.getDefault());
        Map<String, BField> structFields = ((BStructureType) type)
                .getFields();
        int fieldCount = structFields.size();
        Object[] structData = new Object[fieldCount];
        Iterator<BField> fieldIterator = structFields.values().iterator();
        for (int i = 0; i < fieldCount; ++i) {
            BField field = fieldIterator.next();
            Object bValue = ((MapValue) value).get(field.getFieldName());
            int typeTag = field.getFieldType().getTag();
            switch (typeTag) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.DECIMAL_TAG:
                structData[i] = bValue;
                break;
            case TypeTags.ARRAY_TAG:
                BType elementType = ((BArrayType) field
                        .getFieldType()).getElementType();
                if (elementType.getTag() == TypeTags.BYTE_TAG) {
                    structData[i] = ((ArrayValue) bValue).getBytes();
                    break;
                } else {
                    throw new ApplicationException("unsupported data type of " + structuredSQLType
                            + " specified for struct parameter");
                }
            case TypeTags.RECORD_TYPE_TAG:
                Object structValue = bValue;
                Object[] internalStructData = getStructData(structValue, conn);
                Object[] dataArray = (Object[]) internalStructData[0];
                String internalStructType = (String) internalStructData[1];
                structValue = conn.createStruct(internalStructType, dataArray);
                structData[i] = structValue;
                break;
            default:
                throw new ApplicationException("unsupported data type of " + structuredSQLType
                        + " specified for struct parameter");
            }
        }
        return new Object[] { structData, structuredSQLType };
    }
}
