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
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.model.CatchScope;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;

/**
 * {@code TryCatchStmt} represents a try/catch statement.
 *
 * @since 0.8.0
 */
public class TryCatchStmt extends AbstractStatement {
    private Statement tryBlock;
    private CatchScope catchScope;
    private Statement catchBlock;

    private TryCatchStmt(NodeLocation location, Statement tryBlock, Statement catchBlock, CatchScope catchScope) {
        super(location);
        this.tryBlock = tryBlock;
        this.catchBlock = catchBlock;
        this.catchScope = catchScope;
    }

    public Statement getTryBlock() {
        return tryBlock;
    }

    public Statement getCatchBlock() {
        return catchBlock;
    }

    public CatchScope getCatchScope() {
        return catchScope;
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
     * Builds a {@code {@link TryCatchStmt}} statement.
     *
     * @since 0.8.0
     */
    public static class TryCatchStmtBuilder {
        private CatchScope catchScope;
        private Statement tryBlock;
        private Statement catchBlock;
        private NodeLocation location;

        public Statement getTryBlock() {
            return tryBlock;
        }

        public void setTryBlock(Statement tryBlock) {
            this.tryBlock = tryBlock;
        }

        public CatchScope getCatchScope() {
            return catchScope;
        }

        public void setCatchScope(CatchScope catchScope) {
            this.catchScope = catchScope;
        }

        public Statement getCatchBlock() {
            return catchBlock;
        }

        public void setCatchBlock(Statement catchBlock) {
            this.catchBlock = catchBlock;
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setLocation(NodeLocation location) {
            this.location = location;
        }

        public TryCatchStmt build() {
            return new TryCatchStmt(
                    location,
                    tryBlock,
                    catchBlock,
                    catchScope);
        }
    }
}
