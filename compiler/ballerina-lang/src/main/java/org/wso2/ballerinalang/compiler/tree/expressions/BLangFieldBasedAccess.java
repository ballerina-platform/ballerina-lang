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
import org.ballerinalang.model.tree.expressions.FieldBasedAccessNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.util.FieldKind;

/**
 * Implementation of {@link FieldBasedAccessNode}.
 *
 * @since 0.94
 */
public class BLangFieldBasedAccess extends BLangAccessExpression implements FieldBasedAccessNode {

    // BLangNodes
    public BLangIdentifier field;

    // Parser Flags and Data
    public FieldKind fieldKind;

    // Semantic Data
    public BVarSymbol varSymbol;
    // Only used at Desugar and after.
    public boolean isStoreOnCreation = false;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public BLangIdentifier getFieldName() {
        return field;
    }

    @Override
    public boolean isOptionalFieldAccess() {
        return optionalFieldAccess;
    }

    @Override
    public String toString() {
        return expr + (optionalFieldAccess ? "?." : ".") + field;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.FIELD_BASED_ACCESS_EXPR;
    }

    /**
     * @since 0.97
     */
    public static class BLangStructFunctionVarRef extends BLangFieldBasedAccess {

        public BLangStructFunctionVarRef(BLangExpression varRef, BVarSymbol varSymbol) {
            this.expr = varRef;
            this.symbol = varSymbol;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            analyzer.visit(this, props);
        }

        @Override
        public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
            return modifier.transform(this, props);
        }
    }

    /**
     * Extend {@link BLangFieldBasedAccess} to support xml attribute access expressions with namespace prefixes.
     *
     * @since 1.2.0
     */
    public static class BLangNSPrefixedFieldBasedAccess extends BLangFieldBasedAccess {

        // BLangNodes
        public BLangIdentifier nsPrefix;

        // Semantic Data
        public BXMLNSSymbol nsSymbol;

        public String toString() {
            return expr + "." + nsPrefix + ":" + field;
        }

        @Override
        public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            analyzer.visit(this, props);
        }

        @Override
        public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
            return modifier.transform(this, props);
        }

    }
}
