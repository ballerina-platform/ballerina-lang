/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

/**
 * Includes utility methods required for table related operations.
 *
 * @since 0.995.0
 */
public class TableUtils {

    private static final String DEFAULT_ERROR_DETAIL_MESSAGE = "Error occurred during table manipulation";

    public static String generateInsertDataStatement(String tableName, MapValueImpl<?, ?> constrainedType) {
        StringBuilder sbSql = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        sbSql.append(TableConstants.SQL_INSERT_INTO).append(tableName).append(" (");
        Collection<BField> structFields = ((BStructureType) constrainedType.getType()).getFields().values();
        String sep = "";
        for (BField sf : structFields) {
            String name = sf.getFieldName();
            sbSql.append(sep).append(name).append(" ");
            sbValues.append(sep).append("?");
            sep = ",";
        }
        sbSql.append(") values (").append(sbValues).append(")");
        return sbSql.toString();
    }

    public static String generateDeleteDataStatment(String tableName, MapValueImpl<?, ?> constrainedType) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append(TableConstants.SQL_DELETE_FROM).append(tableName).append(TableConstants.SQL_WHERE);
        Collection<BField> structFields = ((BStructureType) constrainedType.getType()).getFields().values();
        String sep = "";
        for (BField sf : structFields) {
            String name = sf.getFieldName();
            sbSql.append(sep).append(name).append(" = ? ");
            sep = TableConstants.SQL_AND;
        }
        return sbSql.toString();
    }

    public static void prepareAndExecuteStatement(PreparedStatement stmt, MapValueImpl<?, ?> data) {
        try {
            Collection<BField> structFields = ((BStructureType) data.getType()).getFields().values();
            int index = 1;
            for (BField sf : structFields) {
                int type = sf.getFieldType().getTag();
                String fieldName = sf.getFieldName();
                switch (type) {
                    case TypeTags.INT_TAG:
                    case TypeTags.STRING_TAG:
                    case TypeTags.FLOAT_TAG:
                    case TypeTags.DECIMAL_TAG:
                    case TypeTags.BOOLEAN_TAG:
                    case TypeTags.XML_TAG:
                    case TypeTags.JSON_TAG:
                    case TypeTags.ARRAY_TAG:
                        prepareAndExecuteStatement(stmt, data, index, sf, type, fieldName);
                        break;
                    case TypeTags.UNION_TAG:
                        List<BType> members = ((BUnionType) sf.getFieldType()).getMemberTypes();
                        if (members.size() != 2) {
                            throw createTableOperationError(
                                    "Corresponding Union type in the record is not an assignable nillable type");
                        }
                        if (members.get(0).getTag() == TypeTags.NULL_TAG) {
                            prepareAndExecuteStatement(stmt, data, index, sf, members.get(1).getTag(), fieldName);
                        } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
                            prepareAndExecuteStatement(stmt, data, index, sf, members.get(0).getTag(), fieldName);
                        } else {
                            throw createTableOperationError(
                                    "Corresponding Union type in the record is not an assignable nillable type");
                        }
                        break;
                }
                ++index;
            }
            stmt.execute();
        } catch (SQLException e) {
            throw createTableOperationError("execute update failed: " + e.getMessage());
        }
    }

    private static void prepareAndExecuteStatement(PreparedStatement stmt, MapValueImpl<?, ?> data, int index,
            BField sf, int type, String fieldName) throws SQLException {
        Object value = data.get(fieldName);
        switch (type) {
            case TypeTags.INT_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.BIGINT);
                } else {
                    stmt.setLong(index, data.getIntValue(fieldName));
                }
                break;
            case TypeTags.STRING_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.VARCHAR);
                } else {
                    stmt.setString(index, data.getStringValue(fieldName));
                }
                break;
            case TypeTags.FLOAT_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.DOUBLE);
                } else {
                    stmt.setDouble(index, data.getFloatValue(fieldName));
                }
                break;
            case TypeTags.DECIMAL_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.DECIMAL);
                } else {
                    stmt.setBigDecimal(index, ((DecimalValue) data.get(fieldName)).decimalValue());
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.BOOLEAN);
                } else {
                    stmt.setBoolean(index, data.getBooleanValue(fieldName));
                }
                break;
            case TypeTags.XML_TAG:
                stmt.setString(index, data.get(fieldName).toString());
                break;
            case TypeTags.JSON_TAG:
                if (value == null) {
                    stmt.setNull(index, Types.VARCHAR);
                } else {
                    stmt.setString(index, StringUtils.getJsonString(data.get(fieldName)));
                }
                break;
            case TypeTags.ARRAY_TAG:
                boolean isBlobType = ((BArrayType) sf.getFieldType()).getElementType().getTag() == TypeTags.BYTE_TAG;
                if (isBlobType) {
                    if (value != null) {
                        byte[] blobData = ((ArrayValue) data.get(fieldName)).getBytes();
                        stmt.setBlob(index, new ByteArrayInputStream(blobData), blobData.length);
                    } else {
                        stmt.setNull(index, Types.BLOB);
                    }
                } else {
                    Object[] arrayData = getArrayData((ArrayValue) data.get(fieldName));
                    stmt.setObject(index, arrayData);
                }
                break;
        }
    }

    static Object[] getArrayData(ArrayValue value) {
        if (value == null) {
            return new Object[] {null};
        }
        int typeTag = value.getElementType().getTag();
        Object[] arrayData;
        int arrayLength;
        switch (typeTag) {
            case TypeTags.INT_TAG:
                arrayLength = value.size();
                arrayData = new Long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = value.getInt(i);
                }
                break;
            case TypeTags.FLOAT_TAG:
                arrayLength = value.size();
                arrayData = new Double[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = value.getFloat(i);
                }
                break;
            case TypeTags.STRING_TAG:
                arrayLength = value.size();
                arrayData = new String[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = value.getString(i);
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                arrayLength = value.size();
                arrayData = new Boolean[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = value.getBoolean(i);
                }
                break;
            case TypeTags.DECIMAL_TAG:
                arrayLength = value.size();
                arrayData = new BigDecimal[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((DecimalValue) value.getRefValue(i)).value();
                }
                break;
            default:
                throw createTableOperationError("unsupported data type for array parameter");
        }
        return arrayData;
    }

    public static ErrorValue createTableOperationError(Throwable throwable, String errorSuffix) {
        String detail = throwable.getMessage() != null ?
                errorSuffix + ": " + throwable.getMessage() :
                DEFAULT_ERROR_DETAIL_MESSAGE;
        return BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR, detail);
    }

    public static ErrorValue createTableOperationError(Throwable throwable) {
        String detail = throwable.getMessage() != null ? throwable.getMessage() : DEFAULT_ERROR_DETAIL_MESSAGE;
        return BallerinaErrors
                .createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR, detail);
    }

    public static ErrorValue createTableOperationError(String detail) {
        return BallerinaErrors
                .createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR, detail);
    }
}
