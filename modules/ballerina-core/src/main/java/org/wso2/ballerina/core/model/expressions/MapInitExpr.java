/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code MapInitExpr} represents a map initializer expression
 * <p>
 * e.g.  map a;
 * a = {"name":"wso2", "employees":"500"}
 * <p>
 * Extends {@code NaryExpression} because can be considered as an operation with multiple arguments.
 *
 * @since 0.8.0
 */
public class MapInitExpr extends NaryExpression {

    public MapInitExpr(Expression[] argExprs) {
        super(null, null, argExprs);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     * {@code MapInitExprBuilder} represents a map initializer expression builder
     *
     * @since 0.8.0
     */
    public static class MapInitExprBuilder {
        List<KeyValueExpression> argList = new ArrayList<>();

        public MapInitExprBuilder() {
        }

        public void setArgList(List<KeyValueExpression> argList) {
            this.argList = argList;
        }

        public MapInitExpr build() {
            return new MapInitExpr(argList.toArray(new Expression[argList.size()]));
        }
    }
}
