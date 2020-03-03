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

import org.ballerinalang.jvm.types.*;
import org.ballerinalang.jvm.values.*;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
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
        }
    }

    private static Object getResult(ResultSet resultSet, int columnIndex, int sqlType, BType ballerinaType)
            throws SQLException, ApplicationError, IOException {
        switch (sqlType) {
            case Types.ARRAY:
                return RecordConverterUtils.convert(resultSet.getArray(columnIndex), sqlType, ballerinaType);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return RecordConverterUtils.convert(resultSet.getString(columnIndex), sqlType, ballerinaType);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return RecordConverterUtils.convert(resultSet.getBytes(columnIndex), sqlType, ballerinaType);
            case Types.BLOB:
                return RecordConverterUtils.convert(resultSet.getBlob(columnIndex), sqlType, ballerinaType);
            case Types.CLOB:
                String clobValue = RecordConverterUtils.getString(resultSet.getClob(columnIndex));
                RecordConverterUtils.convert(clobValue, sqlType, ballerinaType);
            case Types.NCLOB:
                String nClobValue = RecordConverterUtils.getString(resultSet.getNClob(columnIndex));
                return RecordConverterUtils.convert(nClobValue, sqlType, ballerinaType);
            case Types.DATE:
                Date date = resultSet.getDate(columnIndex, calendar);
                return RecordConverterUtils.convert(date, sqlType, ballerinaType);
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                Time time = resultSet.getTime(columnIndex, calendar);
                return RecordConverterUtils.convert(time, sqlType, ballerinaType);
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                Timestamp timestamp = resultSet.getTimestamp(columnIndex, calendar);
                return RecordConverterUtils.convert(timestamp, sqlType, ballerinaType);
            case Types.ROWID:
                String rowId = new String(resultSet.getRowId(columnIndex).getBytes(), StandardCharsets.UTF_8);
                return RecordConverterUtils.convert(rowId, sqlType, ballerinaType);
            case Types.TINYINT:
            case Types.SMALLINT:
                long iValue = resultSet.getInt(columnIndex);
                return RecordConverterUtils.convert(iValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.INTEGER:
            case Types.BIGINT:
                long lValue = resultSet.getLong(columnIndex);
                return RecordConverterUtils.convert(lValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.REAL:
            case Types.FLOAT:
                double fValue = resultSet.getFloat(columnIndex);
                return RecordConverterUtils.convert(fValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.DOUBLE:
                double dValue = resultSet.getDouble(columnIndex);
                return RecordConverterUtils.convert(dValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.NUMERIC:
            case Types.DECIMAL:
                BigDecimal decimalValue = resultSet.getBigDecimal(columnIndex);
                return RecordConverterUtils.convert(decimalValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.BIT:
            case Types.BOOLEAN:
                boolean boolValue = resultSet.getBoolean(columnIndex);
                return RecordConverterUtils.convert(boolValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.STRUCT:
                Struct structData = (Struct) resultSet.getObject(columnIndex);
                return RecordConverterUtils.convert(structData, sqlType, ballerinaType);
            default:
                throw new ApplicationError("unsupported sql type " + sqlType
                        + " found for the column index: " + columnIndex);
        }
    }


    public static Object closeResult(ObjectValue recordIterator) {
        return null;
    }
}
