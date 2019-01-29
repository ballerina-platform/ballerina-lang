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
package org.ballerinalang.database.sql;

import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.time.util.TimeUtils;
import org.ballerinalang.util.TableIterator;
import org.ballerinalang.util.TableResourceManager;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;
import javax.sql.rowset.CachedRowSet;

import static org.ballerinalang.database.sql.SQLDatasourceUtils.POSTGRES_DATABASE_NAME;
import static org.ballerinalang.database.sql.SQLDatasourceUtils.POSTGRES_OID_COLUMN_TYPE_NAME;

/**
 * This iterator mainly wrap java.sql.ResultSet. This will provide table operations
 * related to ballerina.data.actions.sql connector.
 *
 * @since 0.8.0
 */
public class SQLDataIterator extends TableIterator {

    private Calendar utcCalendar;
    private StructureTypeInfo timeStructInfo;
    private StructureTypeInfo zoneStructInfo;
    private static final String UNASSIGNABLE_UNIONTYPE_EXCEPTION =
            "Corresponding Union type in the record is not an assignable nillable type";
    private static final String MISMATCHING_FIELD_ASSIGNMENT = "Trying to assign to a mismatching type";
    private String sourceDatabase;

    public SQLDataIterator(TableResourceManager rm, ResultSet rs, Calendar utcCalendar,
            List<ColumnDefinition> columnDefs, BStructureType structType, StructureTypeInfo timeStructInfo,
            StructureTypeInfo zoneStructInfo, String databaseProductName) {
        super(rm, rs, structType, columnDefs);
        this.utcCalendar = utcCalendar;
        this.timeStructInfo = timeStructInfo;
        this.zoneStructInfo = zoneStructInfo;
        this.sourceDatabase = databaseProductName;
    }

    @Override
    public void close() {
        try {
            if (rs != null && !(rs instanceof CachedRowSet) && !rs.isClosed()) {
                rs.close();
            }
            resourceManager.gracefullyReleaseResources();
            rs = null;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public void reset() {
        try {
            if (rs instanceof CachedRowSet) {
                rs.beforeFirst();
            } else {
                close();
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getBlob(int columnIndex) {
        try {
            Blob bValue = rs.getBlob(columnIndex);
            return rs.wasNull() ? null : SQLDatasourceUtils.getString(bValue);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public BMap<String, BValue> generateNext() {
        if (this.type == null) {
            throw new BallerinaException("the expected record type is not specified in the remote function");
        }
        BMap<String, BValue> bStruct = new BMap<>(this.type);
        int index = 0;
        String columnName = null;
        int sqlType = -1;
        try {
            BField[] structFields = this.type.getFields().values().toArray(new BField[0]);
            if (columnDefs.size() != structFields.length) {
                throw new BallerinaException(
                        "Number of fields in the constraint type is " + (structFields.length > columnDefs.size() ?
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
                    String fieldName = field.fieldName;
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
                            sValue = new String(rs.getRowId(index).getBytes(), "UTF-8");
                            handleStringValue(sValue, fieldName, bStruct, fieldType);
                            break;
                        case Types.TINYINT:
                        case Types.SMALLINT:
                            long iValue = rs.getInt(index);
                            handleLongValue(iValue, bStruct, fieldName, fieldType);
                            break;
                        case Types.INTEGER:
                        case Types.BIGINT:
                            if (sourceDatabase.equalsIgnoreCase(POSTGRES_DATABASE_NAME)) {
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
                            double decimalValue = 0;
                            BigDecimal bigDecimalValue = rs.getBigDecimal(index);
                            if (bigDecimalValue != null) {
                                decimalValue = bigDecimalValue.doubleValue();
                            }
                            handleDoubleValue(decimalValue, bStruct, fieldName, fieldType);
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
                            throw new BallerinaException("unsupported sql type "
                                    + sqlType + " found for the column " + columnName + " index:" + index);
                    }
                }
            }
        } catch (Throwable e) {
            throw new BallerinaException(
                    "error in retrieving next value for column: " + columnName + ": of SQL Type: " + sqlType + ": "
                            + "at " + "index:" + index + ":" + e.getMessage());
        }
        return bStruct;
    }

    private void validateAndSetRefRecordField(BMap<String, BValue> bStruct, String fieldName, int expectedTypeTag,
                                              int actualTypeTag, BRefType value, String exceptionMessage) {
        if (expectedTypeTag == actualTypeTag) {
            bStruct.put(fieldName, value);
        } else {
            throw new BallerinaException(exceptionMessage);
        }
    }

    private void handleNilToNonNillableFieldAssignment() {
        throw new BallerinaException("Trying to assign a Nil value to a non-nillable field");
    }

    private void handleMismatchingFieldAssignment() {
        throw new BallerinaException("Trying to assign to a mismatching type");
    }

    private void handleUnAssignableUnionTypeAssignment() {
        throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
    }

    private int retrieveNonNilTypeTag(BType fieldType) {
        List<BType> members = ((BUnionType) fieldType).getMemberTypes();
        return retrieveNonNilType(members).getTag();
    }

    private BMap<String, BValue> createTimeStruct(long millis) {
        return TimeUtils.createTimeStruct(zoneStructInfo, timeStructInfo, millis, Constants.TIMEZONE_UTC);
    }

    private BMap<String, BValue> createUserDefinedType(Struct structValue, BStructureType structType) {
        if (structValue == null) {
            return null;
        }
        BField[] internalStructFields = structType.getFields().values().toArray(new BField[0]);
        BMap<String, BValue> struct = new BMap<>(structType);
        try {
            Object[] dataArray = structValue.getAttributes();
            if (dataArray != null) {
                if (dataArray.length != internalStructFields.length) {
                    throw new BallerinaException("specified struct and returned struct are not compatible");
                }
                int index = 0;
                for (BField internalField : internalStructFields) {
                    int type = internalField.getFieldType().getTag();
                    String fieldName = internalField.fieldName;
                    Object value = dataArray[index];
                    switch (type) {
                    case TypeTags.INT_TAG:
                        if (value instanceof BigDecimal) {
                            struct.put(fieldName, new BInteger(((BigDecimal) value).intValue()));
                        } else {
                            struct.put(fieldName, new BInteger((long) value));
                        }
                        break;
                    case TypeTags.FLOAT_TAG:
                        if (value instanceof BigDecimal) {
                            struct.put(fieldName, new BFloat(((BigDecimal) value).doubleValue()));
                        } else {
                            struct.put(fieldName, new BFloat((double) value));
                        }
                        break;
                    case TypeTags.STRING_TAG:
                        struct.put(fieldName, new BString((String) value));
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        struct.put(fieldName, new BBoolean((int) value == 1));
                        break;
                    case TypeTags.OBJECT_TYPE_TAG:
                    case TypeTags.RECORD_TYPE_TAG:
                        struct.put(fieldName,
                                createUserDefinedType((Struct) value, (BStructureType) internalField.fieldType));
                        break;
                    default:
                        throw new BallerinaException("error in retrieving UDT data for unsupported type:" + type);
                    }
                    ++index;
                }
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in retrieving UDT data:" + e.getMessage());
        }
        return struct;
    }

    private void handleArrayValue(BMap<String, BValue> bStruct, String fieldName, Array data, BType fieldType)
            throws SQLException {
        int fieldTypeTag = fieldType.getTag();
        BNewArray dataArray = getDataArray(data);
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

    private void handleMappingArrayValue(BType nonNilType, BMap<String, BValue> bStruct, BNewArray dataArray,
                                         String fieldName, boolean containsNull) {
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

    private void handleMappingArrayElementToNonUnionType(boolean containsNull, BType nonNilType, BNewArray newArray,
                                                         BMap<String, BValue> bStruct, String fieldName) {
        if (containsNull) {
            throw new BallerinaException(
                    "Trying to assign an array containing NULL values to an array of a non-nillable element type");
        } else {
            validateAndSetRefRecordField(bStruct, fieldName, ((BArrayType) nonNilType)
                    .getElementType().getTag(), ((BArrayType) newArray.getType()).getElementType().getTag(),
                    newArray, MISMATCHING_FIELD_ASSIGNMENT);
        }
    }

    private void handleMappingArrayElementToUnionType(BArrayType expectedArrayType, BNewArray arrayTobeSet,
                                                      String fieldName, BMap<String, BValue> bStruct) {
        BArrayType arrayType = (BArrayType) arrayTobeSet.getType();
        if (arrayType.getElementType().getTag() == TypeTags.NULL_TAG) {
            bStruct.put(fieldName, arrayTobeSet);
        } else {
            BUnionType expectedArrayElementUnionType = (BUnionType) expectedArrayType.getElementType();
            BType expectedNonNilArrayElementType = retrieveNonNilType(expectedArrayElementUnionType.getMemberTypes());
            BType actualNonNilArrayElementType = getActualNonNilArrayElementType(arrayType.getElementType());
            validateAndSetRefRecordField(bStruct, fieldName, expectedNonNilArrayElementType
                    .getTag(), actualNonNilArrayElementType.getTag(), arrayTobeSet, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private BType getActualNonNilArrayElementType(BType actualArrayElementType) {
        if (actualArrayElementType.getTag() == TypeTags.UNION_TAG) {
            return (((BUnionType) actualArrayElementType).getMemberTypes()).get(0);
        } else {
            return actualArrayElementType;
        }
    }

    private BType retrieveNonNilType(List<BType> members) {
        if (members.size() != 2) {
            throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
        if (members.get(0).getTag() == TypeTags.NULL_TAG) {
            return members.get(1);
        } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
            return members.get(0);
        } else {
            throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private void handleStructValue(BMap<String, BValue> bStruct, String fieldName, Struct structData, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType structFieldType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            if (structFieldType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BMap<String, BValue> userDefinedType = createUserDefinedType(structData, (BRecordType) structFieldType);
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

    private void handleBooleanValue(BMap<String, BValue> bStruct, String fieldName, boolean boolValue, BType fieldType)
            throws SQLException {
        int fieldTypeTag = fieldType.getTag();
        boolean isOriginalValueNull = rs.wasNull();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BBoolean(boolValue);
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.BOOLEAN_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.BOOLEAN_TAG, fieldTypeTag,
                        new BBoolean(boolValue), MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleDateValue(BMap<String, BValue> bStruct, String fieldName, java.util.Date date,
                                 BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            handleMappingDateValueToUnionType(fieldType, bStruct, fieldName, date);
        } else {
            handleMappingDateValueToNonUnionType(date, fieldTypeTag, bStruct, fieldName);
        }
    }

    private void handleBinaryValue(BMap<String, BValue> bStruct, String fieldName, byte[] bytes, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType nonNillType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            if (nonNillType.getTag() == TypeTags.ARRAY_TAG) {
                int elementTypeTag = ((BArrayType) nonNillType).getElementType().getTag();
                if (elementTypeTag == TypeTags.BYTE_TAG) {
                    BRefType refValue = bytes == null ? null : new BValueArray(bytes);
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
                        bStruct.put(fieldName, new BValueArray(bytes));
                    } else {
                        throw new BallerinaException(MISMATCHING_FIELD_ASSIGNMENT);
                    }
                } else {
                    throw new BallerinaException(MISMATCHING_FIELD_ASSIGNMENT);
                }
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleStringValue(String stringValue, String fieldName,
                                   BMap<String, BValue> bStruct, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = stringValue == null ? null : new BString(stringValue);
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.STRING_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (stringValue != null) {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.STRING_TAG, fieldTypeTag,
                        new BString(stringValue), MISMATCHING_FIELD_ASSIGNMENT);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    @FunctionalInterface
    private interface ErrorHandlerFunction {
        void apply();
    }

    private ErrorHandlerFunction mismatchingFieldAssignmentHandler = this::handleMismatchingFieldAssignment;
    private ErrorHandlerFunction unassignableUnionTypeAssignmentHandler = this::handleUnAssignableUnionTypeAssignment;

    private void handleOIDValue(int index, BMap<String, BValue> bStruct, String fieldName, BType fieldType)
            throws SQLException {
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
            BMap<String, BValue> bStruct, ErrorHandlerFunction errorHandlerFunction) throws SQLException {
        if (fieldTypeTag == TypeTags.ARRAY_TAG) {
            int elementTypeTag = ((BArrayType) fieldType).getElementType().getTag();
            if (elementTypeTag == TypeTags.BYTE_TAG) {
                Blob blobValue = rs.getBlob(index);
                byte[] bytes = blobValue.getBytes(1L, (int) blobValue.length());
                bStruct.put(fieldName, bytes == null ? null : new BValueArray(bytes));
            } else {
                errorHandlerFunction.apply();
            }
        } else if (fieldTypeTag == TypeTags.INT_TAG) {
            bStruct.put(fieldName, new BInteger(rs.getLong(index)));
        } else {
            errorHandlerFunction.apply();
        }
    }

    private void handleLongValue(long longValue, BMap<String, BValue> bStruct, String fieldName, BType fieldType)
            throws SQLException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BInteger(longValue);
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.INT_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.INT_TAG, fieldTypeTag,
                        new BInteger(longValue), MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleDoubleValue(double fValue, BMap<String, BValue> bStruct, String fieldName, BType fieldType)
            throws SQLException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BFloat(fValue);
            validateAndSetRefRecordField(bStruct, fieldName, TypeTags.FLOAT_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                validateAndSetRefRecordField(bStruct, fieldName, TypeTags.FLOAT_TAG, fieldTypeTag,
                        new BFloat(fValue), MISMATCHING_FIELD_ASSIGNMENT);
            }
        }
    }

    private void handleMappingDateValueToUnionType(BType fieldType, BMap<String, BValue> bStruct,
                                                   String fieldName, java.util.Date date) {
        int type = retrieveNonNilTypeTag(fieldType);
        switch (type) {
        case TypeTags.STRING_TAG:
            String dateValue = SQLDatasourceUtils.getString(date);
            bStruct.put(fieldName, dateValue != null ? new BString(dateValue) : null);
            break;
        case TypeTags.OBJECT_TYPE_TAG:
        case TypeTags.RECORD_TYPE_TAG:
            bStruct.put(fieldName, date != null ? createTimeStruct(date.getTime()) : null);
            break;
        case TypeTags.INT_TAG:
            bStruct.put(fieldName, date != null ? new BInteger(date.getTime()) : null);
            break;
        default:
            handleMismatchingFieldAssignment();
        }
    }

    private void handleMappingDateValueToNonUnionType(java.util.Date date, int fieldTypeTag,
                                                      BMap<String, BValue> bStruct, String fieldName) {
        if (date != null) {
            switch (fieldTypeTag) {
            case TypeTags.STRING_TAG:
                String dateValue = SQLDatasourceUtils.getString(date);
                bStruct.put(fieldName, new BString(dateValue));
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                bStruct.put(fieldName, createTimeStruct(date.getTime()));
                break;
            case TypeTags.INT_TAG:
                bStruct.put(fieldName, new BInteger(date.getTime()));
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
