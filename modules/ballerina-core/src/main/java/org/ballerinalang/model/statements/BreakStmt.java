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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code BreakStmt} represents a break statement.
 */
public class BreakStmt extends AbstractStatement {

    private BlockStmt[] blockStmts;

    private BreakStmt(NodeLocation nodeLocations, WhiteSpaceDescriptor whiteSpaceDescriptor, BlockStmt[] statements) {
        super(nodeLocations);
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
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
     * Builds a {@link BreakStmt} statement.
     */
    public static class BreakStmtBuilder {

        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private List<BlockStmt> statementList = new ArrayList<>();

        public BreakStmtBuilder() {
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
            return whiteSpaceDescriptor;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public void addStmt(BlockStmt statement) {
            statementList.add(statement);
        }

        public BreakStmt build() {
            return new BreakStmt(location, whiteSpaceDescriptor,
                    statementList.toArray(new BlockStmt[statementList.size()]));
        }
    }
}
