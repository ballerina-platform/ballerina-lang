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

package org.ballerinalang.nativeimpl.builtin.tablelib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.sql.BMirrorTable;
import org.ballerinalang.nativeimpl.sql.SQLDatasourceUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.TableUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@code Remove} is the function to remove data from a table.
 *
 * @since 0.963.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "builtin",
                   functionName = "table.remove",
                   args = {
                           @Argument(name = "dt", type = TypeKind.TABLE),
                           @Argument(name = "func", type = TypeKind.ANY)
                   })
public class Remove extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BTable table = (BTable) context.getRefArgument(0);
        BFunctionPointer lambDaFunction = (BFunctionPointer) context.getRefArgument(1);
        if (table instanceof BMirrorTable) {
            removeDataFromMirroredTable((BMirrorTable) table, context, lambDaFunction);
        } else {
            removeDataFromInMemoryTable(table, context, lambDaFunction);
        }
    }

    private void removeDataFromInMemoryTable(BTable table, Context context, BFunctionPointer lambdaFunction) {
        int deletedCount = 0;
        while (table.hasNext(false)) {
            BStruct data = table.getNext();
            BValue[] args = { data };
            BValue[] returns = BLangFunctions
                    .invokeCallable(lambdaFunction.value().getFunctionInfo(), args);
            if (((BBoolean) returns[0]).booleanValue()) {
                ++deletedCount;
                table.removeData(data);
            }
        }
        context.setReturnValues(new BInteger(deletedCount));
    }

    private void removeDataFromMirroredTable(BMirrorTable table, Context context, BFunctionPointer lambdaFunction) {
        int deletedCount = 0;
        Connection connection = null;
        boolean isInTransaction = context.isInTransaction();
        try {
            connection = SQLDatasourceUtils
                    .getDatabaseConnection(context, table.getDatasource(), isInTransaction);
            if (!isInTransaction) {
                connection.setAutoCommit(false);
            }
            while (table.hasNext(false)) {
                BStruct data = table.getNext();
                BValue[] args = { data };
                BValue[] returns = BLangFunctions
                        .invokeCallable(lambdaFunction.value().getFunctionInfo(),
                                args);
                if (((BBoolean) returns[0]).booleanValue()) {
                    ++deletedCount;
                    table.removeData(data, connection);
                }
            }
            if (!isInTransaction) {
                connection.commit();
            }
            context.setReturnValues(new BInteger(deletedCount));
        } catch (SQLException e) {
            context.setReturnValues(TableUtils.createTableOperationError(context, e));
            SQLDatasourceUtils.handleErrorOnTransaction(context);
            if (!isInTransaction) {
                releaseConnection(connection);
            }
        }
    }

    private void releaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new BallerinaException("Error occurred while releasing the connection");
        }
    }
}
