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
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.values.BValue;

/**
 * {@code ArrayMapAccessExpr} represents an arrays access operation.
 * <p>
 * e.g. x[0] = 5;
 * y = x[0]
 *
 * @since 0.8.0
 */
public class ArrayMapAccessExpr extends UnaryExpression implements ReferenceExpr {
    private String varName;
    private SymbolName symbolName;
    private Expression indexExpr;
    private boolean isLHSExpr;

    private ArrayMapAccessExpr(NodeLocation location, SymbolName symbolName,
                               Expression arrayVarRefExpr, Expression indexExpr) {
        super(location, null, arrayVarRefExpr);
        this.symbolName = symbolName;
        this.indexExpr = indexExpr;
    }

    private ArrayMapAccessExpr(NodeLocation location, String varName,
                               Expression arrayVarRefExpr, Expression indexExpr) {
        super(location, null, arrayVarRefExpr);
        this.varName = varName;
        this.indexExpr = indexExpr;
    }

    @Override
    public String getVarName() {
        return varName;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public Expression getIndexExpr() {
        return indexExpr;
    }

    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    public void setLHSExpr(boolean lhsExpr) {
        isLHSExpr = lhsExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     * {@code ArrayMapAccessExprBuilder} represents an arrays access expression builder.
     *
     * @since 0.8.0
     */
    public static class ArrayMapAccessExprBuilder {
        private NodeLocation location;
        private SymbolName varName;
        private Expression arrayMapVarRefExpr;
        private Expression indexExpr;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setVarName(SymbolName varName) {
            this.varName = varName;
        }

        public void setArrayMapVarRefExpr(Expression arrayMapVarRefExpr) {
            this.arrayMapVarRefExpr = arrayMapVarRefExpr;
        }

        public void setIndexExpr(Expression rExpr) {
            this.indexExpr = rExpr;
        }

        public ArrayMapAccessExpr build() {
            return new ArrayMapAccessExpr(location, varName, arrayMapVarRefExpr, indexExpr);
        }
    }
}
