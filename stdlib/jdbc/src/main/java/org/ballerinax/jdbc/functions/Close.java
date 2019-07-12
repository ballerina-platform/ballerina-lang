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
package org.ballerinax.jdbc.functions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.SQLDatasource;
import org.ballerinax.jdbc.SQLDatasourceUtils;

/**
 * {@code Close} is the Close function implementation of the JDBC client connection pool.
 *
 * @since 0.8.4
 */
@BallerinaFunction(
        orgName = "ballerinax", packageName = "jdbc",
        functionName = "close"
)
public class Close extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        //TODO: #16033
    }

    public static Object close(Strand strand, ObjectValue client) {
        SQLDatasource datasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        // When an exception is thrown during database endpoint init (eg: driver not present) stop operation
        // of the endpoint is automatically called. But at this point, datasource is null therefore to handle that
        // situation following null check is needed.
        if (datasource != null && !datasource.isGlobalDatasource()) {
            try {
                datasource.decrementClientCounterAndAttemptPoolShutdown();
            } catch (InterruptedException e) {
                return  SQLDatasourceUtils.getSQLApplicationError("Error while stopping the database client");
            }
        }
        return null;
    }
}
