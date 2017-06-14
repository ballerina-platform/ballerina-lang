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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.WhiteSpaceDescriptor;

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
    private String pkgName;
    private String pkgPath;
    private SymbolName symbolName;
    private Expression[] indexExprs;
    private boolean isLHSExpr;

    private ArrayMapAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String varName,
                               String pkgName, String pkgPath, Expression arrayVarRefExpr, Expression[] indexExprs) {
        super(location, whiteSpaceDescriptor, null, arrayVarRefExpr);
        this.varName = varName;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.symbolName = new SymbolName(varName, pkgPath);
        this.indexExprs = indexExprs;
    }

    private ArrayMapAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, SymbolName symbolName,
                               String pkgName, String pkgPath, Expression arrayVarRefExpr, Expression[] indexExprs) {
        super(location, whiteSpaceDescriptor, null, arrayVarRefExpr);
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.symbolName = symbolName;
        this.indexExprs = indexExprs;
    }

    @Override
    public String getVarName() {
        return varName;
    }

    @Override
    public String getPkgName() {
        return pkgName;
    }

    @Override
    public String getPkgPath() {
        return pkgPath;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public Expression[] getIndexExprs() {
        return indexExprs;
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

    /**
     * {@code ArrayMapAccessExprBuilder} represents an arrays access expression builder.
     *
     * @since 0.8.0
     */
    public static class ArrayMapAccessExprBuilder {
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private String varName;
        private String pkgName;
        private String pkgPath;
        private SymbolName symbolName;
        private Expression arrayMapVarRefExpr;
        private Expression[] indexExprs;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public void setPkgPath(String pkgPath) {
            this.pkgPath = pkgPath;
        }

        public void setSymbolName(SymbolName symbolName) {
            this.symbolName = symbolName;
        }

        public void setArrayMapVarRefExpr(Expression arrayMapVarRefExpr) {
            this.arrayMapVarRefExpr = arrayMapVarRefExpr;
        }

        public void setIndexExprs(Expression[] rExpr) {
            this.indexExprs = rExpr;
        }

        public ArrayMapAccessExpr build() {
            return new ArrayMapAccessExpr(location, whiteSpaceDescriptor, varName, pkgName, pkgPath, arrayMapVarRefExpr,
                    indexExprs);
        }
    }
}
