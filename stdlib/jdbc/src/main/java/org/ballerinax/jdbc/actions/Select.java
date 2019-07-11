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
package org.ballerinax.jdbc.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.SQLDatasource;
import org.ballerinax.jdbc.statement.SQLStatement;
import org.ballerinax.jdbc.statement.SelectStatement;

/**
 * {@code Select} is the Select remote function implementation of the JDBC client.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        orgName = "ballerinax", packageName = "jdbc",
        functionName = "nativeSelect"
)
public class Select extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        //TODO: #16033
    }

    public static Object nativeSelect(Strand strand, ObjectValue client, String query, Object recordType,
            ArrayValue parameters) {
        //TODO: JBalMigration: once default params are supported fix this
        ////TODO: #16033
        boolean loadSQLTableToMemory = false;
        SQLDatasource sqlDatasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        //        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        //        return (String) bConnector.getNativeData(Constants.CONNECTOR_ID_KEY);
        SQLStatement selectStatement = new SelectStatement(client, sqlDatasource, query, parameters,
                (TypedescValue) recordType, loadSQLTableToMemory);
        return selectStatement.execute();
    }
}
