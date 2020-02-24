/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jdbc.methods;

import org.ballerinalang.jdbc.statement.SQLStatement;
import org.ballerinalang.jdbc.statement.SelectStatementStream;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;

/**
 * External remote method implementations of the JDBC client.
 *
 * @since 1.1.0
 */
public class ExternActions {

    private ExternActions() {
    }

    public static StreamValue nativeQuery(ObjectValue client, String query,
                                           Object parameters, Object recordType) {
        SQLStatement selectStatement = new SelectStatementStream(Scheduler.getStrand());
        Object result = selectStatement.execute();
        if (result instanceof StreamValue) {
            return (StreamValue) result;
        }
        return null;
    }
}
