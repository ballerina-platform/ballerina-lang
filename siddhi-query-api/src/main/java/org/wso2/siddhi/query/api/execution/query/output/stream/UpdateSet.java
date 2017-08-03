/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Updating UpdateSet Attribute for UpdateStream
 */
public class UpdateSet {

  private List<SetAttribute> setAttributeList = new ArrayList<>();

    public UpdateSet set(Variable tableVariable, Expression assignmentExpression) {
        setAttributeList.add(new SetAttribute(tableVariable, assignmentExpression));
        return this;
    }

    public List<SetAttribute> getSetAttributeList() {
        return setAttributeList;
    }

    /**
     * Attribute assignment for updates
     */
    public static class SetAttribute {
        private Variable tableVariable;
        private Expression assignmentExpression;

        public SetAttribute(Variable tableVariable, Expression assignmentExpression) {
            this.tableVariable = tableVariable;
            this.assignmentExpression = assignmentExpression;
        }

        public Variable getTableVariable() {
            return tableVariable;
        }

        public Expression getAssignmentExpression() {
            return assignmentExpression;
        }
    }
}
