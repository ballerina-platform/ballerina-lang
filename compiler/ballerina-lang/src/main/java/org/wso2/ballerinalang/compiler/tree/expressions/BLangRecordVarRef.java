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
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordVariableReferenceNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeEntry;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of RecordVariableReferenceNode.
 *
 * @since 0.985.0
 */
public class BLangRecordVarRef extends BLangVariableReference implements RecordVariableReferenceNode {
    // BLangNodes
    public BLangIdentifier pkgAlias;
    public List<BLangRecordVarRefKeyValue> recordRefFields;
    public BLangExpression restParam;

    // Semantic Data
    public BVarSymbol varSymbol;

    public BLangRecordVarRef() {
        recordRefFields = new ArrayList<>();
    }

    @Override
    public BLangIdentifier getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public List<? extends BLangRecordVarRefKeyValue> getRecordRefFields() {
        return recordRefFields;
    }

    @Override
    public ExpressionNode getRestParam() {
        return restParam;
    }

    @Override
    public String toString() {
        return " {" + recordRefFields.stream()
                .map(BLangRecordVarRefKeyValue::toString)
                .collect(Collectors.joining(",")) + "}";
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
        return NodeKind.RECORD_VARIABLE_REF;
    }

    /**
     * This static inner class represents key/value pair of a record var ref.
     *
     * @since 0.985.0
     */
    public static class BLangRecordVarRefKeyValue extends BLangNodeEntry implements BLangRecordVarRefKeyValueNode {

        // BLangNodes
        public BLangExpression variableReference;
        public BLangIdentifier variableName;

        @Override
        public BLangIdentifier getVariableName() {
            return variableName;
        }

        @Override
        public BLangExpression getBindingPattern() {
            return variableReference;
        }

        @Override
        public String toString() {
            return variableName + ": " + variableReference;
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
