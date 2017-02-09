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

import org.wso2.ballerina.core.model.LinkedNodeExecutor;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.Expression;

import java.util.List;

/**
 * {@code ForkJoinStmt} represents a fork/join statement.
 *
 * @since 0.8.0
 */
public class ForkJoinStmt extends AbstractStatement {
    private List<Worker> workers;
    private Expression joinCondition;
    private Statement joinBlock;

    public ForkJoinStmt(NodeLocation location, List<Worker> workers, Expression joinCondition, Statement joinBlock) {
        super(location);
        this.workers = workers;
        this.joinCondition = joinCondition;
        this.joinBlock = joinBlock;
    }

    @Override
    public void accept(NodeVisitor visitor) {
//        visitor.accept(this);
    }

    @Override
    public void execute(NodeExecutor executor) {

    }

    @Override
    public void executeLNode(LinkedNodeExecutor executor) {
        executor.visit(this);
    }
}
