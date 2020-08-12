/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BTypedesc;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import static org.ballerinalang.sql.utils.Utils.convert;
import static org.ballerinalang.sql.utils.Utils.getString;

/**
 * Util class to process InOut/Out parameters of procedure calls.
 */
public class OutParameterUtils {

    public static Object get(ObjectValue result, BTypedesc typeDesc) {
        int sqlType = (int) result.getNativeData(Constants.ParameterObject.SQL_TYPE_NATIVE_DATA);
        Object value = result.getNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA);

        BType ballerinaType = typeDesc.getDescribingType();
        try {
            switch (sqlType) {
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                    return convert((String) value, sqlType, ballerinaType);
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    if (ballerinaType.getTag() == TypeTags.STRING_TAG) {
                        return convert((String) value, sqlType, ballerinaType);
                    } else {
                        return convert(((String) value).getBytes(Charset.defaultCharset()), sqlType, ballerinaType,
                                JDBCType.valueOf(sqlType).getName());
                    }
                case Types.ARRAY:
                    return convert((Array) value, sqlType, ballerinaType);
                case Types.BLOB:
                    return convert((Blob) value, sqlType, ballerinaType);
                case Types.CLOB:
                    String clobValue = getString((Clob) value);
                    return convert(clobValue, sqlType, ballerinaType);
                case Types.NCLOB:
                    String nClobValue = getString((NClob) value);
                    return convert(nClobValue, sqlType, ballerinaType);
                case Types.DATE:
                    return convert((Date) value, sqlType, ballerinaType);
                case Types.TIME:
                case Types.TIME_WITH_TIMEZONE:
                    return convert((Time) value, sqlType, ballerinaType);
                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    return convert((Timestamp) value, sqlType, ballerinaType);
                case Types.ROWID:
                    return convert(((RowId) value).getBytes(), sqlType, ballerinaType, "SQL RowID");
                case Types.TINYINT:
                case Types.SMALLINT:
                    if (value == null) {
                        return null;
                    }
                    return convert((int) value, sqlType, ballerinaType, false);
                case Types.INTEGER:
                case Types.BIGINT:
                    if (value == null) {
                        return null;
                    }
                    return convert((long) value, sqlType, ballerinaType, false);
                case Types.REAL:
                case Types.FLOAT:
                    if (value == null) {
                        return null;
                    }
                    return convert((float) value, sqlType, ballerinaType, false);
                case Types.DOUBLE:
                    if (value == null) {
                        return null;
                    }
                    return convert((double) value, sqlType, ballerinaType, false);
                case Types.NUMERIC:
                case Types.DECIMAL:
                    if (value == null) {
                        return null;
                    }
                    return convert((BigDecimal) value, sqlType, ballerinaType, false);
                case Types.BIT:
                case Types.BOOLEAN:
                    if (value == null) {
                        return null;
                    }
                    return convert((boolean) value, sqlType, ballerinaType, false);
                case Types.REF:
                case Types.STRUCT:
                    return convert((Struct) value, sqlType, ballerinaType);
                case Types.SQLXML:
                    return convert((SQLXML) value, sqlType, ballerinaType);
                default:
                    // Cannot reach to Default
                    return ErrorGenerator.getSQLApplicationError("Unsupported SQL type " + sqlType);
            }
        } catch (ApplicationError | IOException applicationError) {
            return ErrorGenerator.getSQLApplicationError(applicationError.getMessage());
        } catch (SQLException sqlException) {
            return ErrorGenerator.getSQLDatabaseError(sqlException, "Error when parsing out parameter.");
        }
    }
}
