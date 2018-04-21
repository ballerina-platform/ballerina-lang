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
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of SimpleVariableReferenceNode.
 *
 * @since 0.94
 */
public class BLangSimpleVarRef extends BLangVariableReference implements SimpleVariableReferenceNode {

    public BVarSymbol varSymbol;
    public BLangIdentifier pkgAlias;

    public BLangIdentifier variableName;

    @Override
    public BLangIdentifier getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public BLangIdentifier getVariableName() {
        return variableName;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        if (pkgAlias != null && !pkgAlias.getValue().isEmpty()) {
            br.append(String.valueOf(pkgAlias)).append(":");
        }
        br.append(String.valueOf(variableName));
        return br.toString();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.SIMPLE_VARIABLE_REF;
    }

    /***
     * @since 0.94
     */
    public static class BLangLocalVarRef extends BLangSimpleVarRef {

        public BLangLocalVarRef(BVarSymbol varSymbol) {
            this.symbol = varSymbol;
            this.varSymbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return this.symbol.name.value;
        }
    }

    /***
     * @since 0.94
     */
    public static class BLangFieldVarRef extends BLangSimpleVarRef {

        public BLangFieldVarRef(BVarSymbol varSymbol) {
            this.symbol = varSymbol;
            this.varSymbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /***
     * @since 0.94
     */
    public static class BLangPackageVarRef extends BLangSimpleVarRef {

        public BLangPackageVarRef(BVarSymbol varSymbol) {
            this.symbol = varSymbol;
            this.varSymbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /***
     * @since 0.94
     */
    public static class BLangFunctionVarRef extends BLangSimpleVarRef {

        public BLangFunctionVarRef(BVarSymbol varSymbol) {
            this.symbol = varSymbol;
            this.varSymbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /***
     * @since 0.970.0
     */
    public static class BLangTypeLoad extends BLangSimpleVarRef {

        public BLangTypeLoad(BSymbol varSymbol) {
            this.symbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
