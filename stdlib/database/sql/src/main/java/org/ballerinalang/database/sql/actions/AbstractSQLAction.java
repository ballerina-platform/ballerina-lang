/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.database.sql.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;

/**
 * {@code AbstractSQLAction} is the base class for all SQL remote functions.
 *
 * @since 0.8.0
 */
public abstract class AbstractSQLAction implements NativeCallableUnit {

    public boolean isBlocking() {
        return false;
    }

    protected BStructureType getStructType(Context context, int index) {
        BStructureType structType = null;
        BTypeDescValue type = (BTypeDescValue) context.getNullableRefArgument(index);
        if (type != null) {
            structType = (BStructureType) type.value();
        }
        return structType;
    }

    protected SQLDatasource retrieveDatasource(Context context) {
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        return (SQLDatasource) bConnector.getNativeData(Constants.SQL_CLIENT);
    }
}
