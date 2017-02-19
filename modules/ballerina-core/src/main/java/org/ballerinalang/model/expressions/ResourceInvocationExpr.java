/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.model.expressions;

import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.values.BValue;

/**
 * {@code ResourceInvoker} is the entity which invokes a particular resource.
 *
 * @since 0.8.0
 */
public class ResourceInvocationExpr extends AbstractExpression implements ExecutableMultiReturnExpr {
    private Resource resource;
    private Expression[] exprs;

    public ResourceInvocationExpr(Resource resource, Expression[] exprs) {
        super(null);
        this.resource = resource;
        this.exprs = exprs;
    }

    public Resource getResource() {
        return resource;
    }

    public Expression[] getArgExprs() {
        return exprs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue[] executeMultiReturn(NodeExecutor executor) {
        return executor.visit(this);
    }

}
