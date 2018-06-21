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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
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
            "Corresponding Union type in the record is not an " + "assignable nillable type";
    private static final String MISMATCHING_FIELD_ASSIGNMENT = "Trying to assign to a mismatching type";

    public SQLDataIterator(Calendar utcCalendar, BStructureType structType, StructureTypeInfo timeStructInfo,
                           StructureTypeInfo zoneStructInfo, TableResourceManager rm,
                           ResultSet rs, List<ColumnDefinition> columnDefs) {
        super(rm, rs, structType, columnDefs);
        this.utcCalendar = utcCalendar;
        this.timeStructInfo = timeStructInfo;
        this.zoneStructInfo = zoneStructInfo;
    }

    public SQLDataIterator(TableResourceManager rm, ResultSet rs, Calendar utcCalendar,
            List<ColumnDefinition> columnDefs, BStructureType structType, StructureTypeInfo timeStructInfo,
                           StructureTypeInfo zoneStructInfo) {
        super(rm, rs, structType, columnDefs);
        this.utcCalendar = utcCalendar;
        this.timeStructInfo = timeStructInfo;
        this.zoneStructInfo = zoneStructInfo;
    }

    @Override
    public void close(boolean isInTransaction) {
        try {
            if (rs != null && !(rs instanceof CachedRowSet) && !rs.isClosed()) {
                rs.close();
            }
            resourceManager.gracefullyReleaseResources(isInTransaction);
            rs = null;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public void reset(boolean isInTransaction) {
        try {
            if (rs instanceof CachedRowSet) {
                rs.beforeFirst();
            } else {
                close(isInTransaction);
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getBlob(int columnIndex) {
        try {
            Blob bValue = rs.getBlob(columnIndex);
            return SQLDatasourceUtils.getString(bValue);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public BStruct generateNext() {
        if (this.type == null) {
            throw new BallerinaException("the expected struct type is not specified in action");
        }
        BStruct bStruct = new BStruct(this.type);
        RegistryIndex longRegIndex = new RegistryIndex(-1);
        RegistryIndex doubleRegIndex = new RegistryIndex(-1);
        RegistryIndex stringRegIndex = new RegistryIndex(-1);
        RegistryIndex booleanRegIndex = new RegistryIndex(-1);
        RegistryIndex blobRegIndex = new RegistryIndex(-1);
        RegistryIndex refRegIndex = new RegistryIndex(-1);
        int index = 0;
        String columnName = null;
        int sqlType = -1;
        try {
            BField[] structFields = this.type.getFields();
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    columnName = def.getName();
                    sqlType = def.getSqlType();
                    ++index;
                    BType fieldType = structFields[index - 1].getFieldType();
                    switch (sqlType) {
                    case Types.ARRAY:
                        Array data = rs.getArray(index);
                        handleArrayValue(bStruct, refRegIndex, data, fieldType);
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                        String sValue = rs.getString(index);
                        handleStringValue(sValue, refRegIndex, stringRegIndex, bStruct, fieldType);
                        break;
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        byte[] binaryValue = rs.getBytes(index);
                        handleBinaryValue(bStruct, refRegIndex, blobRegIndex, binaryValue, fieldType);
                        break;
                    case Types.BLOB:
                        Blob blobValue = rs.getBlob(index);
                        handleBinaryValue(bStruct, refRegIndex, blobRegIndex,
                                blobValue == null ? null : blobValue.getBytes(1L, (int) blobValue.length()), fieldType);
                        break;
                    case Types.CLOB:
                        String clobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        handleStringValue(clobValue, refRegIndex, stringRegIndex, bStruct, fieldType);
                        break;
                    case Types.NCLOB:
                        String nClobValue = SQLDatasourceUtils.getString((rs.getNClob(index)));
                        handleStringValue(nClobValue, refRegIndex, stringRegIndex, bStruct, fieldType);
                        break;
                    case Types.DATE:
                        Date date = rs.getDate(index);
                        handleDateValue(bStruct, stringRegIndex, refRegIndex, longRegIndex, date, fieldType);
                        break;
                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        Time time = rs.getTime(index, utcCalendar);
                        handleDateValue(bStruct, stringRegIndex, refRegIndex, longRegIndex, time, fieldType);
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        Timestamp timestamp = rs.getTimestamp(index, utcCalendar);
                        handleDateValue(bStruct, stringRegIndex, refRegIndex, longRegIndex, timestamp, fieldType);
                        break;
                    case Types.ROWID:
                        sValue = new String(rs.getRowId(index).getBytes(), "UTF-8");
                        handleStringValue(sValue, refRegIndex, stringRegIndex, bStruct, fieldType);
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                        long iValue = rs.getInt(index);
                        handleLongValue(iValue, bStruct, refRegIndex, longRegIndex, fieldType);
                        break;
                    case Types.INTEGER:
                    case Types.BIGINT:
                        long lValue = rs.getLong(index);
                        handleLongValue(lValue, bStruct, refRegIndex, longRegIndex, fieldType);
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        double fValue = rs.getFloat(index);
                        handleDoubleValue(fValue, bStruct, refRegIndex, doubleRegIndex, fieldType);
                        break;
                    case Types.DOUBLE:
                        double dValue = rs.getDouble(index);
                        handleDoubleValue(dValue, bStruct, refRegIndex, doubleRegIndex, fieldType);
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        double decimalValue = 0;
                        BigDecimal bigDecimalValue = rs.getBigDecimal(index);
                        if (bigDecimalValue != null) {
                            decimalValue = bigDecimalValue.doubleValue();
                        }
                        handleDoubleValue(decimalValue, bStruct, refRegIndex, doubleRegIndex, fieldType);
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        boolean boolValue = rs.getBoolean(index);
                        handleBooleanValue(bStruct, refRegIndex, booleanRegIndex, boolValue, fieldType);
                        break;
                    case Types.STRUCT:
                        Struct structData = (Struct) rs.getObject(index);
                        handleStructValue(bStruct, refRegIndex, structData, fieldType);
                        break;
                    default:
                        throw new BallerinaException(
                                "unsupported sql type " + sqlType + " found for the column " + columnName + " index:"
                                        + index);
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

    private void validateAndSetRefRecordField(BStruct bStruct, int refRegIndex, int expectedTypeTag, int actualTypeTag,
            BRefType value, String exceptionMessage) {
        if (expectedTypeTag == actualTypeTag) {
            bStruct.setRefField(refRegIndex, value);
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

    private int retrieveNonNilTypeTag(BType fieldType) {
        List<BType> members = ((BUnionType) fieldType).getMemberTypes();
        return retrieveNonNilType(members).getTag();
    }

    private BStruct createTimeStruct(long millis) {
        return TimeUtils.createTimeStruct(zoneStructInfo, timeStructInfo, millis, Constants.TIMEZONE_UTC);
    }

    private BStruct createUserDefinedType(Struct structValue, BStructureType structType) {
        if (structValue == null) {
            return null;
        }
        BField[] internalStructFields = structType.getFields();
        BStruct struct = new BStruct(structType);
        try {
            Object[] dataArray = structValue.getAttributes();
            if (dataArray != null) {
                if (dataArray.length != internalStructFields.length) {
                    throw new BallerinaException("specified struct and returned struct are not compatible");
                }
                int longRegIndex = -1;
                int doubleRegIndex = -1;
                int stringRegIndex = -1;
                int booleanRegIndex = -1;
                int refRegIndex = -1;
                int index = 0;
                for (BField internalField : internalStructFields) {
                    int type = internalField.getFieldType().getTag();
                    Object value = dataArray[index];
                    switch (type) {
                    case TypeTags.INT_TAG:
                        if (value instanceof BigDecimal) {
                            struct.setIntField(++longRegIndex, ((BigDecimal) value).intValue());
                        } else {
                            struct.setIntField(++longRegIndex, (long) value);
                        }
                        break;
                    case TypeTags.FLOAT_TAG:
                        if (value instanceof BigDecimal) {
                            struct.setFloatField(++doubleRegIndex, ((BigDecimal) value).doubleValue());
                        } else {
                            struct.setFloatField(++doubleRegIndex, (double) value);
                        }
                        break;
                    case TypeTags.STRING_TAG:
                        struct.setStringField(++stringRegIndex, (String) value);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        struct.setBooleanField(++booleanRegIndex, (int) value);
                        break;
                    case TypeTags.OBJECT_TYPE_TAG:
                    case TypeTags.RECORD_TYPE_TAG:
                        struct.setRefField(++refRegIndex,
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

    private void handleArrayValue(BStruct bStruct, RegistryIndex refRegIndex, Array data, BType fieldType)
            throws SQLException {
        int fieldTypeTag = fieldType.getTag();
        BNewArray dataArray = getDataArray(data);
        if (dataArray != null) {
            BType nonNilType = fieldType;
            if (fieldTypeTag == TypeTags.UNION_TAG) {
                nonNilType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            }
            // The dataArray is created from the array returned from the database. There, a Union Type is
            // created only if the array includes NULL elements.
            boolean containsNull = dataArray.getType().getTag() == TypeTags.UNION_TAG;
            handleMappingArrayValue(nonNilType, bStruct, dataArray, refRegIndex, containsNull);
        } else {
            if (fieldTypeTag == TypeTags.UNION_TAG) {
                bStruct.setRefField(refRegIndex.incrementAndGet(), null);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleMappingArrayValue(BType nonNilType, BStruct bStruct, BNewArray dataArray,
            RegistryIndex refRegIndex, boolean containsNull) {
        if (nonNilType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType expectedArrayType = (BArrayType) nonNilType;
            if (((BArrayType) nonNilType).getElementType().getTag() == TypeTags.UNION_TAG) {
                handleMappingArrayElementToUnionType(expectedArrayType, dataArray, refRegIndex, bStruct);
            } else {
                handleMappingArrayElementToNonUnionType(containsNull, nonNilType, dataArray, bStruct, refRegIndex);
            }
        } else {
            handleMismatchingFieldAssignment();
        }
    }

    private void handleMappingArrayElementToNonUnionType(boolean containsNull, BType nonNilType, BNewArray newArray,
            BStruct bStruct, RegistryIndex refRegIndex) {
        if (containsNull) {
            throw new BallerinaException(
                    "Trying to assign an array containing NULL values to an array of a non-nillable element type");
        } else {
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), ((BArrayType) nonNilType)
                    .getElementType().getTag(), ((BArrayType) newArray.getType()).getElementType().getTag(),
                    newArray, MISMATCHING_FIELD_ASSIGNMENT);
        }
    }

    private void handleMappingArrayElementToUnionType(BArrayType expectedArrayType, BNewArray arrayTobeSet,
            RegistryIndex refRegIndex, BStruct bStruct) {
        BType actualArrayElementType = arrayTobeSet.getType();
        if (actualArrayElementType.getTag() == TypeTags.NULL_TAG) {
            bStruct.setRefField(refRegIndex.incrementAndGet(), arrayTobeSet);
        } else {
            BUnionType expectedArrayElementUnionType = (BUnionType) expectedArrayType.getElementType();
            BType expectedNonNilArrayElementType = retrieveNonNilType(expectedArrayElementUnionType.getMemberTypes());
            BType actualNonNilArrayElementType = getActualNonNilArrayElementType(actualArrayElementType);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), expectedNonNilArrayElementType
                    .getTag(), actualNonNilArrayElementType.getTag(), arrayTobeSet, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private BType getActualNonNilArrayElementType(BType actualArrayElementType) {
        if (actualArrayElementType.getTag() == TypeTags.UNION_TAG) {
            return (((BUnionType) actualArrayElementType).getMemberTypes()).get(0);
        } else {
            return ((BArrayType) actualArrayElementType).getElementType();
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

    private void handleStructValue(BStruct bStruct, RegistryIndex refRegIndex, Struct structData, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BType structFieldType = retrieveNonNilType(((BUnionType) fieldType).getMemberTypes());
            if (structFieldType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BStruct userDefinedType = createUserDefinedType(structData, (BRecordType) structFieldType);
                bStruct.setRefField(refRegIndex.incrementAndGet(), userDefinedType);
            } else {
                throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
            }
        } else if (fieldTypeTag == TypeTags.RECORD_TYPE_TAG) {
            bStruct.setRefField(refRegIndex.incrementAndGet(),
                    createUserDefinedType(structData, (BRecordType) fieldType));
        } else {
            handleMismatchingFieldAssignment();
        }
    }

    private void handleBooleanValue(BStruct bStruct, RegistryIndex refRegIndex, RegistryIndex booleanRegIndex,
            boolean boolValue, BType fieldType) throws SQLException {
        int fieldTypeTag = fieldType.getTag();
        boolean isOriginalValueNull = rs.wasNull();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BBoolean(boolValue);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), TypeTags.BOOLEAN_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                bStruct.setBooleanField(booleanRegIndex.incrementAndGet(), boolValue ? 1 : 0);
            }
        }
    }

    private void handleDateValue(BStruct bStruct, RegistryIndex stringRegIndex, RegistryIndex refRegIndex,
            RegistryIndex longRegIndex, java.util.Date date, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            handleMappingDateValueToUnionType(fieldType, bStruct, refRegIndex, date);
        } else {
            handleMappingDateValueToNonUnionType(date, fieldTypeTag, bStruct, stringRegIndex, refRegIndex,
                    longRegIndex);
        }
    }

    private void handleBinaryValue(BStruct bStruct, RegistryIndex refRegIndex, RegistryIndex blobRegIndex, byte[] bytes,
            BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = bytes == null ? null : new BBlob(bytes);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), TypeTags.BLOB_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (bytes != null) {
                bStruct.setBlobField(blobRegIndex.incrementAndGet(), bytes);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleStringValue(String stringValue, RegistryIndex refRegIndex, RegistryIndex stringRegIndex,
            BStruct bStruct, BType fieldType) {
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = stringValue == null ? null : new BString(stringValue);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), TypeTags.STRING_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (stringValue != null) {
                bStruct.setStringField(stringRegIndex.incrementAndGet(), stringValue);
            } else {
                handleNilToNonNillableFieldAssignment();
            }
        }
    }

    private void handleLongValue(long longValue, BStruct bStruct, RegistryIndex refRegIndex, RegistryIndex longRegIndex,
            BType fieldType) throws SQLException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BInteger(longValue);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), TypeTags.INT_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                bStruct.setIntField(longRegIndex.incrementAndGet(), longValue);
            }
        }
    }

    private void handleDoubleValue(double fValue, BStruct bStruct, RegistryIndex refRegIndex,
            RegistryIndex doubleRegIndex, BType fieldType) throws SQLException {
        boolean isOriginalValueNull = rs.wasNull();
        int fieldTypeTag = fieldType.getTag();
        if (fieldTypeTag == TypeTags.UNION_TAG) {
            BRefType refValue = isOriginalValueNull ? null : new BFloat(fValue);
            validateAndSetRefRecordField(bStruct, refRegIndex.incrementAndGet(), TypeTags.FLOAT_TAG,
                    retrieveNonNilTypeTag(fieldType), refValue, UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        } else {
            if (isOriginalValueNull) {
                handleNilToNonNillableFieldAssignment();
            } else {
                bStruct.setFloatField(doubleRegIndex.incrementAndGet(), fValue);
            }
        }
    }

    private void handleMappingDateValueToUnionType(BType fieldType, BStruct bStruct, RegistryIndex refRegIndex,
            java.util.Date date) {
        int type = retrieveNonNilTypeTag(fieldType);
        switch (type) {
        case TypeTags.STRING_TAG:
            String dateValue = SQLDatasourceUtils.getString(date);
            bStruct.setRefField(refRegIndex.incrementAndGet(), dateValue != null ? new BString(dateValue) : null);
            break;
        case TypeTags.OBJECT_TYPE_TAG:
        case TypeTags.RECORD_TYPE_TAG:
            bStruct.setRefField(refRegIndex.incrementAndGet(), date != null ? createTimeStruct(date.getTime()) : null);
            break;
        case TypeTags.INT_TAG:
            bStruct.setRefField(refRegIndex.incrementAndGet(), date != null ? new BInteger(date.getTime()) : null);
            break;
        default:
            handleMismatchingFieldAssignment();
        }
    }

    private void handleMappingDateValueToNonUnionType(java.util.Date date, int fieldTypeTag, BStruct bStruct,
            RegistryIndex stringRegIndex, RegistryIndex refRegIndex, RegistryIndex longRegIndex) {
        if (date != null) {
            switch (fieldTypeTag) {
            case TypeTags.STRING_TAG:
                String dateValue = SQLDatasourceUtils.getString(date);
                bStruct.setStringField(stringRegIndex.incrementAndGet(), dateValue);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                bStruct.setRefField(refRegIndex.incrementAndGet(), createTimeStruct(date.getTime()));
                break;
            case TypeTags.INT_TAG:
                bStruct.setIntField(longRegIndex.incrementAndGet(), date.getTime());
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

    /**
     * This represents a Registry Index of a @code{BStruct} instance.
     */
    private static class RegistryIndex {
        private int index;

        private RegistryIndex(int index) {
            this.index = index;
        }

        private int incrementAndGet() {
            return ++index;
        }
    }
}
