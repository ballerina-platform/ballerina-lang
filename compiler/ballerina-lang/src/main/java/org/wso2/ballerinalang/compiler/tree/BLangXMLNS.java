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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * @since 0.94
 */
public class BLangXMLNS extends BLangNode implements XMLNSDeclarationNode {

    public BLangExpression namespaceURI;
    public BLangIdentifier prefix;
    public BSymbol symbol;

    @Override
    public ExpressionNode getNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public BLangIdentifier getPrefix() {
        return prefix;
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
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public void setNamespaceURI(ExpressionNode namespaceURI) {
        this.namespaceURI = (BLangExpression) namespaceURI;
    }

    @Override
    public void setPrefix(IdentifierNode prefix) {
        this.prefix = (BLangIdentifier) prefix;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XMLNS;
    }
    
    @Override
    public String toString() {
        return "BLangXMLNS: " + prefix + "[" +  namespaceURI + "]";
    }

    /**
     * @since 0.94
     */
    public static class BLangLocalXMLNS extends BLangXMLNS {

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            analyzer.visit(this, props);
        }

        @Override
        public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
            return transformer.transform(this, props);
        }
    }

    /**
     * @since 0.94
     */
    public static class BLangPackageXMLNS extends BLangXMLNS {

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            analyzer.visit(this, props);
        }

        @Override
        public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
            return transformer.transform(this, props);
        }
    }
}
