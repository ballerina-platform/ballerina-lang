/*
*  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.ClientDeclarationNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a client declaration.
 *
 * @since 2201.3.0
 */
public class BLangClientDeclaration extends BLangNode implements ClientDeclarationNode {

    public BLangLiteral uri;
    public BLangIdentifier prefix;
    public List<BLangAnnotationAttachment> annAttachments = new ArrayList<>();

    public BSymbol symbol;

    public LiteralNode getUri() {
        return uri;
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
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    public void setUri(LiteralNode uri) {
        this.uri = (BLangLiteral) uri;
    }

    @Override
    public void setPrefix(IdentifierNode prefix) {
        this.prefix = (BLangIdentifier) prefix;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CLIENT_DECL;
    }
    
    @Override
    public String toString() {
        return "BLangClientDeclaration: " + prefix + "[" + uri + "]";
    }
}
