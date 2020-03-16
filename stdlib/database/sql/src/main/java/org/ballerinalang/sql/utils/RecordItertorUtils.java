/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.utils;


import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * This class provides functionality for the `RecordIterator` to iterate through the sql result set.
 *
 * @since 1.2.0
 */
public class RecordItertorUtils {
    private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_UTC));

    public static Object nextResult(ObjectValue recordIterator) {
        ResultSet resultSet = (ResultSet) recordIterator.getNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD);
        try {
            if (resultSet.next()) {
                BStructureType streamConstraint = (BStructureType) recordIterator.
                        getNativeData(Constants.RECORD_TYPE_DATA_FIELD);
                MapValue<String, Object> bStruct = new MapValueImpl<>(streamConstraint);
                List<ColumnDefinition> columnDefinitions = (List<ColumnDefinition>) recordIterator
                        .getNativeData(Constants.COLUMN_DEFINITIONS_DATA_FIELD);
                for (int i = 0; i < columnDefinitions.size(); i++) {
                    ColumnDefinition columnDefinition = columnDefinitions.get(i);
                    bStruct.put(columnDefinition.getBallerinaFieldName(), getResult(resultSet, i + 1,
                            columnDefinition.getSqlType(), columnDefinition.getBallerinaType()));

                }
                return bStruct;
            } else {
                return null;
            }
        } catch (SQLException e) {
            return ErrorGenerator.getSQLDatabaseError(e, "Error when iterating the SQL result");
        } catch (IOException | ApplicationError e) {
            return ErrorGenerator.getSQLApplicationError("Error when iterating the SQL result. "
                    + e.getMessage());
        } catch (Throwable throwable) {
            return ErrorGenerator.getSQLApplicationError("Error when iterating through the " +
                    "SQL result. " + throwable.getMessage());
        }
    }

    private static Object getResult(ResultSet resultSet, int columnIndex, int sqlType, BType ballerinaType)
            throws SQLException, ApplicationError, IOException {
        switch (sqlType) {
            case Types.ARRAY:
                return Utils.convert(resultSet.getArray(columnIndex), sqlType, ballerinaType);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return Utils.convert(resultSet.getString(columnIndex), sqlType, ballerinaType);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return Utils.convert(resultSet.getBytes(columnIndex), sqlType, ballerinaType);
            case Types.BLOB:
                return Utils.convert(resultSet.getBlob(columnIndex), sqlType, ballerinaType);
            case Types.CLOB:
                String clobValue = Utils.getString(resultSet.getClob(columnIndex));
                return Utils.convert(clobValue, sqlType, ballerinaType);
            case Types.NCLOB:
                String nClobValue = Utils.getString(resultSet.getNClob(columnIndex));
                return Utils.convert(nClobValue, sqlType, ballerinaType);
            case Types.DATE:
                Date date = resultSet.getDate(columnIndex, calendar);
                return Utils.convert(date, sqlType, ballerinaType);
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                Time time = resultSet.getTime(columnIndex, calendar);
                return Utils.convert(time, sqlType, ballerinaType);
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                Timestamp timestamp = resultSet.getTimestamp(columnIndex, calendar);
                return Utils.convert(timestamp, sqlType, ballerinaType);
            case Types.ROWID:
                String rowId = new String(resultSet.getRowId(columnIndex).getBytes(), StandardCharsets.UTF_8);
                return Utils.convert(rowId, sqlType, ballerinaType);
            case Types.TINYINT:
            case Types.SMALLINT:
                long iValue = resultSet.getInt(columnIndex);
                return Utils.convert(iValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.INTEGER:
            case Types.BIGINT:
                long lValue = resultSet.getLong(columnIndex);
                return Utils.convert(lValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.REAL:
            case Types.FLOAT:
                double fValue = resultSet.getFloat(columnIndex);
                return Utils.convert(fValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.DOUBLE:
                double dValue = resultSet.getDouble(columnIndex);
                return Utils.convert(dValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.NUMERIC:
            case Types.DECIMAL:
                BigDecimal decimalValue = resultSet.getBigDecimal(columnIndex);
                return Utils.convert(decimalValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.BIT:
            case Types.BOOLEAN:
                boolean boolValue = resultSet.getBoolean(columnIndex);
                return Utils.convert(boolValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.STRUCT:
                Struct structData = (Struct) resultSet.getObject(columnIndex);
                return Utils.convert(structData, sqlType, ballerinaType);
            default:
                throw new ApplicationError("unsupported sql type " + sqlType
                        + " found for the column index: " + columnIndex);
        }
    }


    public static Object closeResult(ObjectValue recordIterator) {
        ResultSet resultSet = (ResultSet) recordIterator.getNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD);
        Statement statement = (Statement) recordIterator.getNativeData(Constants.STATEMENT_NATIVE_DATA_FIELD);
        Connection connection = (Connection) recordIterator.getNativeData(Constants.CONNECTION_NATIVE_DATA_FIELD);
        if (resultSet != null) {
            try {
                resultSet.close();
                recordIterator.addNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD, null);
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e, "Error while closing the result set. ");
            }
        }
        if (statement != null) {
            try {
                statement.close();
                recordIterator.addNativeData(Constants.STATEMENT_NATIVE_DATA_FIELD, null);
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e, "Error while closing the result set. ");
            }
        }
        if (connection != null) {
            try {
                connection.close();
                recordIterator.addNativeData(Constants.CONNECTION_NATIVE_DATA_FIELD, null);
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e, "Error while closing the connection. ");
            }
        }
        return null;
    }
}
