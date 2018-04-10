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
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlobArray;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Includes utility methods required for table related operations.
 *
 * @since 0.970.0
 */
public class TableUtils {
    private static final String TABLE_OPERATION_ERROR = "TableOperationError";
    private static final String TABLE_PACKAGE_PATH = "ballerina.builtin";
    private static final String EXCEPTION_OCCURRED = "Exception occurred";

    public static String generateInsertDataStatment(String tableName, BStruct constrainedType) {
        StringBuilder sbSql = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        sbSql.append(TableConstants.SQL_INSERT_INTO).append(tableName).append(" (");
        BStructType.StructField[] structFields = constrainedType.getType().getStructFields();
        String sep = "";
        for (BStructType.StructField sf : structFields) {
            String name = sf.getFieldName();
            sbSql.append(sep).append(name).append(" ");
            sbValues.append(sep).append("?");
            sep = ",";
        }
        sbSql.append(") values (").append(sbValues).append(")");
        return sbSql.toString();
    }

    public static String generateDeleteDataStatment(String tableName, BStruct constrainedType) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append(TableConstants.SQL_DELETE_FROM).append(tableName).append(TableConstants.SQL_WHERE);
        BStructType.StructField[] structFields = constrainedType.getType().getStructFields();
        String sep = "";
        for (BStructType.StructField sf : structFields) {
            String name = sf.getFieldName();
            sbSql.append(sep).append(name).append(" = ? ");
            sep = TableConstants.SQL_AND;
        }
        return sbSql.toString();
    }

    public static void prepareAndExecuteStatement(PreparedStatement stmt, BStruct constrainedType) {
        try {
            BStructType.StructField[] structFields = constrainedType.getType().getStructFields();
            int intFieldIndex = 0;
            int floatFieldIndex = 0;
            int stringFieldIndex = 0;
            int booleanFieldIndex = 0;
            int refFieldIndex = 0;
            int blobFieldIndex = 0;
            int index = 1;
            for (BStructType.StructField sf : structFields) {
                int type = sf.getFieldType().getTag();
                switch (type) {
                case TypeTags.INT_TAG:
                    stmt.setLong(index, constrainedType.getIntField(intFieldIndex));
                    ++intFieldIndex;
                    break;
                case TypeTags.STRING_TAG:
                    stmt.setString(index, constrainedType.getStringField(stringFieldIndex));
                    ++stringFieldIndex;
                    break;
                case TypeTags.FLOAT_TAG:
                    stmt.setDouble(index, constrainedType.getFloatField(floatFieldIndex));
                    ++floatFieldIndex;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    stmt.setBoolean(index, constrainedType.getBooleanField(booleanFieldIndex) == 1);
                    ++booleanFieldIndex;
                    break;
                case TypeTags.XML_TAG:
                case TypeTags.JSON_TAG:
                    stmt.setString(index, constrainedType.getRefField(refFieldIndex).toString());
                    ++refFieldIndex;
                    break;
                case TypeTags.BLOB_TAG:
                    byte[] blobData = constrainedType.getBlobField(blobFieldIndex);
                    stmt.setBlob(index, new ByteArrayInputStream(blobData), blobData.length);
                    ++blobFieldIndex;
                    break;
                case TypeTags.ARRAY_TAG:
                    Object[] arrayData = getArrayData(constrainedType.getRefField(refFieldIndex));
                    stmt.setObject(index, arrayData);
                    ++refFieldIndex;
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
            arrayLength = (int) ((BIntArray) value).size();
            arrayData = new Long[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BIntArray) value).get(i);
            }
            break;
        case TypeTags.FLOAT_TAG:
            arrayLength = (int) ((BFloatArray) value).size();
            arrayData = new Double[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BFloatArray) value).get(i);
            }
            break;
        case TypeTags.STRING_TAG:
            arrayLength = (int) ((BStringArray) value).size();
            arrayData = new String[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BStringArray) value).get(i);
            }
            break;
        case TypeTags.BOOLEAN_TAG:
            arrayLength = (int) ((BBooleanArray) value).size();
            arrayData = new Boolean[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BBooleanArray) value).get(i) > 0;
            }
            break;
        case TypeTags.BLOB_TAG:
            arrayLength = (int) ((BBlobArray) value).size();
            arrayData = new Blob[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((BBlobArray) value).get(i);
            }
            break;
        default:
            throw new BallerinaException("unsupported data type for array parameter");
        }
        return arrayData;
    }

    /**
     * Creates an instance of {@code {@link BStruct}} of the type TableOperationError.
     *
     * @param context The context
     * @param throwable The Throwable object to be used
     * @return {@code {@link BStruct}} of the type {@code TableOperationError}
     */
    public static BStruct createTableOperationError(Context context, Throwable throwable) {
        PackageInfo tableLibPackage = context.getProgramFile()
                .getPackageInfo(TABLE_PACKAGE_PATH);
        StructInfo errorStructInfo = tableLibPackage.getStructInfo(TABLE_OPERATION_ERROR);
        BStruct tableOperationError = new BStruct(errorStructInfo.getType());
        if (throwable.getMessage() == null) {
            tableOperationError.setStringField(0, EXCEPTION_OCCURRED);
        } else {
            tableOperationError.setStringField(0, throwable.getMessage());
        }
        return tableOperationError;
    }

}
