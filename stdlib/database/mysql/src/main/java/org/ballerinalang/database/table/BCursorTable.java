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
package org.ballerinalang.database.table;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.util.TableUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Represents a cursor based table which is returned from a database as result of a query.
 *
 * @since 0.970.2
 */
public class BCursorTable extends BTable {

    private boolean loadSQLTableToMemory;

    public BCursorTable(DataIterator dataIterator, boolean loadSQLTableToMemory) {
        super();
        this.iterator = dataIterator;
        this.loadSQLTableToMemory = loadSQLTableToMemory;
    }

    public void reset(boolean isInTransaction) {
        if (loadSQLTableToMemory) {
            iterator.reset(false);
        } else {
            if (iterator != null) {
                iterator.reset(isInTransaction);
            }
        }
        resetIterationHelperAttributes();
    }

    public String stringValue() {
        return "";
    }

    public void addData(BStruct data, Context context) {
        throw new BallerinaException("data cannot be added to a table returned from a database");
    }

    public void performRemoveOperation(Context context, BFunctionPointer lambdaFunction) {
        context.setReturnValues(TableUtils.createTableOperationError(context,
                new BallerinaException("data cannot be deleted from a table returned from a database")));
    }

    protected boolean isIteratorGenerationConditionMet() {
        return false;
    }
}
