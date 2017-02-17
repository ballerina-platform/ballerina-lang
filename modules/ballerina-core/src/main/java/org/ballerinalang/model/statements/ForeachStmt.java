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
 * {@code ForeachStmt} Represents a foreach statement.
 *
 * @since 0.8.0
 */
public class ForeachStmt extends AbstractStatement {
    private Expression condition;
    //    private IteratorType itr;
    private Statement forEachBlock;

    public ForeachStmt(NodeLocation location) {
        super(location);
    }

//    public ForeachStmt(Expression condition, IteratorType itr, Statement forEachBlock) {
//        this.condition = condition;
//        this.itr = itr;
//        this.forEachBlock = forEachBlock;
//    }

    @Override
    public void accept(NodeVisitor visitor) {
//        visitor.accept(this);
    }

    @Override
    public void execute(NodeExecutor executor) {

    }
}
