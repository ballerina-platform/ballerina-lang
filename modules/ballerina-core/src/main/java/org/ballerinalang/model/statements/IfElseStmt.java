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

import java.util.ArrayList;
import java.util.List;

/**
 * {@code IfElseStmt} represents a if/else if/else statement.
 *
 * @since 0.8.0
 */
public class IfElseStmt extends AbstractStatement {
    private Expression ifCondition;
    private Statement thenBody;
    private ElseIfBlock[] elseIfBlocks;
    private Statement elseBody;

    private IfElseStmt(NodeLocation location,
                       Expression ifCondition,
                       Statement thenBody,
                       ElseIfBlock[] elseIfBlocks,
                       Statement elseBody) {
        super(location);
        this.ifCondition = ifCondition;
        this.thenBody = thenBody;
        this.elseIfBlocks = elseIfBlocks;
        this.elseBody = elseBody;
    }

    public Expression getCondition() {
        return ifCondition;
    }

    public Statement getThenBody() {
        return thenBody;
    }

    public ElseIfBlock[] getElseIfBlocks() {
        return elseIfBlocks;
    }

    public Statement getElseBody() {
        return elseBody;
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
     * Represent an else if block of an if statement.
     */
    public static class ElseIfBlock {
        NodeLocation location;
        Expression elseIfCondition;
        BlockStmt elseIfBody;

        public ElseIfBlock(NodeLocation location, Expression elseIfCondition, BlockStmt elseIfBody) {
            this.location = location;
            this.elseIfCondition = elseIfCondition;
            this.elseIfBody = elseIfBody;
        }

        public Expression getElseIfCondition() {
            return elseIfCondition;
        }

        public BlockStmt getElseIfBody() {
            return elseIfBody;
        }
    }

    /**
     * Builds a {@code IfElseStmt} statement.
     *
     * @since 0.8.0
     */
    public static class IfElseStmtBuilder {
        private NodeLocation location;
        private Expression ifCondition;
        private Statement thenBody;
        private List<ElseIfBlock> elseIfBlockList = new ArrayList<>();
        private Statement elseBody;

        public NodeLocation getLocation() {
            return location;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setIfCondition(Expression ifCondition) {
            this.ifCondition = ifCondition;
        }

        public void setThenBody(BlockStmt thenBody) {
            this.thenBody = thenBody;
        }

        public void addElseIfBlock(NodeLocation location, Expression elseIfCondition, BlockStmt elseIfBody) {
            this.elseIfBlockList.add(new ElseIfBlock(location, elseIfCondition, elseIfBody));
        }

        public void setElseBody(BlockStmt elseBody) {
            this.elseBody = elseBody;
        }

        public IfElseStmt build() {
            return new IfElseStmt(
                    location,
                    ifCondition,
                    thenBody,
                    elseIfBlockList.toArray(new ElseIfBlock[elseIfBlockList.size()]),
                    elseBody);
        }
    }
}
