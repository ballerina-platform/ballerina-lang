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
        try {
            for (ColumnDefinition columnDef : columnDefs) {
                if (columnDef instanceof SQLColumnDefinition) {
                    SQLColumnDefinition def = (SQLColumnDefinition) columnDef;
                    columnName = def.getName();
                    int sqlType = def.getSqlType();
                    BStructType.StructField[] structFields = this.type.getStructFields();
                    ++index;
                    int fieldTypeTag = this.type.getStructFields()[index - 1].getFieldType().getTag();
                    boolean isOriginalValueNull = false;
                    switch (sqlType) {
                    case Types.ARRAY:
                        Array dataArray = rs.getArray(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex,
                                    getDataArray(dataArray), TypeTags.ARRAY_TAG);
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
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.STRING_TAG);
                        } else {
                            if (sValue != null) {
                                bStruct.setStringField(++stringRegIndex, sValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        Blob blobValue = rs.getBlob(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = blobValue == null ? null :
                                    new BBlob(blobValue.getBytes(1L, (int) blobValue.length()));
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.BLOB_TAG);
                        } else {
                            if (blobValue != null) {
                                bStruct.setBlobField(++blobRegIndex, blobValue.getBytes(1L, (int) blobValue.length()));
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.CLOB:
                        String clobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = clobValue == null ? null : new BString(clobValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.STRING_TAG);
                        } else {
                            if (clobValue != null) {
                                bStruct.setStringField(++stringRegIndex, clobValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.NCLOB:
                        String nClobValue = SQLDatasourceUtils.getString((rs.getClob(index)));
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = nClobValue == null ? null : new BString(nClobValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.STRING_TAG);
                        } else {
                            if (nClobValue != null) {
                                bStruct.setStringField(++stringRegIndex, nClobValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.DATE:
                        Date date = rs.getDate(index);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            if (isNillableType(structFields, index - 1, TypeTags.STRING_TAG)) {
                                String dateValue = SQLDatasourceUtils.getString(date);
                                bStruct.setRefField(++stringRegIndex,
                                        dateValue != null ? new BString(dateValue) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.STRUCT_TAG)) {
                                bStruct.setRefField(++refRegIndex,
                                        date != null ? createTimeStruct(date.getTime()) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.INT_TAG)) {
                                bStruct.setRefField(++longRegIndex, date != null ? new BInteger(date.getTime()) : null);
                            } else {
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (date != null) {
                                if (fieldTypeTag == TypeTags.STRING_TAG) {
                                    String dateValue = SQLDatasourceUtils.getString(date);
                                    bStruct.setStringField(++stringRegIndex, dateValue);
                                } else if (fieldTypeTag == TypeTags.STRUCT_TAG) {
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(date.getTime()));
                                } else if (fieldTypeTag == TypeTags.INT_TAG) {
                                    bStruct.setIntField(++longRegIndex, date.getTime());
                                }
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.TIME:
                    case Types.TIME_WITH_TIMEZONE:
                        Time time = rs.getTime(index, utcCalendar);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            if (isNillableType(structFields, index - 1, TypeTags.STRING_TAG)) {
                                String timeValue = SQLDatasourceUtils.getString(time);
                                bStruct.setRefField(++stringRegIndex,
                                        timeValue != null ? new BString(timeValue) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.STRUCT_TAG)) {
                                bStruct.setRefField(++refRegIndex,
                                        time != null ? createTimeStruct(time.getTime()) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.INT_TAG)) {
                                bStruct.setRefField(++longRegIndex, time != null ? new BInteger(time.getTime()) : null);
                            } else {
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (time != null) {
                                if (fieldTypeTag == TypeTags.STRING_TAG) {
                                    String timeValue = SQLDatasourceUtils.getString(time);
                                    bStruct.setStringField(++stringRegIndex, timeValue);
                                } else if (fieldTypeTag == TypeTags.STRUCT_TAG) {
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(time.getTime()));
                                } else if (fieldTypeTag == TypeTags.INT_TAG) {
                                    bStruct.setIntField(++longRegIndex, time.getTime());
                                } else {
                                    handleMismatchingFieldAssignment();
                                }
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        Timestamp timestamp = rs.getTimestamp(index, utcCalendar);
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            if (isNillableType(structFields, index - 1, TypeTags.STRING_TAG)) {
                                String timestampValue = SQLDatasourceUtils.getString(timestamp);
                                bStruct.setRefField(++stringRegIndex,
                                        timestampValue != null ? new BString(timestampValue) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.STRUCT_TAG)) {
                                bStruct.setRefField(++refRegIndex,
                                        timestamp != null ? createTimeStruct(timestamp.getTime()) : null);
                            } else if (isNillableType(structFields, index - 1, TypeTags.INT_TAG)) {
                                bStruct.setRefField(++longRegIndex,
                                        timestamp != null ? new BInteger(timestamp.getTime()) : null);
                            } else {
                                handleMismatchingFieldAssignment();
                            }
                        } else {
                            if (timestamp != null) {
                                if (fieldTypeTag == TypeTags.STRING_TAG) {
                                    String timestmpValue = SQLDatasourceUtils.getString(timestamp);
                                    bStruct.setStringField(++stringRegIndex, timestmpValue);
                                } else if (fieldTypeTag == TypeTags.STRUCT_TAG) {
                                    bStruct.setRefField(++refRegIndex, createTimeStruct(timestamp.getTime()));
                                } else if (fieldTypeTag == TypeTags.INT_TAG) {
                                    bStruct.setIntField(++longRegIndex, timestamp.getTime());
                                }
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.ROWID:
                        sValue = new String(rs.getRowId(index).getBytes(), "UTF-8");
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = new BString(sValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.STRING_TAG);
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
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.INT_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setIntField(++longRegIndex, iValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.INTEGER:
                    case Types.BIGINT:
                        long lValue = rs.getLong(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BInteger(lValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.INT_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setIntField(++longRegIndex, lValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        double fValue = rs.getFloat(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BFloat(fValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.FLOAT_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setFloatField(++doubleRegIndex, fValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.DOUBLE:
                        double dValue = rs.getDouble(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BFloat(dValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.FLOAT_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setFloatField(++doubleRegIndex, dValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
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
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.FLOAT_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setFloatField(++doubleRegIndex, decimalValue);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        boolean boolValue = rs.getBoolean(index);
                        isOriginalValueNull = rs.wasNull();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            BRefType refValue = isOriginalValueNull ? null : new BBoolean(boolValue);
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex, refValue,
                                    TypeTags.BOOLEAN_TAG);
                        } else {
                            if (!isOriginalValueNull) {
                                bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                            } else {
                                handleNilToNonNilableFieldAssignment();
                            }
                        }
                        break;
                    case Types.STRUCT:
                        Struct structdata = (Struct) rs.getObject(index);
                        BType structFieldType = this.type.getStructFields()[index - 1].getFieldType();
                        if (fieldTypeTag == TypeTags.UNION_TAG) {
                            handleUnionTypeStructField(bStruct, structFields, index, ++refRegIndex,
                                    createUserDefinedType(structdata, (BStructType) structFieldType),
                                    TypeTags.STRUCT_TAG);
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
                    "error in retrieving next value for column: " + columnName + ": at index:" + index + ":" + e
                            .getMessage());
        }
        return bStruct;
    }

    private void handleUnionTypeStructField(BStruct bStruct, BStructType.StructField[] structFields, int index,
            int refRegIndex, BRefType value, int refType) {
        if (isNillableType(structFields, index - 1, refType)) {
            bStruct.setRefField(refRegIndex, value);
        } else {
            throw new BallerinaException("Union type contains more than 2 members or the members do not result in "
                    + "an assignable nillable type");
        }
    }

    private void handleNilToNonNilableFieldAssignment() {
        throw new BallerinaException("Trying to assign a Nil value to a non-nillable field");
    }

    private void handleMismatchingFieldAssignment() {
        throw new BallerinaException("Trying to assign to a mismatching field");
    }

    private boolean isNillableType(BStructType.StructField[] structFields, int index, int typeTag) {
        List<BType> memberTypes = ((BUnionType) structFields[index].getFieldType())
                .getMemberTypes();
        if (memberTypes.size() != 2) {
            return false;
        }
        boolean containsType = false;
        boolean containsNil = false;
        for (BType memberType : memberTypes) {
            if (memberType.getTag() == TypeTags.UNION_TAG) {
                return false;
            }
            if (memberType.getTag() == typeTag) {
                containsType = true;
            }
            if (memberType.getTag() == TypeTags.NULL_TAG) {
                containsNil = true;
            }
        }
        return containsType && containsNil;
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
