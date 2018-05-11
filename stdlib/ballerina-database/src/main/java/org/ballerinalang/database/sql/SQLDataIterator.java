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
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.Utils;
import org.ballerinalang.util.TableIterator;
import org.ballerinalang.util.TableResourceManager;
import org.ballerinalang.util.codegen.StructInfo;
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
    private StructInfo timeStructInfo;
    private StructInfo zoneStructInfo;
    private static final String UNASSIGNABLE_UNIONTYPE_EXCEPTION = "Corresponding Union type in the record is not an "
            + "assignable nillable type";

    public SQLDataIterator(Calendar utcCalendar, BStructType structType, StructInfo timeStructInfo,
            StructInfo zoneStructInfo, TableResourceManager rm, ResultSet rs, List<ColumnDefinition> columnDefs)
            throws SQLException {
        super(rm, rs, structType, columnDefs);
        this.utcCalendar = utcCalendar;
        this.timeStructInfo = timeStructInfo;
        this.zoneStructInfo = zoneStructInfo;
    }

    public SQLDataIterator(TableResourceManager rm, ResultSet rs, Calendar utcCalendar,
            List<ColumnDefinition> columnDefs, BStructType structType, StructInfo timeStructInfo,
            StructInfo zoneStructInfo) throws SQLException {
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
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        int index = 0;
        String columnName = null;
        int sqlType = -1;
        try {
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    columnName = def.getName();
                    sqlType = def.getSqlType();
                    BStructType.StructField[] structFields = this.type.getStructFields();
                    ++index;
                    int fieldTypeTag = structFields[index - 1].getFieldType().getTag();
                    boolean isOriginalValueNull;
                    switch (sqlType) {
                    case Types.ARRAY:
                        Array dataArray = rs.getArray(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.ARRAY_TAG,
                                    retrieveType(structFields, index - 1), getDataArray(dataArray));
                        } else {
                            bStruct.setRefField(++refRegIndex, getDataArray(dataArray));
                        }
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                        String sValue = rs.getString(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = sValue == null ? null : new BString(sValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.STRING_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (sValue != null) {
                                bStruct.setStringField(++stringRegIndex, sValue);
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        Blob blobValue = rs.getBlob(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = blobValue == null ?
                                    null :
                                    new BBlob(blobValue.getBytes(1L, (int) blobValue.length()));
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.BLOB_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (blobValue != null) {
                                bStruct.setBlobField(++blobRegIndex, blobValue.getBytes(1L, (int) blobValue.length()));
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.CLOB:
                        String clobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = clobValue == null ? null : new BString(clobValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.STRING_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (clobValue != null) {
                                bStruct.setStringField(++stringRegIndex, clobValue);
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.NCLOB:
                        String nClobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = nClobValue == null ? null : new BString(nClobValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.STRING_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (nClobValue != null) {
                                bStruct.setStringField(++stringRegIndex, nClobValue);
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.DATE:
                        Date date = rs.getDate(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            int type = retrieveType(structFields, index - 1);
                            switch (type) {
                            case TypeTags.STRING_TAG:
                                String dateValue = SQLDatasourceUtils.getString(date);
                                bStruct.setRefField(++stringRegIndex,
                                        dateValue != null ? new BString(dateValue) : null);
                                break;
                            case TypeTags.STRUCT_TAG:
                                bStruct.setRefField(++refRegIndex,
                                        date != null ? createTimeStruct(date.getTime()) : null);
                                break;
                            case TypeTags.INT_TAG:
                                bStruct.setRefField(++longRegIndex, date != null ? new BInteger(date.getTime()) : null);
                                break;
                            default:
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (date != null) {
                                switch (fieldTypeTag) {
                                case TypeTags.STRING_TAG:
                                    String dateValue = SQLDatasourceUtils.getString(date);
                                    bStruct.setStringField(++stringRegIndex, dateValue);
                                    break;
                                case TypeTags.STRUCT_TAG:
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(date.getTime()));
                                    break;
                                case TypeTags.INT_TAG:
                                    bStruct.setIntField(++longRegIndex, date.getTime());
                                    break;
                                default:
                                    handleMismatchingFieldAssignment();
                                }
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        Time time = rs.getTime(index, utcCalendar);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            int type = retrieveType(structFields, index - 1);
                            switch (type) {
                            case TypeTags.STRING_TAG:
                                String timeValue = SQLDatasourceUtils.getString(time);
                                bStruct.setRefField(++stringRegIndex,
                                        timeValue != null ? new BString(timeValue) : null);
                                break;
                            case TypeTags.STRUCT_TAG:
                                bStruct.setRefField(++refRegIndex,
                                        time != null ? createTimeStruct(time.getTime()) : null);
                                break;
                            case TypeTags.INT_TAG:
                                bStruct.setRefField(++longRegIndex, time != null ? new BInteger(time.getTime()) : null);
                                break;
                            default:
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (time != null) {
                                switch (fieldTypeTag) {
                                case TypeTags.STRING_TAG:
                                    String timeValue = SQLDatasourceUtils.getString(time);
                                    bStruct.setStringField(++stringRegIndex, timeValue);
                                    break;
                                case TypeTags.STRUCT_TAG:
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(time.getTime()));
                                    break;
                                case TypeTags.INT_TAG:
                                    bStruct.setIntField(++longRegIndex, time.getTime());
                                    break;
                                default:
                                    handleMismatchingFieldAssignment();
                                }
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        Timestamp timestamp = rs.getTimestamp(index, utcCalendar);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            int type = retrieveType(structFields, index - 1);
                            switch (type) {
                            case TypeTags.STRING_TAG:
                                String timestampValue = SQLDatasourceUtils.getString(timestamp);
                                bStruct.setRefField(++stringRegIndex,
                                        timestampValue != null ? new BString(timestampValue) : null);
                                break;
                            case TypeTags.STRUCT_TAG:
                                bStruct.setRefField(++refRegIndex,
                                        timestamp != null ? createTimeStruct(timestamp.getTime()) : null);
                                break;
                            case TypeTags.INT_TAG:
                                bStruct.setRefField(++longRegIndex,
                                        timestamp != null ? new BInteger(timestamp.getTime()) : null);
                                break;
                            default:
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (timestamp != null) {
                                switch (fieldTypeTag) {
                                case TypeTags.STRING_TAG:
                                    String timestmpValue = SQLDatasourceUtils.getString(timestamp);
                                    bStruct.setStringField(++stringRegIndex, timestmpValue);
                                    break;
                                case TypeTags.STRUCT_TAG:
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(timestamp.getTime()));
                                    break;
                                case TypeTags.INT_TAG:
                                    bStruct.setIntField(++longRegIndex, timestamp.getTime());
                                    break;
                                default:
                                    handleMismatchingFieldAssignment();
                                }
                            } else {
                                handleNilToNonNillableFieldAssignment();
                            }
                        }
                        break;
                    case Types.ROWID:
                        sValue = new String(rs.getRowId(index).getBytes(), "UTF-8");
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = new BString(sValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.STRING_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            bStruct.setStringField(++stringRegIndex, sValue);
                        }
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                        long iValue = rs.getInt(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BInteger(iValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.INT_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setIntField(++longRegIndex, iValue);
                            }
                        }
                        break;
                    case Types.INTEGER:
                    case Types.BIGINT:
                        long lValue = rs.getLong(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BInteger(lValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.INT_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setIntField(++longRegIndex, lValue);
                            }
                        }
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        double fValue = rs.getFloat(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BFloat(fValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.FLOAT_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setFloatField(++doubleRegIndex, fValue);
                            }
                        }
                        break;
                    case Types.DOUBLE:
                        double dValue = rs.getDouble(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BFloat(dValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.FLOAT_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setFloatField(++doubleRegIndex, dValue);
                            }
                        }
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        double decimalValue = 0;
                        BigDecimal bigDecimalValue = rs.getBigDecimal(index);
                        isOriginalValueNull = rs.wasNull();
                        if (bigDecimalValue != null) {
                            decimalValue = bigDecimalValue.doubleValue();
                        }
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BFloat(decimalValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.FLOAT_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setFloatField(++doubleRegIndex, decimalValue);
                            }
                        }
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        boolean boolValue = rs.getBoolean(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BBoolean(boolValue);
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.BOOLEAN_TAG,
                                    retrieveType(structFields, index - 1), refValue);
                        } else {
                            if (isOriginalValueNull) {
                                handleNilToNonNillableFieldAssignment();
                            } else {
                                bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                            }
                        }
                        break;
                    case Types.STRUCT:
                        Struct structdata = (Struct) rs.getObject(index);
                        BType structFieldType = this.type.getStructFields()[index - 1].getFieldType();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            validateAndSetRefRecordField(bStruct, ++refRegIndex, TypeTags.STRUCT_TAG,
                                    retrieveType(structFields, index - 1),
                                    createUserDefinedType(structdata, (BStructType) structFieldType));
                        } else if (fieldTypeTag == TypeTags.STRUCT_TAG) {
                            bStruct.setRefField(++refRegIndex,
                                    createUserDefinedType(structdata, (BStructType) structFieldType));
                        }
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
            BRefType value) {
        if (expectedTypeTag == actualTypeTag) {
            bStruct.setRefField(refRegIndex, value);
        } else {
            throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private void handleNilToNonNillableFieldAssignment() {
        throw new BallerinaException("Trying to assign a Nil value to a non-nillable field");
    }

    private void handleMismatchingFieldAssignment() {
        throw new BallerinaException("Trying to assign to a mismatching field");
    }

    private int retrieveType(BStructType.StructField[] structFields, int index) {
        List<BType> members = ((BUnionType) structFields[index].getFieldType()).getMemberTypes();
        if (members.size() != 2) {
            throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
        if (members.get(0).getTag() == TypeTags.NULL_TAG) {
            return members.get(1).getTag();
        } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
            return members.get(0).getTag();
        } else {
            throw new BallerinaException(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
        }
    }

    private BStruct createTimeStruct(long millis) {
        return Utils.createTimeStruct(zoneStructInfo, timeStructInfo, millis, Constants.TIMEZONE_UTC);
    }

    private BStruct createUserDefinedType(Struct structValue, BStructType structType) {
        if (structValue == null) {
            return null;
        }
        BStructType.StructField[] internalStructFields = structType.getStructFields();
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
                for (BStructType.StructField internalField : internalStructFields) {
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
                    case TypeTags.STRUCT_TAG:
                        struct.setRefField(++refRegIndex,
                                createUserDefinedType((Struct) value, (BStructType) internalField.fieldType));
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
