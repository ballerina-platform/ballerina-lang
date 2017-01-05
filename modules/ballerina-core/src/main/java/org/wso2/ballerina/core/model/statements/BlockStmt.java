/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A BlockStmt represents a list of statements between balanced braces.
 *
 * @since 1.0.0
 */
public class BlockStmt implements Statement {

    private Statement[] statements;

    public BlockStmt(Statement[] statements) {
        this.statements = statements;
    }

    public Statement[] getStatements() {
        return this.statements;
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
     * Builds a {@code BlockStmt}
     *
     * @since 1.0.0
     */
    public static class BlockStmtBuilder {

        private List<Statement> statementList = new ArrayList<>();

        public BlockStmtBuilder() {
        }

        public void addStmt(Statement statement) {
            statementList.add(statement);
        }

        public BlockStmt build() {
            return new BlockStmt(statementList.toArray(new Statement[statementList.size()]));
        }
    }
}

