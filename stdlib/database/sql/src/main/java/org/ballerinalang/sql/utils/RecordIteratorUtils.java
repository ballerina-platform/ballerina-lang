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


import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.ballerinalang.jvm.StringUtils.fromString;
import static org.ballerinalang.sql.utils.Utils.cleanUpConnection;
import static org.ballerinalang.sql.utils.Utils.convert;
import static org.ballerinalang.sql.utils.Utils.getString;

/**
 * This class provides functionality for the `RecordIterator` to iterate through the sql result set.
 *
 * @since 1.2.0
 */
public class RecordIteratorUtils {
    private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_UTC.getValue()));

    public static Object nextResult(ObjectValue recordIterator) {
        ResultSet resultSet = (ResultSet) recordIterator.getNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD);
        try {
            if (resultSet.next()) {
                BStructureType streamConstraint = (BStructureType) recordIterator.
                        getNativeData(Constants.RECORD_TYPE_DATA_FIELD);
                MapValue<BString, Object> bStruct = new MapValueImpl<>(streamConstraint);
                List<ColumnDefinition> columnDefinitions = (List<ColumnDefinition>) recordIterator
                        .getNativeData(Constants.COLUMN_DEFINITIONS_DATA_FIELD);
                for (int i = 0; i < columnDefinitions.size(); i++) {
                    ColumnDefinition columnDefinition = columnDefinitions.get(i);
                    bStruct.put(fromString(columnDefinition.getBallerinaFieldName()),
                            getResult(resultSet, i + 1, columnDefinition));
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

    private static Object getResult(ResultSet resultSet, int columnIndex, ColumnDefinition columnDefinition)
            throws SQLException, ApplicationError, IOException {
        int sqlType = columnDefinition.getSqlType();
        BType ballerinaType = columnDefinition.getBallerinaType();
        switch (sqlType) {
            case Types.ARRAY:
                return convert(resultSet.getArray(columnIndex), sqlType, ballerinaType);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return convert(resultSet.getString(columnIndex), sqlType, ballerinaType);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                if (ballerinaType.getTag() == TypeTags.STRING_TAG) {
                    return convert(resultSet.getString(columnIndex), sqlType, ballerinaType,
                            columnDefinition.getSqlName());
                } else {
                    return convert(resultSet.getBytes(columnIndex), sqlType, ballerinaType,
                            columnDefinition.getSqlName());
                }
            case Types.BLOB:
                return convert(resultSet.getBlob(columnIndex), sqlType, ballerinaType);
            case Types.CLOB:
                String clobValue = getString(resultSet.getClob(columnIndex));
                return convert(clobValue, sqlType, ballerinaType);
            case Types.NCLOB:
                String nClobValue = getString(resultSet.getNClob(columnIndex));
                return convert(nClobValue, sqlType, ballerinaType);
            case Types.DATE:
                Date date = resultSet.getDate(columnIndex, calendar);
                return convert(date, sqlType, ballerinaType);
            case Types.TIME:
                Time time = resultSet.getTime(columnIndex, calendar);
                return convert(time, sqlType, ballerinaType);
            case Types.TIME_WITH_TIMEZONE:
                try {
                    time = resultSet.getTime(columnIndex, calendar);
                    return convert(time, sqlType, ballerinaType);
                } catch (SQLException ex) {
                    //Some database drivers do not support getTime operation,
                    // therefore falling back to getObject method.
                    OffsetTime offsetTime = resultSet.getObject(columnIndex, OffsetTime.class);
                    return convert(Time.valueOf(offsetTime.toLocalTime()), sqlType, ballerinaType);
                }
            case Types.TIMESTAMP:
                Timestamp timestamp = resultSet.getTimestamp(columnIndex, calendar);
                return convert(timestamp, sqlType, ballerinaType);
            case Types.TIMESTAMP_WITH_TIMEZONE:
                try {
                    timestamp = resultSet.getTimestamp(columnIndex, calendar);
                    return convert(timestamp, sqlType, ballerinaType);
                } catch (SQLException ex) {
                    //Some database drivers do not support getTimestamp operation,
                    // therefore falling back to getObject method.
                    OffsetDateTime offsetDateTime = resultSet.getObject(columnIndex, OffsetDateTime.class);
                    return convert(Timestamp.valueOf(offsetDateTime.toLocalDateTime()), sqlType, ballerinaType);
                }
            case Types.ROWID:
                return convert(resultSet.getRowId(columnIndex).getBytes(), sqlType, ballerinaType, "SQL RowID");
            case Types.TINYINT:
            case Types.SMALLINT:
                long iValue = resultSet.getInt(columnIndex);
                return convert(iValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.INTEGER:
            case Types.BIGINT:
                long lValue = resultSet.getLong(columnIndex);
                return convert(lValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.REAL:
            case Types.FLOAT:
                double fValue = resultSet.getFloat(columnIndex);
                return convert(fValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.DOUBLE:
                double dValue = resultSet.getDouble(columnIndex);
                return convert(dValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.NUMERIC:
            case Types.DECIMAL:
                BigDecimal decimalValue = resultSet.getBigDecimal(columnIndex);
                return convert(decimalValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.BIT:
            case Types.BOOLEAN:
                boolean boolValue = resultSet.getBoolean(columnIndex);
                return convert(boolValue, sqlType, ballerinaType, resultSet.wasNull());
            case Types.REF:
            case Types.STRUCT:
                Struct structData = (Struct) resultSet.getObject(columnIndex);
                return convert(structData, sqlType, ballerinaType);
            case Types.SQLXML:
                SQLXML sqlxml = resultSet.getSQLXML(columnIndex);
                return convert(sqlxml, sqlType, ballerinaType);
            default:
                if (ballerinaType.getTag() == TypeTags.INT_TAG) {
                    convert(resultSet.getInt(columnIndex), sqlType, ballerinaType, resultSet.wasNull());
                } else if (ballerinaType.getTag() == TypeTags.STRING_TAG
                        || ballerinaType.getTag() == TypeTags.ANY_TAG
                        || ballerinaType.getTag() == TypeTags.ANYDATA_TAG) {
                    return convert(resultSet.getString(columnIndex), sqlType, ballerinaType);
                } else if (ballerinaType.getTag() == TypeTags.BOOLEAN_TAG) {
                    return convert(resultSet.getBoolean(columnIndex),
                            sqlType, ballerinaType, resultSet.wasNull());
                } else if (ballerinaType.getTag() == TypeTags.ARRAY_TAG &&
                        ((BArrayType) ballerinaType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    return convert(resultSet.getBytes(columnIndex), sqlType, ballerinaType,
                            columnDefinition.getSqlName());
                } else if (ballerinaType.getTag() == TypeTags.FLOAT_TAG) {
                    return convert(resultSet.getDouble(columnIndex), sqlType, ballerinaType,
                            resultSet.wasNull());
                } else if (ballerinaType.getTag() == TypeTags.DECIMAL_TAG) {
                    return convert(resultSet.getBigDecimal(columnIndex), sqlType, ballerinaType,
                            resultSet.wasNull());
                } else if (ballerinaType.getTag() == TypeTags.XML_TAG) {
                    return convert(resultSet.getSQLXML(columnIndex), sqlType, ballerinaType);
                } else if (ballerinaType.getTag() == TypeTags.JSON_TAG) {
                    String jsonString = convert(resultSet.getString(columnIndex), sqlType, ballerinaType)
                            .getValue();
                    Reader reader = new StringReader(jsonString);
                    try {
                        return JSONParser.parse(reader);
                    } catch (BallerinaException e) {
                        throw new ApplicationError("Error while converting to JSON type. " + e.getDetail());
                    }
                }
                throw new ApplicationError("Unsupported SQL type " + columnDefinition.getSqlName());
        }
    }

    public static Object closeResult(ObjectValue recordIterator) {
        ResultSet resultSet = (ResultSet) recordIterator.getNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD);
        Statement statement = (Statement) recordIterator.getNativeData(Constants.STATEMENT_NATIVE_DATA_FIELD);
        Connection connection = (Connection) recordIterator.getNativeData(Constants.CONNECTION_NATIVE_DATA_FIELD);
        return cleanUpConnection(recordIterator, resultSet, statement, connection);
    }
}
