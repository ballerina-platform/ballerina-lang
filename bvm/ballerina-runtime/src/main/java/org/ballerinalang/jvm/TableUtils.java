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
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

/**
 * Includes utility methods required for table related operations.
 *
 * @since 0.995.0
 */
public class TableUtils {

    private static final String DEFAULT_ERROR_DETAIL_MESSAGE = "Error occurred during table manipulation";

    public static String generateInsertDataStatment(String tableName, MapValue<?, ?> constrainedType) {
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

    public static String generateDeleteDataStatment(String tableName, MapValue<?, ?> constrainedType) {
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

    public static void prepareAndExecuteStatement(PreparedStatement stmt, MapValue<?, ?> data) {
        try {
            Collection<BField> structFields = ((BStructureType) data.getType()).getFields().values();
            int index = 1;
            for (BField sf : structFields) {
                int type = sf.getFieldType().getTag();
                String fieldName = sf.getFieldName();
                switch (type) {
                    case TypeTags.INT_TAG:
                        stmt.setLong(index, data.getIntValue(fieldName));
                        break;
                    case TypeTags.STRING_TAG:
                        stmt.setString(index, data.getStringValue(fieldName));
                        break;
                    case TypeTags.FLOAT_TAG:
                        stmt.setDouble(index,  data.getFloatValue(fieldName));
                        break;
                    case TypeTags.DECIMAL_TAG:
                        stmt.setDouble(index,  (Double) data.get(fieldName));
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        stmt.setBoolean(index, data.getBooleanValue(fieldName));
                        break;
                    case TypeTags.XML_TAG:
                    case TypeTags.JSON_TAG:
                        stmt.setString(index, data.get(fieldName).toString());
                        break;
                    case TypeTags.ARRAY_TAG:
                        boolean isBlobType =
                                ((BArrayType) sf.getFieldType()).getElementType().getTag() == TypeTags.BYTE_TAG;
                        if (isBlobType) {
                            Object value = data.get(fieldName);
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
                ++index;
            }
            stmt.execute();
        } catch (SQLException e) {
            throw new BallerinaException("execute update failed: " + e.getMessage(), e);
        }
    }

    static Object[] getArrayData(ArrayValue value) {
        if (value == null) {
            return new Object[] {null};
        }
        int typeTag = value.elementType.getTag();
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
                    arrayData[i] = value.getRefValue(i);
                }
                break;
            default:
                throw new BallerinaException("unsupported data type for array parameter");
        }
        return arrayData;
    }

    public static ErrorValue createTableOperationError(Throwable throwable) {
        String detail = throwable.getMessage() != null ? throwable.getMessage() : DEFAULT_ERROR_DETAIL_MESSAGE;
        return BallerinaErrors
                .createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR, detail);
    }
}
