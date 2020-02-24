/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jdbc.statement;

import org.ballerinalang.jdbc.Constants;
import org.ballerinalang.jvm.values.MapValue;

import static org.ballerinalang.jdbc.Constants.PARAMETER_DIRECTION_FIELD;
import static org.ballerinalang.jdbc.Constants.PARAMETER_SQL_TYPE_FIELD;

/**
 * Utility methods for processing statements.
 *
 * @since 1.0.0
 */
public class StatementProcessUtils {

    private StatementProcessUtils() {

    }

    static int getParameterDirection(MapValue<String, Object> parameter) {
        int direction = 0;
        String directionInput = parameter.getStringValue(PARAMETER_DIRECTION_FIELD);
        if (directionInput != null) {
            switch (directionInput) {
            case Constants.QueryParamDirection.DIR_OUT:
                direction = Constants.QueryParamDirection.OUT;
                break;
            case Constants.QueryParamDirection.DIR_INOUT:
                direction = Constants.QueryParamDirection.INOUT;
                break;
            default:
                direction = Constants.QueryParamDirection.IN;
                break;
            }
        }
        return direction;
    }

    static String getSQLType(MapValue<String, Object> parameter) {
        String sqlType = "";
        Object sqlTypeValue = parameter.get(PARAMETER_SQL_TYPE_FIELD);
        if (sqlTypeValue != null) {
            sqlType = (String) sqlTypeValue;
        }
        return sqlType;
    }
}
