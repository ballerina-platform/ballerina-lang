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

package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.expressions.Expression;

/**
 * Represents an abstract assign statement. All assign statements nodes extends this abstract class.
 * <p>
 *
 * @since 0.85
 */
public abstract class AbstractAssignStatement extends AbstractStatement {

    private Expression rhsExpr;

    public AbstractAssignStatement(NodeLocation location, Expression rhsExpr) {
        super(location);
        this.rhsExpr = rhsExpr;
    }

    public Expression getRhsExpr() {
        return rhsExpr;
    }

    public void setRhsExpr(Expression rhsExpr) {
        this.rhsExpr = rhsExpr;
    }

}
