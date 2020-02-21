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
package org.ballerinax.jdbc.table;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.DataIterator;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TableValue;

import java.util.Map;

/**
 * Represents a cursor based table which is returned from a database as result of a query.
 *
 * @since 0.970.2
 */
public class BCursorTable extends TableValue {

    public BCursorTable(DataIterator dataIterator, BStructureType constraintType) {
        super(constraintType);
        this.iterator = dataIterator;
    }

    public void reset() {
        iterator.reset();
        resetIterationHelperAttributes();
    }

    public String stringValue() {
        return "";
    }

    public int length() {
        throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                "the row count of a table returned from a database cannot be provided");
    }

    protected boolean isIteratorGenerationConditionMet() {
        return false;
    }

    @Override
    public boolean isInMemoryTable() {
        return false;
    }

    public Object performAddOperation(MapValueImpl<String, Object> data) {
        throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                "data cannot be added to a table returned from a database");
    }

    @Override
    public Object performRemoveOperation(Strand strand, FPValue<Object, Boolean> func) {
        throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                "data cannot be deleted from a table returned from a database");
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                "a table returned from a database can not be cloned");
    }
}
