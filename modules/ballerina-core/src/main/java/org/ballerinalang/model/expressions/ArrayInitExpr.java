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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.values.BValue;

/**
 * {@code ArrayInitExpr} represents an arrays initializer expression.
 * <p>
 * e.g.  int[] a;
 * a = [1, 2, 3, 4, 5, 6, 7, 8, 9]
 * <p>
 * Extends {@code NaryExpression} because can be considered as an operation with multiple arguments.
 *
 * @since 0.8.0
 */
public class ArrayInitExpr extends RefTypeInitExpr {

    public ArrayInitExpr(NodeLocation location, Expression[] argExprs) {
        super(location, argExprs);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

}
