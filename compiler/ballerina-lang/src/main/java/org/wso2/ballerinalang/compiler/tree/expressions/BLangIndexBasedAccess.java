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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Implementation of {@link IndexBasedAccessNode}.
 *
 * @since 0.94
 */
public class BLangIndexBasedAccess extends BLangAccessExpression implements IndexBasedAccessNode {

    public BLangExpression indexExpr;

    // Only used at Desugar and after.
    public boolean isStoreOnCreation = false;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public BLangExpression getIndex() {
        return indexExpr;
    }

    @Override
    public String toString() {
        return String.valueOf(expr) + "[" + String.valueOf(indexExpr) + "]";
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.INDEX_BASED_ACCESS_EXPR;
    }

    /**
     * @since 0.94
     */
    public static class BLangArrayAccessExpr extends BLangIndexBasedAccess {

        public BLangArrayAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression indexExpr) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = indexExpr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangMapAccessExpr extends BLangIndexBasedAccess {

        public BLangMapAccessExpr(DiagnosticPos pos, BLangExpression varExpr, BLangExpression keyExpr) {
            this.pos = pos;
            this.expr = varExpr;
            this.indexExpr = keyExpr;
        }

        public BLangMapAccessExpr(DiagnosticPos pos, BLangExpression varExpr, BLangExpression keyExpr,
                                  boolean isStoreOnCreation) {
            this.pos = pos;
            this.expr = varExpr;
            this.indexExpr = keyExpr;
            this.isStoreOnCreation = isStoreOnCreation;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangJSONAccessExpr extends BLangIndexBasedAccess {

        public BLangJSONAccessExpr(DiagnosticPos pos, BLangExpression varExpr, BLangExpression keyExpr) {
            this.pos = pos;
            this.expr = varExpr;
            this.indexExpr = keyExpr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 1.3.0
     */
    public static class BLangTableAccessExpr extends BLangIndexBasedAccess {

        public BLangTableAccessExpr(DiagnosticPos pos, BLangExpression varExpr, BLangExpression keyExpr) {
            this.pos = pos;
            this.expr = varExpr;
            this.indexExpr = keyExpr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 1.0
     */
    public static class BLangStringAccessExpr extends BLangIndexBasedAccess {

        public BLangStringAccessExpr(DiagnosticPos pos, BLangExpression varExpr, BLangExpression keyExpr) {
            this.pos = pos;
            this.expr = varExpr;
            this.indexExpr = keyExpr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangXMLAccessExpr extends BLangIndexBasedAccess {

        public FieldKind fieldType;
        
        public BLangXMLAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression indexExpr) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = indexExpr;
            this.fieldType = FieldKind.SINGLE;
        }

        public BLangXMLAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression indexExpr,
                FieldKind fieldType) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = indexExpr;
            this.fieldType = fieldType;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangStructFieldAccessExpr extends BLangIndexBasedAccess {

        public BLangStructFieldAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression keyExpr,
                                          BVarSymbol fieldSymbol, boolean except) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = keyExpr;
            this.symbol = fieldSymbol;
        }

        public BLangStructFieldAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression keyExpr,
                                          BVarSymbol fieldSymbol, boolean except, boolean isStoreOnCreation) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = keyExpr;
            this.symbol = fieldSymbol;
            this.isStoreOnCreation = isStoreOnCreation;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * @since 0.980.0
     */
    @Deprecated
    public static class BLangTupleAccessExpr extends BLangIndexBasedAccess {

        @Deprecated
        public BLangTupleAccessExpr(DiagnosticPos pos, BLangExpression varRef, BLangExpression indexExpr) {
            this.pos = pos;
            this.expr = varRef;
            this.indexExpr = indexExpr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
