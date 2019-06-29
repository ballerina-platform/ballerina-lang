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
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.statement.SQLStatement;
import org.ballerinalang.database.sql.statement.SelectStatement;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * {@code Select} is the Select remote function implementation of the SQL Connector.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "sql",
        functionName = "nativeSelect",
        args = {
                @Argument(name = "sqlQuery", type = TypeKind.STRING),
                @Argument(name = "recordType", type = TypeKind.TYPEDESC),
                //@Argument(name = "loadToMemory", type = TypeKind.BOOLEAN),
                @Argument(name = "parameters", type = TypeKind.ARRAY, elementType = TypeKind.UNION,
                          structType = "Param")
        },
        returnType = {
                @ReturnType(type = TypeKind.TABLE),
                @ReturnType(type = TypeKind.RECORD, structType = "JdbcClientError",
                        structPackage = BALLERINA_BUILTIN_PKG)
        }
)
public class Select extends AbstractSQLAction {

    @Override
    public void execute(Context context) {
        //TODO: #16033
        /*String query = context.getStringArgument(0);
        BStructureType structType = getStructType(context, 1);
        boolean loadSQLTableToMemory = context.getBooleanArgument(0);

        BValueArray parameters = (BValueArray) context.getNullableRefArgument(2);
        SQLDatasource datasource = retrieveDatasource(context);

        SQLStatement selectStatement = new SelectStatement(context, datasource, query, parameters, structType,
                loadSQLTableToMemory);
        selectStatement.execute();*/
    }

    public static Object nativeSelect(Strand strand, ObjectValue client, String query, Object recordType,
            ArrayValue parameters) {
        //TODO: JBalMigration: once default params are supported fix this
        ////TODO: #16033
        Boolean loadSQLTableToMemory = false;
        SQLDatasource sqlDatasource = retrieveDatasource(client);
        //        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        //        return (String) bConnector.getNativeData(Constants.CONNECTOR_ID_KEY);
        SQLStatement selectStatement = new SelectStatement(client, sqlDatasource, query, parameters,
                (TypedescValue) recordType, loadSQLTableToMemory);
        return selectStatement.execute();
    }
}
