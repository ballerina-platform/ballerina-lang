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
package org.ballerinax.jdbc.table;

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableIterator;
import org.ballerinalang.stdlib.time.util.TimeUtils;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasourceUtils;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;
import org.ballerinax.jdbc.exceptions.PanickingApplicationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * This iterator mainly wrap java.sql.ResultSet. This will provide table operations
 * related to JDBC connector.
 *
 * @since 0.8.0
 */
public class SQLDataIterator extends TableIterator {

    private Calendar utcCalendar;
    private static final String UNASSIGNABLE_UNIONTYPE_EXCEPTION = "corresponding Union type in the record is not "
            + "an assignable nillable type";
    private static final String MISMATCHING_FIELD_ASSIGNMENT = "trying to assign to a mismatching type";
    private String sourceDatabase;
    private static final String POSTGRES_OID_COLUMN_TYPE_NAME = "oid";

    public SQLDataIterator(TableResourceManager rm, ResultSet rs, Calendar utcCalendar,
            List<ColumnDefinition> columnDefs, BStructureType structType, String databaseProductName) {
        super(rm, rs, structType, columnDefs);
        this.utcCalendar = utcCalendar;
        this.sourceDatabase = databaseProductName;
    }

    @Override
    public void close() {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            resourceManager.gracefullyReleaseResources();
            rs = null;
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLDatabaseError(e);
        }
    }

    public void reset() {
        close();
    }

    @Override
    public String getBlob(int columnIndex) {
        try {
            Blob bValue = rs.getBlob(columnIndex);
            return rs.wasNull() ? null : SQLDatasourceUtils.getString(bValue);
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLDatabaseError(e);
        }
    }

    @Override
    public MapValue<String, Object> generateNext() {
        if (this.type == null) {
            throw ErrorGenerator
                    .getSQLApplicationError("the expected record type is not specified in the remote function");
        }
        MapValue<String, Object> bStruct = new MapValueImpl<>(this.type);
        int index = 0;
        String columnName = null;
        int sqlType = -1;
        try {
            BField[] structFields = this.type.getFields().values().toArray(new BField[0]);
            if (columnDefs.size() != structFields.length) {
                throw ErrorGenerator.getSQLApplicationError("number of fields in the constraint type is " + (
                        structFields.length > columnDefs.size() ?
                                "greater" : "lower") + " than column count of the result set");
            }
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    columnName = def.getName();
                    sqlType = def.getSqlType();
                    ++index;
                    BField field = structFields[index - 1];
                    BType fieldType = field.getFieldType();
                    String fieldName = field.getFieldName();
                    switch (sqlType) {
                        case Types.ARRAY:
                            Array data = rs.getArray(index);
                            handleArrayValue(bStruct, fieldName, data, fieldType);
                            break;
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                            String sValue = rs.getString(index);
                            handleStringValue(sValue, fieldName, bStruct, fieldType);
                            break;
                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                            byte[] binaryValue = rs.getBytes(index);
                            handleBinaryValue(bStruct, fieldName, binaryValue, fieldType);
                            break;
                        case Types.BLOB:
                            Blob blobValue = rs.getBlob(index);
                            handleBinaryValue(bStruct, fieldName, blobValue == null ?
                                    null : blobValue.getBytes(1L, (int) blobValue.length()), fieldType);
                            break;
                        case Types.CLOB:
                            String clobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                            handleStringValue(clobValue, fieldName, bStruct, fieldType);
                            break;
                        case Types.NCLOB:
                            String nClobValue = SQLDatasourceUtils.getString((rs.getNClob(index)));
                            handleStringValue(nClobValue, fieldName, bStruct, fieldType);
                            break;
                        case Types.DATE:
                            Date date = rs.getDate(index);
                            handleDateValue(bStruct, fieldName, date, fieldType);
                            break;
                        case Types.TIME:
                        case Types.TIME_WITH_TIMEZONE:
                            Time time = rs.getTime(index, utcCalendar);
                            handleDateValue(bStruct, fieldName, time, fieldType);
                            break;
                        case Types.TIMESTAMP:
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            Timestamp timestamp = rs.getTimestamp(index, utcCalendar);
                            handleDateValue(bStruct, fieldName, timestamp, fieldType);
                            break;
                        case Types.ROWID:
                            sValue = new String(rs.getRowId(index).getBytes(), StandardCharsets.UTF_8);
                            handleStringValue(sValue, fieldName, bStruct, fieldType);
                            break;
                        case Types.TINYINT:
                        case Types.SMALLINT:
                            long iValue = rs.getInt(index);
                            handleLongValue(iValue, bStruct, fieldName, fieldType);
                            break;
                        case Types.INTEGER:
                        case Types.BIGINT:
                            if (sourceDatabase.equalsIgnoreCase(Constants.DatabaseNames.POSTGRESQL)) {
                                boolean isOID = rs.getMetaData().getColumnTypeName(index)
                                        .equalsIgnoreCase(POSTGRES_OID_COLUMN_TYPE_NAME);
                                if (isOID) {
                                    handleOIDValue(index, bStruct, fieldName, fieldType);
                                } else {
                                    long lValue = rs.getLong(index);
                                    handleLongValue(lValue, bStruct, fieldName, fieldType);
                                }
                            } else {
                                long lValue = rs.getLong(index);
                                handleLongValue(lValue, bStruct, fieldName, fieldType);
                            }
                            break;
                        case Types.REAL:
                        case Types.FLOAT:
                            double fValue = rs.getFloat(index);
                            handleDoubleValue(fValue, bStruct, fieldName, fieldType);
                            break;
                        case Types.DOUBLE:
                            double dValue = rs.getDouble(index);
                            handleDoubleValue(dValue, bStruct, fieldName, fieldType);
                            break;
                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            BigDecimal bigDecimalValue = rs.getBigDecimal(index);
                            handleDecimalValue(bigDecimalValue, bStruct, fieldName, fieldType);
                            break;
                        case Types.BIT:
                        case Types.BOOLEAN:
                            boolean boolValue = rs.getBoolean(index);
                            handleBooleanValue(bStruct, fieldName, boolValue, fieldType);
                            break;
                        case Types.STRUCT:
                            Struct structData = (Struct) rs.getObject(index);
                            handleStructValue(bStruct, fieldName, structData, fieldType);
                            break;
                        default:
                            throw ErrorGenerator.getSQLApplicationError("unsupported sql type " + sqlType
                                    + " found for the column " + columnName + " at index " + index);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw ErrorGenerator.getSQLApplicationError("error while retrieving next value for column "
                    + columnName + " of SQL Type " + sqlType + " at index " + index + ", " + e.getMessage());
        } catch (PanickingApplicationException e) {
            throw ErrorGenerator.getSQLApplicationError(e);
        }
        return bStruct;
    }

    private void validateAndSetRefRecordField(MapValue<String, Object> bStruct, String fieldName, int expectedTypeTag,
            int actualTypeTag, Object value, String exceptionMessage) throws PanickingApplicationException {
        setMatchingRefRecordField(bStruct, fieldName, value, expectedTypeTag == actualTypeTag, exceptionMessage);
    }

    private void validateAndSetRefRecordField(MapValue<String, Object> bStruct, String fieldName,
            int[] expectedTypeTags, int actualTypeTag, Object value, String exceptionMessage)
            throws PanickingApplicationException {
        boolean typeMatches = Arrays.stream(expectedTypeTags).anyMatch(tag -> actualTypeTag == tag);
        setMatchingRefRecordField(bStruct, fieldName, value, typeMatches, exceptionMessage);
    }

    private void setMatchingRefRecordField(MapValue<String, Object> bStruct, String fieldName, Object value,
            boolean typeMatches, String exceptionMessage) throws PanickingApplicationException {
        if (typeMatches) {
            bStruct.put(fieldName, value);
        } else {
            throw new PanickingApplicationException(exceptionMessage);
        }
    }
    private void validateAndSetDecimalValue(MapValue<String, Object> bStruct, String fieldName, int actualTypeTag,
            DecimalValue value, String exceptionMessage)
            throws PanickingApplicationException {
        boolean typeMatches = isValidType(actualTypeTag, value);
        setMatchingRefRecordField(bStruct, fieldName, value, typeMatches, exceptionMessage);
    }

    private boolean isValidType(int actualTypeTag, DecimalValue value) {
        if (actualTypeTag == TypeTags.DECIMAL_TAG) {
            return true;
        } else if (actualTypeTag == TypeTags.INT_TAG) {
            return value == null || value.value().scale() == 0;
        }
        return false;
    }

    private void handleNilToNonNillableFieldAssignment() throws PanickingApplicationException {
        throw new PanickingApplicationException("trying to assign a Nil value to a non-nillable field");
    }

    private void handleMismatchingFieldAssignment() throws PanickingApplicationException {
        throw new PanickingApplicationException("trying to assign to a mismatching type");
    }

    private void handleUnAssignableUnionTypeAssignment() throws PanickingApplicationException {
        throw new PanickingApplicationException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
    }

    private int retrieveNonNilTypeTag(BType fieldType) throws PanickingApplicationException {
        List<BType> members = ((BUnionType) fieldType).getMemberTypes();
        return retrieveNonNilType(members).getTag();
    }

    private MapValue<String, Object> createTimeStruct(long millis) {
        return TimeUtils.createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(), millis,
                Constants.TIMEZONE_UTC);
    }

    private MapValue<String, Object> createUserDefinedType(Struct structValue, BStructureType structType)
            throws PanickingApplicationException {
        if (structValue == null) {
            return null;
        }
        BField[] internalStructFields = structType.getFields().values().toArray(new BField[0]);
        MapValue<String, Object> struct = new MapValueImpl<>(structType);
        try {
            Object[] dataArray = structValue.getAttributes();
            if (dataArray != null) {
                if (dataArray.length != internalStructFields.length) {
                    throw new PanickingApplicationException("specified record and the returned record types are " +
                            "not compatible");
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
                                createUserDefinedType((Struct) value, (BStructureType) internalField.getFieldType()));
                        break;
                    default:
                        throw new PanickingApplicationException(
                                "error while retrieving UDT data for unsupported type " + type);
                    }
                    ++index;
                }
            }
        } catch (SQLException e) {
            throw new PanickingApplicationException("error while retrieving UDT data: " + e.getMessage());
        }
        return struct;
    }

    private void handleArrayValue(MapValue<String, Object> bStruct, String fieldName, Array data, BType fieldType)
            throws SQLException, PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        ArrayValue dataArray = getDataArray(data);
        if (dataArray != null) {
            BType nonNilType = fieldType;
            if (fieldTypeTag == TypeTags.UNION_TAG) {
                nonNilType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            }
            // The dataArray is created from the array returned from the database. There, an array of Union Type is
            // created only if the array includes NULL elements.
            boolean containsNull = dataArray.getType().getTag() == TypeTags.ARRAY_TAG
                    && ((BArrayType) dataArray.getType()).getElementType().getTag() == TypeTags.UNION_TAG;
            handleMappingArrayValue(nonNilType, bStruct, dataArray, fieldName, containsNull);
        } else {
            if (fieldTypeTag == TypeTags.UNION_TAG) {
                bStruct.put(fieldName, null);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleMappingArrayValue(BType nonNilType, MapValue<String, Object> bStruct, ArrayValue dataArray,
            String fieldName, boolean containsNull) throws PanickingApplicationException {
        if (nonNilType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType expectedArrayType = (BArrayType) nonNilType;
            if (((BArrayType) nonNilType).getElementType().getTag() == TypeTags.UNION_TAG) {
                handleMappingArrayElementToUnionType(expectedArrayType, dataArray, fieldName, bStruct);
            } else {
                handleMappingArrayElementToNonUnionType(containsNull, nonNilType, dataArray, bStruct, fieldName);
            }
        } else {
            handleMismatchingFieldAssignment();
        }
    }

    private void handleMappingArrayElementToNonUnionType(boolean containsNull, BType nonNilType, ArrayValue newArray,
            MapValue<String, Object> bStruct, String fieldName) throws PanickingApplicationException {
        if (containsNull) {
            throw new PanickingApplicationException(
                    "trying to assign an array containing NULL values to an array of a non-nillable element type");
        } else {
            int expectedTypeTag = ((BArrayType) nonNilType).getElementType().getTag();
            int actualTypeTag;
            if (newArray.getType().getTag() == TypeTags.DECIMAL_TAG) {
                actualTypeTag = TypeTags.DECIMAL_TAG;
            } else {
                actualTypeTag = ((BArrayType) newArray.getType()).getElementType().getTag();
            }
            validateAndSetRefRecordField(bStruct, fieldName, expectedTypeTag, actualTypeTag, newArray,
                    MISMATCHING_FIELD_ASSIGNMENT);
        }
    }

    private void handleMappingArrayElementToUnionType(BArrayType expectedArrayType, ArrayValue arrayTobeSet,
            String fieldName, MapValue<String, Object> bStruct) throws PanickingApplicationException {
        BType arrayType;
        if (arrayTobeSet.getType().getTag() == TypeTags.DECIMAL_TAG) {
            arrayType = BTypes.typeDecimal;
        } else {
            arrayType = ((BArrayType) arrayTobeSet.getType()).getElementType();
        }
        if (arrayType.getTag() == TypeTags.NULL_TAG) {
            bStruct.put(fieldName, arrayTobeSet);
        } else {
            BUnionType expectedArrayElementUnionType = (BUnionType) expectedArrayType.getElementType();
            BType expectedNonNilArrayElementType = retrieveNonNilType(expectedArrayElementUnionType.getMemberTypes());
            BType actualNonNilArrayElementType = getActualNonNilArrayElementType(arrayType);
            validateAndSetRefRecordField(bStruct, fieldName, expectedNonNilArrayElementType.getTag(),
                    actualNonNilArrayElementType.getTag(), arrayTobeSet, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private BType getActualNonNilArrayElementType(BType actualArrayElementType) {
        if (actualArrayElementType.getTag() == TypeTags.UNION_TAG) {
            return (((BUnionType) actualArrayElementType).getMemberTypes()).get(0);
        } else {
            return actualArrayElementType;
        }
    }

    private BType retrieveNonNilType(List<BType> members) throws PanickingApplicationException {
        if (members.size() != 2) {
            throw new PanickingApplicationException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
        if (members.get(0).getTag() == TypeTags.NULL_TAG) {
            return members.get(1);
        } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
            return members.get(0);
        } else {
            throw new PanickingApplicationException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private void handleStructValue(MapValue<String, Object> bStruct, String fieldName, Struct structData,
            BType fieldType) throws PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType structFieldType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            if (structFieldType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                MapValue<String, Object> userDefinedType = createUserDefinedType(structData,
                        (BRecordType) structFieldType);
                bStruct.put(fieldName, userDefinedType);
            } else {
                handleUnAssignableUnionTypeAssignment();
            }
        } else if (fieldTypeTag == TypeTags.RECORD_TYPE_TAG) {
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.RECORD_TYPE_TAG, fieldTypeTag,
                    createUserDefinedType(structData, (BRecordType) fieldType), MISMATCHING_FIELD_ASSIGNMENT);
        } else {
            handleMismatchingFieldAssignment();
        }
    }

    private void handleBooleanValue(MapValue<String, Object> bStruct, String fieldName, boolean boolValue,
            BType fieldType) throws SQLException, PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        boolean isOriginalValueNull = rs.wasNull();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            Boolean booleanValue = isOriginalValueNull ? null : boolValue;
            int[] expectedTypeTags = { TypeTags.INT_TAG, TypeTags.BOOLEAN_TAG };
            validateAndSetRefRecordField(bStruct, fieldName, expectedTypeTags, retrieveNonNilTypeTag(fieldType),
                    booleanValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                int[] expectedTypeTags = { TypeTags.INT_TAG, TypeTags.BOOLEAN_TAG };
                validateAndSetRefRecordField(bStruct, fieldName, expectedTypeTags, fieldTypeTag, boolValue,
                        MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleDateValue(MapValue<String, Object> record, String fieldName, java.util.Date date,
            BType fieldType) throws PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            handleMappingDateValueToUnionType(fieldType, record, fieldName, date);
        } else {
            handleMappingDateValueToNonUnionType(date, fieldTypeTag, record, fieldName);
        }
    }

    private void handleBinaryValue(MapValue<String, Object> bStruct, String fieldName, byte[] bytes, BType fieldType)
            throws PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType nonNillType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            if (nonNillType.getTag() == TypeTags.ARRAY_TAG) {
                int elementTypeTag = ((BArrayType) nonNillType).getElementType().getTag();
                if (elementTypeTag == TypeTags.BYTE_TAG) {
                    RefValue refValue = bytes == null ? null : new ArrayValueImpl(bytes);
                    bStruct.put(fieldName, refValue);
                } else {
                    handleUnAssignableUnionTypeAssignment();
                }
            } else {
                handleUnAssignableUnionTypeAssignment();
            }
        } else {
            if (bytes != null) {
                if (TypeTags.ARRAY_TAG == fieldTypeTag) {
                    int elementTypeTag = ((BArrayType) fieldType).getElementType().getTag();
                    if (elementTypeTag == TypeTags.BYTE_TAG) {
                        bStruct.put(fieldName, new ArrayValueImpl(bytes));
                    } else {
                        throw new PanickingApplicationException(MISMATCHING_FIELD_ASSIGNMENT);
                    }
                } else {
                    throw new PanickingApplicationException(MISMATCHING_FIELD_ASSIGNMENT);
                }
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleStringValue(String stringValue, String fieldName, MapValue<String, Object> bStruct,
            BType fieldType) throws PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            int[] expectedTypeTags = { TypeTags.STRING_TAG, TypeTags.JSON_TAG };
            validateAndSetRefRecordField(bStruct, fieldName, expectedTypeTags, retrieveNonNilTypeTag(fieldType),
                    stringValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (stringValue != null) {
                int[] expectedTypeTags = { TypeTags.STRING_TAG, TypeTags.JSON_TAG };
                validateAndSetRefRecordField(bStruct, fieldName, expectedTypeTags, fieldTypeTag, stringValue,
                        MISMATCHING_FIELD_ASSIGNMENT);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    @FunctionalInterface
    private interface ErrorHandlerFunction {
        void apply() throws PanickingApplicationException;
    }

    private ErrorHandlerFunction mismatchingFieldAssignmentHandler = this::handleMismatchingFieldAssignment;
    private ErrorHandlerFunction unassignableUnionTypeAssignmentHandler = this::handleUnAssignableUnionTypeAssignment;

    private void handleOIDValue(int index, MapValue<String, Object> bStruct, String fieldName, BType fieldType)
            throws SQLException, PanickingApplicationException {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType nonNilType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            assignOIDValue(nonNilType.getTag(), nonNilType, fieldName, index, bStruct,
                    unassignableUnionTypeAssignmentHandler);
        } else {
            // Need to call a getter method before calling ResultSet#wasNull.
            long longValue = rs.getLong(index);
            boolean isOriginalValueNull = rs.wasNull();
            if (longValue == 0 && isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                assignOIDValue(fieldTypeTag, fieldType, fieldName, index, bStruct, mismatchingFieldAssignmentHandler);
            }
        }
    }

    private void assignOIDValue(int fieldTypeTag, BType fieldType, String fieldName, int index,
            MapValue<String, Object> bStruct, ErrorHandlerFunction errorHandlerFunction)
            throws SQLException, PanickingApplicationException {
        if (fieldTypeTag == TypeTags.ARRAY_TAG) {
            int elementTypeTag = ((BArrayType) fieldType).getElementType().getTag();
            if (elementTypeTag == TypeTags.BYTE_TAG) {
                Blob blobValue = rs.getBlob(index);
                byte[] bytes = blobValue.getBytes(1L, (int) blobValue.length());
                bStruct.put(fieldName, bytes == null ? null : new ArrayValueImpl(bytes));
            } else {
                errorHandlerFunction.apply();
            }
        } else if (fieldTypeTag == TypeTags.INT_TAG) {
            bStruct.put(fieldName, rs.getLong(index));
        } else {
            errorHandlerFunction.apply();
        }
    }

    private void handleLongValue(long longValue, MapValue<String, Object> bStruct, String fieldName, BType fieldType)
            throws SQLException, PanickingApplicationException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            Long refValue = isOriginalValueNull ? null : longValue;
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.INT_TAG, retrieveNonNilTypeTag(fieldType),
                    refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.INT_TAG, fieldTypeTag, longValue,
                        MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleDoubleValue(double fValue, MapValue<String, Object> bStruct, String fieldName, BType fieldType)
            throws SQLException, PanickingApplicationException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            Double refValue = isOriginalValueNull ? null : fValue;
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.FLOAT_TAG, retrieveNonNilTypeTag(fieldType),
                    refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.FLOAT_TAG, fieldTypeTag, fValue,
                        MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleDecimalValue(BigDecimal fValue, MapValue<String, Object> bStruct, String fieldName,
            BType fieldType) throws SQLException, PanickingApplicationException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            DecimalValue refValue = isOriginalValueNull ? null : new DecimalValue(fValue);
            validateAndSetDecimalValue(bStruct, fieldName, retrieveNonNilTypeTag(fieldType), refValue,
                    UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetDecimalValue(bStruct, fieldName, fieldTypeTag, new DecimalValue(fValue),
                        MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleMappingDateValueToUnionType(BType fieldType, MapValue<String, Object> record, String fieldName,
            java.util.Date date) throws PanickingApplicationException {
        int type = retrieveNonNilTypeTag(fieldType);
        switch (type) {
        case TypeTags.STRING_TAG:
            String dateValue = SQLDatasourceUtils.getString(date);
            record.put(fieldName, dateValue);
            break;
        case TypeTags.OBJECT_TYPE_TAG:
        case TypeTags.RECORD_TYPE_TAG:
            record.put(fieldName, date != null ? createTimeStruct(date.getTime()) : null);
            break;
        case TypeTags.INT_TAG:
            record.put(fieldName, date != null ? date.getTime() : null);
            break;
        default:
            handleMismatchingFieldAssignment();
        }
    }

    private void handleMappingDateValueToNonUnionType(java.util.Date date, int fieldTypeTag,
            MapValue<String, Object> record, String fieldName) throws PanickingApplicationException {
        if (date != null) {
            switch (fieldTypeTag) {
            case TypeTags.STRING_TAG:
                String dateValue = SQLDatasourceUtils.getString(date);
                record.put(fieldName, dateValue);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                record.put(fieldName, createTimeStruct(date.getTime()));
                break;
            case TypeTags.INT_TAG:
                record.put(fieldName, date.getTime());
                break;
            default:
                handleMismatchingFieldAssignment();
            }
        } else {
            handleNilToNonNillableFieldAssignment();
        }
    }

    /**
     * This represents a column definition for a column in a table.
     */
    public static class SQLColumnDefinition extends ColumnDefinition {

        private int sqlType;

        public SQLColumnDefinition(String name, int mappedType, int sqlType) {
            super(name, mappedType);
            this.sqlType = sqlType;
        }

        public String getName() {
            return name;
        }

        public int getTypeTag() {
            return mappedTypeTag;
        }

        public int getSqlType() {
            return sqlType;
        }
    }
}
