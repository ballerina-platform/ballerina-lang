/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ErrorVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ErrorVariableReferenceNode.
 *
 * @since 0.985.0
 */
public class BLangErrorVarRef extends BLangVariableReference implements ErrorVariableReferenceNode {

    // BLangNodes
    public BLangIdentifier pkgAlias;
    public BLangVariableReference message;
    public BLangVariableReference cause;
    public List<BLangNamedArgsExpression> detail;
    public BLangVariableReference restVar;
    public BLangType typeNode;

    // Semantic Data
    public BVarSymbol varSymbol;

    public BLangErrorVarRef() {
        detail = new ArrayList<>();
    }

    @Override
    public BLangIdentifier getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public ExpressionNode getMessage() {
        return message;
    }

    @Override
    public List<BLangNamedArgsExpression> getDetail() {
        return detail;
    }

    @Override
    public BLangVariableReference getRestVar() {
        return this.restVar;
    }

    @Override
    public BLangType getTypeNode() {
        return this.typeNode;
    }

    @Override
    public String toString() {
        return "error (" + message + ", " + (cause != null ? cause.toString() : "") + detail + ")";
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
        return NodeKind.ERROR_VARIABLE_REF;
    }
}
