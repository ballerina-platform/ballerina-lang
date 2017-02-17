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
 * {@code WhileStmt} represents a while statement.
 *
 * @since 0.8.0
 */
public class WhileStmt extends AbstractStatement {
    private Expression whileCondition;
    private BlockStmt whileBody;

    private WhileStmt(NodeLocation location, Expression whileCondition, BlockStmt whileBody) {
        super(location);
        this.whileCondition = whileCondition;
        this.whileBody = whileBody;
    }

    public Expression getCondition() {
        return whileCondition;
    }

    public BlockStmt getBody() {
        return whileBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    /**
     * Builds a {@code WhileStmt} statement.
     *
     * @since 0.8.0
     */
    public static class WhileStmtBuilder {
        private NodeLocation location;
        private Expression whileCondition;
        private BlockStmt whileBody;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setCondition(Expression whileCondition) {
            this.whileCondition = whileCondition;
        }

        public void setWhileBody(BlockStmt whileBody) {
            this.whileBody = whileBody;
        }

        public WhileStmt build() {
            return new WhileStmt(location, whileCondition, whileBody);
        }
    }
}
