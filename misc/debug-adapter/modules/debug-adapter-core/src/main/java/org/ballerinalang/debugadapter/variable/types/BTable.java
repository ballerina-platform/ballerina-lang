/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPENAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina table variable type.
 */
public class BTable extends BSimpleVariable {

    private static final String FIELD_CONSTRAINT = "constraint";

    public BTable(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.TABLE, value);
    }

    @Override
    public String computeValue() {
        try {
            Optional<Value> type = getFieldValue(jvmValue, FIELD_TYPE);
            if (!type.isPresent()) {
                return UNKNOWN_VALUE;
            }
            Optional<Value> constraint = getFieldValue(type.get(), FIELD_CONSTRAINT);
            if (!constraint.isPresent()) {
                return UNKNOWN_VALUE;
            }
            Optional<Value> constraintTypeName = getFieldValue(constraint.get(), FIELD_TYPENAME);
            if (!constraintTypeName.isPresent()) {
                return UNKNOWN_VALUE;
            }
            return String.format("table<%s>", getStringFrom(constraintTypeName.get()));
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }
}
