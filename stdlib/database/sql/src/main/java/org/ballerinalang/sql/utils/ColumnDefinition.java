/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.types.BType;

/**
 * This class provides the mapping of the sql columns, its names and types.
 *
 * @since 1.2.0
 */
public class ColumnDefinition {
    private String sqlName;
    private String ballerinaFieldName;
    private int sqlType;
    private BType ballerinaType;
    private boolean isNullable;


    ColumnDefinition(String sqlName, String ballerinaFieldName, int sqlType, BType ballerinaType, boolean isNullable) {
        this.sqlName = sqlName;
        if (ballerinaFieldName != null && !ballerinaFieldName.isEmpty()) {
            this.ballerinaFieldName = ballerinaFieldName;
        } else {
            this.ballerinaFieldName = this.sqlName;
        }
        this.sqlType = sqlType;
        this.ballerinaType = ballerinaType;
        this.isNullable = isNullable;
    }

    public String getSqlName() {
        return sqlName;
    }

    public int getSqlType() {
        return sqlType;
    }

    BType getBallerinaType() {
        return ballerinaType;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getBallerinaFieldName() {
        return ballerinaFieldName;
    }
}
