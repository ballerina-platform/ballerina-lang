package org.ballerinax.jdbc.table;

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.types.*;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BIterator;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinax.jdbc.datasource.SQLDatasourceUtils;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

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
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the SQL Stream Iterator.
 */
public class StreamSqlIterator implements BIterator {
    private ResultSet resultSet;
    private List<ColumnDefinition> columnDefinitions;
    private BRecordType bRecordType;

    public StreamSqlIterator(ResultSet resultSet, List<ColumnDefinition> columnDefinitions, BRecordType bRecordType) {
        this.resultSet = resultSet;
        this.columnDefinitions = columnDefinitions;
        this.bRecordType = bRecordType;
    }

    @Override
    public boolean hasNext() {
        try {
            return this.resultSet.next();
        } catch (SQLException e) {
            //TODO: Handle the exception properly.
            return false;
        }
    }

    @Override
    public Object next() {
        boolean hasNext;
        try {
            hasNext = this.resultSet.next();
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLApplicationError("SQL exection when checking the next " +
                    "available data" + e.getMessage());
        }
        if (hasNext) {
//            org.ballerinalang.jvm.types.BRecordType recordType = BType;
//            BRecordType bvmRecordType = new BRecordType(recordType.getName(),
//                    recordType.getPackage().getName(), recordType.flags);
//            Map<String, BField> recordFields =
//                    recordType.getFields().entrySet().stream()
//                            .filter(entry -> !selfTypeStack.contains(entry.getValue()))
//                            .peek(entry -> selfTypeStack.push(entry.getValue()))
//                            .collect(Collectors.toMap(Map.Entry::getKey,
//                                    entry -> new BField(
//                                            getBVMType(entry.getValue().type, selfTypeStack),
//                                            entry.getValue().getFieldName(), entry.getValue().flags),
//                                    (a, b) -> b, LinkedHashMap::new));
//            bvmRecordType.setFields(recordFields);
//            return bvmRecordType;
            MapValue<String, Object> bStruct = new MapValueImpl<>(this.bRecordType);
            int index = 0;
            String columnName = null;
            int sqlType = -1;
            try {
//            BField[] structFields = this.type.getFields().values().toArray(new BField[0]);
//            if (columnDefs.size() != structFields.length) {
//                throw ErrorGenerator.getSQLApplicationError("number of fields in the constraint type is " + (
//                        structFields.length > columnDefs.size() ?
//                                "greater" : "lower") + " than column count of the result set");
//            }
                for (ColumnDefinition columnDef : this.columnDefinitions) {
                    if (columnDef instanceof SQLDataIterator.SQLColumnDefinition) {
                        SQLDataIterator.SQLColumnDefinition def = (SQLDataIterator.SQLColumnDefinition) columnDef;
                        columnName = def.getName();
                        sqlType = def.getSqlType();
                        ++index;
//                    BField field = structFields[index - 1];
//                    BType fieldType = field.getFieldType();
                        String fieldName = columnName;
                        switch (sqlType) {
                            case Types.ARRAY:
                                Array data = this.resultSet.getArray(index);
                                bStruct.put(fieldName, data.toString());
                                break;
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.LONGVARCHAR:
                            case Types.NCHAR:
                            case Types.NVARCHAR:
                            case Types.LONGNVARCHAR:
                                String sValue = this.resultSet.getString(index);
                                bStruct.put(fieldName, sValue);
                                break;
                            case Types.BINARY:
                            case Types.VARBINARY:
                            case Types.LONGVARBINARY:
                                byte[] binaryValue = this.resultSet.getBytes(index);
                                bStruct.put(fieldName, binaryValue);
                                break;
                            case Types.BLOB:
                                Blob blobValue = this.resultSet.getBlob(index);
                                if (blobValue != null) {
                                    bStruct.put(fieldName, blobValue.toString());
                                } else {
                                    bStruct.put(fieldName, null);
                                }
                                break;
                            case Types.CLOB:
                                String clobValue = SQLDatasourceUtils.getString((this.resultSet.getClob(index)));
                                bStruct.put(fieldName, clobValue);
                                break;
                            case Types.NCLOB:
                                String nClobValue = SQLDatasourceUtils.getString((this.resultSet.getNClob(index)));
                                bStruct.put(fieldName, nClobValue);
                                break;
                            case Types.DATE:
                                Date date = this.resultSet.getDate(index);
                                bStruct.put(fieldName, date.toString());
                                break;
                            case Types.TIME:
                            case Types.TIME_WITH_TIMEZONE:
                                Time time = this.resultSet.getTime(index, Calendar.getInstance()); //TODO: check calendar.
                                bStruct.put(fieldName, time.toString());
                                break;
                            case Types.TIMESTAMP:
                            case Types.TIMESTAMP_WITH_TIMEZONE:
                                //TODO: check calendar.
                                Timestamp timestamp = resultSet.getTimestamp(index, Calendar.getInstance());
                                bStruct.put(fieldName, timestamp.toString());
                                break;
                            case Types.ROWID:
                                sValue = new String(this.resultSet.getRowId(index).getBytes(), StandardCharsets.UTF_8);
                                bStruct.put(fieldName, sValue);
                                break;
                            case Types.TINYINT:
                            case Types.SMALLINT:
                                long iValue = this.resultSet.getInt(index);
                                bStruct.put(fieldName, iValue);
                                break;
                            case Types.INTEGER:
                            case Types.BIGINT:
                                long lValue = this.resultSet.getLong(index);
                                bStruct.put(fieldName, lValue);
                                break;
                            case Types.REAL:
                            case Types.FLOAT:
                                double fValue = this.resultSet.getFloat(index);
                                bStruct.put(fieldName, fValue);
                                break;
                            case Types.DOUBLE:
                                double dValue = this.resultSet.getDouble(index);
                                bStruct.put(fieldName, dValue);
                                break;
                            case Types.NUMERIC:
                            case Types.DECIMAL:
                                BigDecimal bigDecimalValue = this.resultSet.getBigDecimal(index);
                                bStruct.put(fieldName, bigDecimalValue);
                                break;
                            case Types.BIT:
                            case Types.BOOLEAN:
                                boolean boolValue = this.resultSet.getBoolean(index);
                                bStruct.put(fieldName, boolValue);
                                break;
                            case Types.STRUCT:
                                Struct structData = (Struct) this.resultSet.getObject(index);
                                bStruct.put(fieldName, structData);
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
            }
            return bStruct;
        } else {
            return null;
        }
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BString bStringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return null;
    }
}
