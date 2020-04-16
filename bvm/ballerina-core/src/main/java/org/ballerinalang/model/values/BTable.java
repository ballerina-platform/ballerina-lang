/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.Map;

/**
 * The {@code BTable} represents a two dimensional data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BTable implements BRefType<Object> {

    private String tableName;
    private BType constraintType;
    private BType type;

    public BTable() {
        this.tableName = null;
        this.type = BTypes.typeTable;
    }

    public BTable(String tableName, BType constraintType) {
        this(constraintType);
        this.tableName = tableName;
    }

    public BTable(BType constraintType) {
        this.constraintType = constraintType;
        this.type = new BTableType(constraintType);
    }

    @Override
    public String stringValue() {
        return "";
    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }

    public BType getConstraintType() {
        return constraintType;
    }
}
