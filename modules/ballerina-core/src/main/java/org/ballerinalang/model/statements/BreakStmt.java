/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code BreakStmt} represents a break statement
 */
public class BreakStmt extends AbstractStatement {

    private BlockStmt[] blockStmts;

    private BreakStmt(NodeLocation nodeLocations, BlockStmt[] statements) {
        super(nodeLocations);
        this.blockStmts = statements;
    }

    public BlockStmt[] getBlockStmts() {
        return blockStmts;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Executes the statement
     *
     * @param executor instance of a {@code NodeExecutor}
     */
    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    /**
     * Builds a {@link BreakStmt} statement.
     */
    public static class BreakStmtBuilder {

        private NodeLocation location;
        private List<BlockStmt> statementList = new ArrayList<>();

        public BreakStmtBuilder() {
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void addStmt(BlockStmt statement) {
            statementList.add(statement);
        }

        public BreakStmt build() {
            return new BreakStmt(location, statementList.toArray(new BlockStmt[statementList.size()]));
        }
    }
}
