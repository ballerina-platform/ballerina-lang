/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.database.sql.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * {@code GetProxyTable} creates a proxy for a SQL database table.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "sql",
        functionName = "getProxyTable",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.CALLER_ACTIONS),
        args = {
                @Argument(name = "tableName", type = TypeKind.STRING),
                @Argument(name = "recordType", type = TypeKind.TYPEDESC)
        },
        returnType = {
                @ReturnType(type = TypeKind.TABLE),
                @ReturnType(type = TypeKind.RECORD, structType = "error", structPackage = BALLERINA_BUILTIN_PKG)
        }
)
public class GetProxyTable extends AbstractSQLAction {

    @Override
    public void execute(Context context) {

        String tableName = context.getStringArgument(0);
        BStructureType structType = getStructType(context, 1);
        SQLDatasource datasource = retrieveDatasource(context);
        try {
            checkAndObserveSQLAction(context, datasource, tableName);
            createProxyTable(context, datasource, tableName, structType);
        } catch (Throwable e) {
            checkAndObserveSQLError(context, e.getMessage());
        }
    }
}
