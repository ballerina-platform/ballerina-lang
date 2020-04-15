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

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BValue;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.readers.CharacterChannelReader;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Array;
import java.sql.Blob;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * This class has the utility methods to process and convert the SQL types into ballerina types,
 * and other shared utility methods.
 *
 * @since 1.2.0
 */
class Utils {

    private static final BArrayType stringArrayType = new BArrayType(BTypes.typeString);
    private static final BArrayType booleanArrayType = new BArrayType(BTypes.typeBoolean);
    private static final BArrayType intArrayType = new BArrayType(BTypes.typeInt);
    private static final BArrayType floatArrayType = new BArrayType(BTypes.typeFloat);
    private static final BArrayType decimalArrayType = new BArrayType(BTypes.typeDecimal);

    public static void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    static String getSqlQuery(MapValue<String, Object> paramString) throws ApplicationError {
        ArrayValue partsArray = paramString.getArrayValue(Constants.ParameterizedStingFields.PARTS);
        ArrayValue insertionsArray = paramString.getArrayValue(Constants.ParameterizedStingFields.INSERTIONS);
        if (partsArray.size() - 1 == insertionsArray.size()) {
            StringBuilder sqlQuery = new StringBuilder();
            for (int i = 0; i < partsArray.size(); i++) {
                if (i > 0) {
                    sqlQuery.append(" ? ");
                }
                sqlQuery.append(partsArray.get(i).toString());
            }
            return sqlQuery.toString();
        } else {
            throw new ApplicationError("Parts and insertions count doesn't match in ParametrizedString passed. "
                    + paramString.toString());
        }
    }

    static void setParams(Connection connection, PreparedStatement preparedStatement, MapValue<String,
            Object> paramString) throws SQLException, ApplicationError, IOException {
        ArrayValue arrayValue = paramString.getArrayValue(Constants.ParameterizedStingFields.INSERTIONS);
        for (int i = 0; i < arrayValue.size(); i++) {
            Object object = arrayValue.get(i);
            int index = i + 1;
            if (object instanceof String) {
                preparedStatement.setString(index, object.toString());
            } else if (object instanceof Long) {
                preparedStatement.setLong(index, (Long) object);
            } else if (object instanceof Double) {
                preparedStatement.setDouble(index, (Double) object);
            } else if (object instanceof DecimalValue) {
                preparedStatement.setBigDecimal(index, ((DecimalValue) object).decimalValue());
            } else if (object instanceof ArrayValue) {
                ArrayValue objectArray = (ArrayValue) object;
                if (objectArray.getElementType().getTag() == org.wso2.ballerinalang.compiler.util.TypeTags.BYTE) {
                    preparedStatement.setBytes(index, objectArray.getBytes());
                } else {
                    throw new ApplicationError("Only byte[] is supported can be set directly into " +
                            "ParameterizedString, any other array types should be wrapped as sql:Value");
                }
            } else if (object instanceof MapValue) {
                MapValue<String, Object> recordValue = (MapValue<String, Object>) object;
                setSqlTypedParam(connection, preparedStatement, index, recordValue);
            }
        }
    }

    private static void setSqlTypedParam(Connection connection, PreparedStatement preparedStatement, int index,
                                         MapValue<String, Object> typedValue)
            throws SQLException, ApplicationError, IOException {
        String sqlType = typedValue.getStringValue(Constants.TypedValueFields.SQL_TYPE);
        Object value = typedValue.get(Constants.TypedValueFields.VALUE);
        switch (sqlType) {
            case Constants.SqlTypes.VARCHAR:
            case Constants.SqlTypes.LONGVARCHAR:
            case Constants.SqlTypes.CHAR:
                preparedStatement.setString(index, value.toString());
                break;
            case Constants.SqlTypes.LONGNVARCHAR:
            case Constants.SqlTypes.NCHAR:
            case Constants.SqlTypes.NVARCHAR:
                preparedStatement.setNString(index, value.toString());
                break;
            case Constants.SqlTypes.BIT:
            case Constants.SqlTypes.BOOLEAN:
                if (value instanceof String) {
                    preparedStatement.setBoolean(index, Boolean.parseBoolean(value.toString()));
                } else if (value instanceof Integer || value instanceof Long) {
                    long lVal = ((Number) value).longValue();
                    if (lVal == 1 || lVal == 0) {
                        preparedStatement.setBoolean(index, lVal == 1);
                    } else {
                        throw new ApplicationError("Only 1 or 0 can be passed for " + sqlType
                                + " SQL Type, but found :" + lVal);
                    }
                } else if (value instanceof Boolean) {
                    preparedStatement.setBoolean(index, (Boolean) value);
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.TINYINT:
                if (value instanceof Integer || value instanceof Long) {
                    preparedStatement.setByte(index, ((Number) value).byteValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.INTEGER:
                if (value instanceof Integer || value instanceof Long) {
                    preparedStatement.setInt(index, ((Number) value).intValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.BIGINT:
                if (value instanceof Integer || value instanceof Long) {
                    preparedStatement.setLong(index, ((Number) value).longValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.SMALLINT:
                if (value instanceof Integer || value instanceof Long) {
                    preparedStatement.setShort(index, ((Number) value).shortValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.FLOAT:
            case Constants.SqlTypes.REAL:
                if (value instanceof Double || value instanceof Long ||
                        value instanceof Float || value instanceof Integer) {
                    preparedStatement.setFloat(index, ((Number) value).floatValue());
                } else if (value instanceof DecimalValue) {
                    preparedStatement.setFloat(index, ((DecimalValue) value).decimalValue().floatValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.DOUBLE:
                if (value instanceof Double || value instanceof Long ||
                        value instanceof Float || value instanceof Integer) {
                    preparedStatement.setDouble(index, ((Number) value).doubleValue());
                } else if (value instanceof DecimalValue) {
                    preparedStatement.setDouble(index, ((DecimalValue) value).decimalValue().doubleValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.NUMERIC:
            case Constants.SqlTypes.DECIMAL:
                if (value instanceof Double || value instanceof Long) {
                    preparedStatement.setBigDecimal(index, new BigDecimal(((Number) value).doubleValue(),
                            MathContext.DECIMAL64));
                } else if (value instanceof Integer || value instanceof Float) {
                    preparedStatement.setBigDecimal(index, new BigDecimal(((Number) value).doubleValue(),
                            MathContext.DECIMAL32));
                } else if (value instanceof DecimalValue) {
                    preparedStatement.setBigDecimal(index, ((DecimalValue) value).decimalValue());
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.BINARY:
            case Constants.SqlTypes.LONGVARBINARY:
            case Constants.SqlTypes.VARBINARY:
            case Constants.SqlTypes.BLOB:
                if (value instanceof ArrayValue) {
                    ArrayValue arrayValue = (ArrayValue) value;
                    if (arrayValue.getElementType().getTag() == org.wso2.ballerinalang.compiler.util.TypeTags.BYTE) {
                        preparedStatement.setBytes(index, arrayValue.getBytes());
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                } else if (value instanceof ObjectValue) {
                    ObjectValue objectValue = (ObjectValue) value;
                    if (objectValue.getType().getName().equalsIgnoreCase(Constants.READ_BYTE_CHANNEL_STRUCT) &&
                            objectValue.getType().getPackage().toString()
                                    .equalsIgnoreCase(IOConstants.IO_PACKAGE_ID.toString())) {
                        Channel byteChannel = (Channel) objectValue.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
                        preparedStatement.setBinaryStream(index, byteChannel.getInputStream());
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                break;
            case Constants.SqlTypes.CLOB:
            case Constants.SqlTypes.NCLOB:
                Clob clob;
                if (sqlType.equalsIgnoreCase(Constants.SqlTypes.NCLOB)) {
                    clob = connection.createNClob();
                } else {
                    clob = connection.createClob();
                }
                if (value instanceof String) {
                    clob.setString(1, value.toString());
                    preparedStatement.setClob(index, clob);
                } else if (value instanceof ObjectValue) {
                    ObjectValue objectValue = (ObjectValue) value;
                    if (objectValue.getType().getName().equalsIgnoreCase(Constants.READ_CHAR_CHANNEL_STRUCT) &&
                            objectValue.getType().getPackage().toString()
                                    .equalsIgnoreCase(IOConstants.IO_PACKAGE_ID.toString())) {
                        CharacterChannel charChannel = (CharacterChannel) objectValue.getNativeData(
                                IOConstants.CHARACTER_CHANNEL_NAME);
                        preparedStatement.setCharacterStream(index, new CharacterChannelReader(charChannel));
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                }
                break;
            case Constants.SqlTypes.DATE:
                Date date;
                if (value instanceof String) {
                    date = Date.valueOf(value.toString());
                } else if (value instanceof Long) {
                    date = new Date((Long) value);
                } else if (value instanceof MapValue) {
                    MapValue<String, Object> dateTimeStruct = (MapValue<String, Object>) value;
                    if (dateTimeStruct.getType().getName()
                            .equalsIgnoreCase(org.ballerinalang.stdlib.time.util.Constants.STRUCT_TYPE_TIME)) {
                        ZonedDateTime zonedDateTime = TimeUtils.getZonedDateTime(dateTimeStruct);
                        date = new Date(zonedDateTime.toInstant().toEpochMilli());
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                preparedStatement.setDate(index, date);
                break;
            case Constants.SqlTypes.TIME:
                Time time = null;
                if (value instanceof String) {
                    time = Time.valueOf(value.toString());
                } else if (value instanceof Long) {
                    time = new Time((Long) value);
                } else if (value instanceof MapValue) {
                    MapValue<String, Object> dateTimeStruct = (MapValue<String, Object>) value;
                    if (dateTimeStruct.getType().getName()
                            .equalsIgnoreCase(org.ballerinalang.stdlib.time.util.Constants.STRUCT_TYPE_TIME)) {
                        ZonedDateTime zonedDateTime = TimeUtils.getZonedDateTime(dateTimeStruct);
                        time = new Time(zonedDateTime.toInstant().toEpochMilli());
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                preparedStatement.setTime(index, time);
                break;
            case Constants.SqlTypes.TIMESTAMP:
            case Constants.SqlTypes.DATETIME:
                Timestamp timestamp = null;
                if (value instanceof String) {
                    timestamp = Timestamp.valueOf(value.toString());
                } else if (value instanceof Long) {
                    timestamp = new Timestamp((Long) value);
                } else if (value instanceof MapValue) {
                    MapValue<String, Object> dateTimeStruct = (MapValue<String, Object>) value;
                    if (dateTimeStruct.getType().getName()
                            .equalsIgnoreCase(org.ballerinalang.stdlib.time.util.Constants.STRUCT_TYPE_TIME)) {
                        ZonedDateTime zonedDateTime = TimeUtils.getZonedDateTime(dateTimeStruct);
                        timestamp = new Timestamp(zonedDateTime.toInstant().toEpochMilli());
                    } else {
                        throw throwInvalidParameterError(value, sqlType);
                    }
                } else {
                    throw throwInvalidParameterError(value, sqlType);
                }
                preparedStatement.setTimestamp(index, timestamp);
                break;
            case Constants.SqlTypes.ARRAY:
                Object[] arrayData = getArrayData(value);
                if (arrayData[0] != null) {
                    Array array = connection.createArrayOf((String) arrayData[1], (Object[]) arrayData[0]);
                    preparedStatement.setArray(index, array);
                } else {
                    preparedStatement.setObject(index, null);
                }
                break;
            case Constants.SqlTypes.STRUCT:
                Object[] structData = getStructData(value, connection);
                Object[] dataArray = (Object[]) structData[0];
                String structuredSQLType = (String) structData[1];
                if (dataArray == null) {
                    preparedStatement.setNull(index + 1, Types.STRUCT);
                } else {
                    Struct struct = connection.createStruct(structuredSQLType, dataArray);
                    preparedStatement.setObject(index + 1, struct);
                }
                break;
            default:
                throw new ApplicationError("Unsupported SQL type: " + sqlType);
        }
    }

    private static Object[] getArrayData(Object value) throws ApplicationError {
        BType type = TypeChecker.getType(value);
        if (value == null || type.getTag() != TypeTags.ARRAY_TAG) {
            return new Object[]{null, null};
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
                return new Object[]{arrayData, Constants.SqlTypes.BIGINT};
            case TypeTags.FLOAT_TAG:
                arrayLength = ((ArrayValue) value).size();
                arrayData = new Double[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((ArrayValue) value).getFloat(i);
                }
                return new Object[]{arrayData, Constants.SqlTypes.DOUBLE};
            case TypeTags.DECIMAL_TAG:
                arrayLength = ((ArrayValue) value).size();
                arrayData = new BigDecimal[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((DecimalValue) ((ArrayValue) value).getRefValue(i)).value();
                }
                return new Object[]{arrayData, Constants.SqlTypes.DECIMAL};
            case TypeTags.STRING_TAG:
                arrayLength = ((ArrayValue) value).size();
                arrayData = new String[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((ArrayValue) value).getString(i);
                }
                return new Object[]{arrayData, Constants.SqlTypes.VARCHAR};
            case TypeTags.BOOLEAN_TAG:
                arrayLength = ((ArrayValue) value).size();
                arrayData = new Boolean[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((ArrayValue) value).getBoolean(i);
                }
                return new Object[]{arrayData, Constants.SqlTypes.BOOLEAN};
            case TypeTags.ARRAY_TAG:
                BType elementTypeOfArrayElement = ((BArrayType) elementType)
                        .getElementType();
                if (elementTypeOfArrayElement.getTag() == TypeTags.BYTE_TAG) {
                    ArrayValue arrayValue = (ArrayValue) value;
                    arrayData = new byte[arrayValue.size()][];
                    for (int i = 0; i < arrayData.length; i++) {
                        arrayData[i] = ((ArrayValue) arrayValue.get(i)).getBytes();
                    }
                    return new Object[]{arrayData, Constants.SqlTypes.BINARY};
                } else {
                    throw throwInvalidParameterError(value, Constants.SqlTypes.ARRAY);
                }
            default:
                throw throwInvalidParameterError(value, Constants.SqlTypes.ARRAY);
        }
    }

    private static Object[] getStructData(Object value, Connection conn) throws SQLException, ApplicationError {
        BType type = TypeChecker.getType(value);
        if (value == null || (type.getTag() != TypeTags.OBJECT_TYPE_TAG
                && type.getTag() != TypeTags.RECORD_TYPE_TAG)) {
            return new Object[]{null, null};
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
                        throw new ApplicationError("unsupported data type of " + structuredSQLType
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
                    throw new ApplicationError("unsupported data type of " + structuredSQLType
                            + " specified for struct parameter");
            }
        }
        return new Object[]{structData, structuredSQLType};
    }

    private static ApplicationError throwInvalidParameterError(Object value, String sqlType) {
        String valueName;
        if (value instanceof BValue) {
            valueName = ((BValue) value).getType().getName();
        } else {
            valueName = value.getClass().getName();
        }
        return new ApplicationError("Invalid parameter :" + valueName + " is passed as value for sql type : "
                + sqlType);
    }


    static ArrayValue convert(Array array, int sqlType, BType bType) throws SQLException, ApplicationError {
        if (array != null) {
            validatedInvalidFieldAssignment(sqlType, bType, "SQL Array");
            Object[] dataArray = (Object[]) array.getArray();
            if (dataArray == null || dataArray.length == 0) {
                return null;
            }

            Object[] result = validateNullable(dataArray);
            Object firstNonNullElement = result[0];
            boolean containsNull = (boolean) result[1];

            if (containsNull) {
                // If there are some null elements, return a union-type element array
                return createAndPopulateRefValueArray(firstNonNullElement, dataArray, bType);
            } else {
                // If there are no null elements, return a ballerina primitive-type array
                return createAndPopulatePrimitiveValueArray(firstNonNullElement, dataArray);
            }

        } else {
            return null;
        }
    }

    private static ArrayValue createAndPopulatePrimitiveValueArray(Object firstNonNullElement, Object[] dataArray) {
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            ArrayValue stringDataArray = (ArrayValue) BValueCreator.createArrayValue(stringArrayType);
            for (int i = 0; i < length; i++) {
                stringDataArray.add(i, (String) dataArray[i]);
            }
            return stringDataArray;
        } else if (firstNonNullElement instanceof Boolean) {
            ArrayValue boolDataArray = (ArrayValue) BValueCreator.createArrayValue(booleanArrayType);
            for (int i = 0; i < length; i++) {
                boolDataArray.add(i, ((Boolean) dataArray[i]).booleanValue());
            }
            return boolDataArray;
        } else if (firstNonNullElement instanceof Integer) {
            ArrayValue intDataArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
            for (int i = 0; i < length; i++) {
                intDataArray.add(i, ((Integer) dataArray[i]).intValue());
            }
            return intDataArray;
        } else if (firstNonNullElement instanceof Long) {
            ArrayValue longDataArray = (ArrayValue) BValueCreator.createArrayValue(intArrayType);
            for (int i = 0; i < length; i++) {
                longDataArray.add(i, ((Long) dataArray[i]).longValue());
            }
            return longDataArray;
        } else if (firstNonNullElement instanceof Float) {
            ArrayValue floatDataArray = (ArrayValue) BValueCreator.createArrayValue(floatArrayType);
            for (int i = 0; i < length; i++) {
                floatDataArray.add(i, ((Float) dataArray[i]).floatValue());
            }
            return floatDataArray;
        } else if (firstNonNullElement instanceof Double) {
            ArrayValue doubleDataArray = (ArrayValue) BValueCreator.createArrayValue(floatArrayType);
            for (int i = 0; i < dataArray.length; i++) {
                doubleDataArray.add(i, ((Double) dataArray[i]).doubleValue());
            }
            return doubleDataArray;
        } else if ((firstNonNullElement instanceof BigDecimal)) {
            ArrayValue decimalDataArray = (ArrayValue) BValueCreator.createArrayValue(decimalArrayType);
            for (int i = 0; i < dataArray.length; i++) {
                decimalDataArray.add(i, new DecimalValue((BigDecimal) dataArray[i]));
            }
            return decimalDataArray;
        } else {
            return null;
        }
    }

    static String getString(Clob data) throws IOException, SQLException {
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
        }
    }

    private static ArrayValue createAndPopulateRefValueArray(Object firstNonNullElement, Object[] dataArray,
                                                             BType bType) {
        ArrayValue refValueArray = null;
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            refValueArray = createEmptyRefValueArray(BTypes.typeString);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof Boolean) {
            refValueArray = createEmptyRefValueArray(BTypes.typeBoolean);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof Integer) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof Long) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof Float) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof Double) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i]);
            }
        } else if (firstNonNullElement instanceof BigDecimal) {
            refValueArray = createEmptyRefValueArray(BTypes.typeDecimal);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new DecimalValue((BigDecimal) dataArray[i]) : null);
            }
        } else if (firstNonNullElement == null) {
            refValueArray = createEmptyRefValueArray(bType);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, firstNonNullElement);
            }
        }
        return refValueArray;
    }

    private static ArrayValue createEmptyRefValueArray(BType type) {
        List<BType> memberTypes = new ArrayList<>(2);
        memberTypes.add(type);
        memberTypes.add(BTypes.typeNull);
        BUnionType unionType = new BUnionType(memberTypes);
        return (ArrayValue) BValueCreator.createArrayValue(new BArrayType(unionType));
    }

    private static Object[] validateNullable(Object[] objects) {
        Object[] returnResult = new Object[2];
        boolean foundNull = false;
        Object nonNullObject = null;
        for (Object object : objects) {
            if (object != null) {
                if (nonNullObject == null) {
                    nonNullObject = object;
                }
                if (foundNull) {
                    break;
                }
            } else {
                foundNull = true;
                if (nonNullObject != null) {
                    break;
                }
            }
        }
        returnResult[0] = nonNullObject;
        returnResult[1] = foundNull;
        return returnResult;
    }

    static Object convert(String value, int sqlType, BType bType) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL String");
        return value;
    }

    static Object convert(byte[] value, int sqlType, BType bType) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL Binary");
        if (value != null) {
            return BValueCreator.createArrayValue(value);
        } else {
            return null;
        }
    }

    static Object convert(long value, int sqlType, BType bType, boolean isNull) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL long or integer");
        if (isNull) {
            return null;
        } else {
            if (bType.getTag() == TypeTags.STRING_TAG) {
                return String.valueOf(value);
            }
            return value;
        }
    }

    static Object convert(double value, int sqlType, BType bType, boolean isNull) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL double or float");
        if (isNull) {
            return null;
        } else {
            if (bType.getTag() == TypeTags.STRING_TAG) {
                return String.valueOf(value);
            }
            return value;
        }
    }

    static Object convert(BigDecimal value, int sqlType, BType bType, boolean isNull) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL decimal or real");
        if (isNull) {
            return null;
        } else {
            if (bType.getTag() == TypeTags.STRING_TAG) {
                return String.valueOf(value);
            }
            return new DecimalValue(value);
        }
    }

    static Object convert(Blob value, int sqlType, BType bType) throws ApplicationError, SQLException {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL Blob");
        if (value != null) {
            return BValueCreator.createArrayValue(value.getBytes(1L, (int) value.length()));
        } else {
            return null;
        }
    }

    static Object convert(java.util.Date date, int sqlType, BType bType) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL Date/Time");
        if (date != null) {
            switch (bType.getTag()) {
                case TypeTags.STRING_TAG:
                    return getString(date);
                case TypeTags.OBJECT_TYPE_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                    return createTimeStruct(date.getTime());
                case TypeTags.INT_TAG:
                    return date.getTime();
            }
        }
        return null;
    }

    static Object convert(boolean value, int sqlType, BType bType, boolean isNull) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL Boolean");
        if (!isNull) {
            switch (bType.getTag()) {
                case TypeTags.BOOLEAN_TAG:
                    return value;
                case TypeTags.INT_TAG:
                    if (value) {
                        return 1L;
                    } else {
                        return 0L;
                    }
                case TypeTags.STRING_TAG:
                    return String.valueOf(value);
            }
        }
        return null;
    }

    static Object convert(Struct value, int sqlType, BType bType) throws ApplicationError {
        validatedInvalidFieldAssignment(sqlType, bType, "SQL Struct");
        if (value != null) {
            if (bType instanceof BRecordType) {
                return createUserDefinedType(value, (BRecordType) bType);
            } else {
                throw new ApplicationError("The ballerina type that can be used for SQL struct should be record type," +
                        " but found " + bType.getName() + " .");
            }
        } else {
            return null;
        }
    }

    private static MapValue<String, Object> createUserDefinedType(Struct structValue, BStructureType structType)
            throws ApplicationError {
        if (structValue == null) {
            return null;
        }
        BField[] internalStructFields = structType.getFields().values().toArray(new BField[0]);
        MapValue<String, Object> struct = new MapValueImpl<>(structType);
        try {
            Object[] dataArray = structValue.getAttributes();
            if (dataArray != null) {
                if (dataArray.length != internalStructFields.length) {
                    throw new ApplicationError("specified record and the returned SQL Struct field counts " +
                            "are different, and hence not compatible");
                }
                int index = 0;
                for (BField internalField : internalStructFields) {
                    int type = internalField.getFieldType().getTag();
                    String fieldName = internalField.getFieldName();
                    Object value = dataArray[index];
                    switch (type) {
                        case TypeTags.INT_TAG:
                            if (value instanceof BigDecimal) {
                                struct.put(fieldName, ((BigDecimal) value).intValue());
                            } else {
                                struct.put(fieldName, value);
                            }
                            break;
                        case TypeTags.FLOAT_TAG:
                            if (value instanceof BigDecimal) {
                                struct.put(fieldName, ((BigDecimal) value).doubleValue());
                            } else {
                                struct.put(fieldName, value);
                            }
                            break;
                        case TypeTags.DECIMAL_TAG:
                            if (value instanceof BigDecimal) {
                                struct.put(fieldName, value);
                            } else {
                                struct.put(fieldName, new DecimalValue((BigDecimal) value));
                            }
                            break;
                        case TypeTags.STRING_TAG:
                            struct.put(fieldName, value);
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            struct.put(fieldName, ((int) value) == 1);
                            break;
                        case TypeTags.OBJECT_TYPE_TAG:
                        case TypeTags.RECORD_TYPE_TAG:
                            struct.put(fieldName,
                                    createUserDefinedType((Struct) value,
                                            (BStructureType) internalField.getFieldType()));
                            break;
                        default:
                            throw new ApplicationError("Error while retrieving data for unsupported type " +
                                    internalField.getFieldType().getName() + " to create "
                                    + structType.getName() + " record.");
                    }
                    ++index;
                }
            }
        } catch (SQLException e) {
            throw new ApplicationError("Error while retrieving data to create " + structType.getName()
                    + " record. ", e);
        }
        return struct;
    }


    private static MapValue<String, Object> createTimeStruct(long millis) {
        return TimeUtils.createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(), millis,
                Constants.TIMEZONE_UTC);
    }

    private static String getString(java.util.Date value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(value);

        if (!calendar.isSet(Calendar.ZONE_OFFSET)) {
            calendar.setTimeZone(TimeZone.getDefault());
        }
        StringBuffer datetimeString = new StringBuffer(28);
        if (value instanceof Date) {
            //'-'? yyyy '-' mm '-' dd zzzzzz?
            calendar.setTime(value);
            appendDate(datetimeString, calendar);
            appendTimeZone(calendar, datetimeString);
        } else if (value instanceof Time) {
            //hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
            calendar.setTimeInMillis(value.getTime());
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
        } else if (value instanceof Timestamp) {
            calendar.setTimeInMillis(value.getTime());
            appendDate(datetimeString, calendar);
            datetimeString.append("T");
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
        } else {
            calendar.setTime(value);
            appendTime(calendar, datetimeString);
            appendTimeZone(calendar, datetimeString);
        }
        return datetimeString.toString();
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

    private static void validatedInvalidFieldAssignment(int sqlType, BType bType, String sqlTypeName)
            throws ApplicationError {
        if (!isValidFieldConstraint(sqlType, bType)) {
            throw new ApplicationError(sqlTypeName + " field cannot be converted to ballerina type : "
                    + bType.getName());
        }
    }

    public static BType validFieldConstraint(int sqlType, BType bType) {
        if (bType.getTag() == TypeTags.UNION_TAG && bType instanceof BUnionType) {
            BUnionType bUnionType = (BUnionType) bType;
            for (BType memberType : bUnionType.getMemberTypes()) {
                //In case if the member type is another union type, check recursively.
                if (isValidFieldConstraint(sqlType, memberType)) {
                    return memberType;
                }
            }
        } else {
            if (isValidPrimitiveConstraint(sqlType, bType)) {
                return bType;
            }
        }
        return null;
    }

    public static boolean isValidFieldConstraint(int sqlType, BType bType) {
        if (bType.getTag() == TypeTags.UNION_TAG && bType instanceof BUnionType) {
            BUnionType bUnionType = (BUnionType) bType;
            for (BType memberType : bUnionType.getMemberTypes()) {
                //In case if the member type is another union type, check recursively.
                if (isValidFieldConstraint(sqlType, memberType)) {
                    return true;
                }
            }
            return false;
        } else {
            return isValidPrimitiveConstraint(sqlType, bType);
        }
    }

    private static boolean isValidPrimitiveConstraint(int sqlType, BType bType) {
        switch (sqlType) {
            case Types.ARRAY:
                return bType.getTag() == TypeTags.ARRAY_TAG;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.ROWID:
                return bType.getTag() == TypeTags.STRING_TAG ||
                        bType.getTag() == TypeTags.JSON_TAG;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
                return bType.getTag() == TypeTags.STRING_TAG ||
                        bType.getTag() == TypeTags.OBJECT_TYPE_TAG ||
                        bType.getTag() == TypeTags.RECORD_TYPE_TAG ||
                        bType.getTag() == TypeTags.INT_TAG;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return bType.getTag() == TypeTags.INT_TAG ||
                        bType.getTag() == TypeTags.STRING_TAG;
            case Types.BIT:
            case Types.BOOLEAN:
                return bType.getTag() == TypeTags.BOOLEAN_TAG ||
                        bType.getTag() == TypeTags.INT_TAG ||
                        bType.getTag() == TypeTags.STRING_TAG;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return bType.getTag() == TypeTags.DECIMAL_TAG ||
                        bType.getTag() == TypeTags.INT_TAG ||
                        bType.getTag() == TypeTags.STRING_TAG;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return bType.getTag() == TypeTags.FLOAT_TAG ||
                        bType.getTag() == TypeTags.STRING_TAG;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                if (bType.getTag() == TypeTags.ARRAY_TAG) {
                    int elementTypeTag = ((BArrayType) bType).getElementType().getTag();
                    return elementTypeTag == TypeTags.BYTE_TAG;
                }
                return bType.getTag() == TypeTags.BYTE_ARRAY_TAG;
            case Types.STRUCT:
                return bType.getTag() == TypeTags.RECORD_TYPE_TAG;
            default:
                return bType.getTag() == TypeTags.ANY_TAG;
        }
    }
}
