/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.api.definition.partition;

import org.wso2.siddhi.query.api.expression.Variable;

public class VariablePartitionType implements PartitionType {
    private Variable variable;

    public VariablePartitionType(Variable variable) {
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return "VariablePartitionType{" +
               "variable=" + variable +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VariablePartitionType that = (VariablePartitionType) o;

        if (variable != null ? !variable.equals(that.variable) : that.variable != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return variable != null ? variable.hashCode() : 0;
    }
}
