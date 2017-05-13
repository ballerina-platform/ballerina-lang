/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.expressions.Expression;

/**
 * {@code TransformStmt} represents a transform statement.
 *
 * @since 0.8.7
 */
public class TransformStmt extends AbstractStatement {
    private BlockStmt transformBody;
    private Expression[] rhsExprs;
    private Expression[] lhsExprs;

    private TransformStmt(NodeLocation location, Expression[] rhsExprs, Expression[] outputExpressions,
                          BlockStmt transformBody) {
        super(location);
        this.transformBody = transformBody;
        this.rhsExprs = rhsExprs;
        this.lhsExprs = outputExpressions;
    }

    public BlockStmt getBody() {
        return transformBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    public Expression[] getRhsExprs() {
        return rhsExprs;
    }

    public Expression[] getLhsExprs() {
        return lhsExprs;
    }

    /**
     * Builds a {@code TransformStmt} statement.
     *
     * @since 0.8.7
     */
    public static class TransformStmtBuilder {
        private NodeLocation location;
        private BlockStmt transformBody;
        private Expression[] rhsExprs;
        private Expression[] lhsExprs;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setTransformBody(BlockStmt transformBody) {
            this.transformBody = transformBody;
        }

        public void setRhsExprs(Expression[] rhsExprs) {
            this.rhsExprs = rhsExprs;
        }

        public void setLhsExprs(Expression[] lhsExprs) {
            this.lhsExprs = lhsExprs;
        }

        public TransformStmt build() {
            return new TransformStmt(location, rhsExprs, lhsExprs, transformBody);
        }
    }
}
