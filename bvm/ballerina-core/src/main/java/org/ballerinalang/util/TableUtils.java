/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

/**
 * Includes utility methods required for table related operations.
 *
 * @since 0.970.0
 */
public class TableUtils {
    private static final String DEFAULT_ERROR_DETAIL_MESSAGE = "Error occurred during table manipulation";

    public static String generateInsertDataStatment(String tableName, BMap<?, ?> constrainedType) {
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

    public static String generateDeleteDataStatment(String tableName, BMap<?, ?> constrainedType) {
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

    public static void prepareAndExecuteStatement(PreparedStatement stmt, BMap<String, BValue> constrainedType) {
        try {
            Collection<BField> structFields = ((BStructureType) constrainedType.getType()).getFields().values();
            int index = 1;
            for (BField sf : structFields) {
                int type = sf.getFieldType().getTag();
                String fieldName = sf.fieldName;
                switch (type) {
                case TypeTags.INT_TAG:
                    stmt.setLong(index, ((BInteger) constrainedType.get(fieldName)).intValue());
                    break;
                case TypeTags.STRING_TAG:
                    stmt.setString(index, constrainedType.get(fieldName).stringValue());
                    break;
                case TypeTags.FLOAT_TAG:
                    stmt.setDouble(index, ((BFloat) constrainedType.get(fieldName)).floatValue());
                    break;
                case TypeTags.DECIMAL_TAG:
                    stmt.setDouble(index, ((BDecimal) constrainedType.get(fieldName)).decimalValue().doubleValue());
                    break;
                case TypeTags.BOOLEAN_TAG:
                    stmt.setBoolean(index, ((BBoolean) constrainedType.get(fieldName)).booleanValue());
                    break;
                case TypeTags.XML_TAG:
                case TypeTags.JSON_TAG:
                    stmt.setString(index, constrainedType.get(fieldName).toString());
                    break;
                case TypeTags.ARRAY_TAG:
                    boolean isBlobType =
                            ((BArrayType) sf.getFieldType()).getElementType().getTag() == TypeTags.BYTE_TAG;
                    if (isBlobType) {
                        BValue value = constrainedType.get(fieldName);
                        if (value != null) {
                            byte[] blobData = ((BValueArray) constrainedType.get(fieldName)).getBytes();
                            stmt.setBlob(index, new ByteArrayInputStream(blobData), blobData.length);
                        } else {
                            stmt.setNull(index, Types.BLOB);
                        }
                    } else {
                        Object[] arrayData = getArrayData(constrainedType.get(fieldName));
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

    static Object[] getArrayData(BValue value) {
        if (value == null) {
            return new Object[] {null};
        }
        int typeTag = ((BArrayType) value.getType()).getElementType().getTag();
        Object[] arrayData;
        int arrayLength;
        switch (typeTag) {
        case TypeTags.INT_TAG:
            arrayLength = (int) ((BValueArray) value).size();
            arrayData = new Long[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BValueArray) value).getInt(i);
            }
            break;
        case TypeTags.FLOAT_TAG:
            arrayLength = (int) ((BValueArray) value).size();
            arrayData = new Double[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BValueArray) value).getFloat(i);
            }
            break;
        case TypeTags.STRING_TAG:
            arrayLength = (int) ((BValueArray) value).size();
            arrayData = new String[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BValueArray) value).getString(i);
            }
            break;
        case TypeTags.BOOLEAN_TAG:
            arrayLength = (int) ((BValueArray) value).size();
            arrayData = new Boolean[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BValueArray) value).getBoolean(i) > 0;
            }
            break;
        case TypeTags.DECIMAL_TAG:
            arrayLength = (int) ((BValueArray) value).size();
            arrayData = new BigDecimal[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BValueArray) value).getRefValue(i).value();
            }
            break;
        default:
            throw new BallerinaException("unsupported data type for array parameter");
        }
        return arrayData;
    }

    /**
     * Creates an instance of {@code {@link BError}} representing an error.
     *
     * @param context The context
     * @param throwable The Throwable object to be used
     * @return error value
     */
    public static BError createTableOperationError(Context context, Throwable throwable) {
        String detail = throwable.getMessage() != null ? throwable.getMessage() : DEFAULT_ERROR_DETAIL_MESSAGE;
        BMap<String, BValue> tableErrorDetail = new BMap<>();
        tableErrorDetail.put("message", new BString(detail));
        return BLangVMErrors.createError(context, true, BTypes.typeError,
                                         BallerinaErrorReasons.TABLE_OPERATION_ERROR, tableErrorDetail);
    }
}
