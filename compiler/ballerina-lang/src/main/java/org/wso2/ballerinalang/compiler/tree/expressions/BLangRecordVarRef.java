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

    public BVarSymbol varSymbol;
    public BLangIdentifier pkgAlias;
    public List<BLangRecordVarRefKeyValue> recordRefFields;
    public ExpressionNode restParam;
    public boolean isClosed;

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
    public NodeKind getKind() {
        return NodeKind.RECORD_VARIABLE_REF;
    }

    /**
     * This static inner class represents key/value pair of a record var ref.
     *
     * @since 0.985.0
     */
    public static class BLangRecordVarRefKeyValue implements BLangRecordVarRefKeyValueNode {

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
    }

}
