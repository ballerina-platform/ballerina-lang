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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.RecordVariableNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Represents a record variable node.
 *
 * Example:
 *      type Person record {
 *          string name;
 *          boolean married;
 *          !...
 *      };
 *
 *      Person {name, married} = {name: "Peter", married: true};
 *
 * @since 0.985.0
 */
public class BLangRecordVariable extends BLangVariable implements RecordVariableNode {

    // BLangNodes
    public List<BLangRecordVariableKeyValue> variableList;
    public BLangVariable restParam;

    public BLangRecordVariable() {
        this.annAttachments = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.variableList = new ArrayList<>();
    }

    @Override
    public List<BLangRecordVariableKeyValue> getVariables() {
        return variableList;
    }

    @Override
    public BLangVariable getRestParam() {
        return restParam;
    }

    @Override
    public boolean hasRestParam() {
        return this.restParam != null;
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
        return NodeKind.RECORD_VARIABLE;
    }

    @Override
    public String toString() {
        return String.valueOf(getBType()) + " {" + variableList.stream()
                .map(BLangRecordVariableKeyValue::toString)
                .collect(Collectors.joining(",")) + "} = " + this.expr;
    }

    /**
     * This static inner class represents key/value pair of a record variable.
     *
     * @since 0.985.0
     */
    public static class BLangRecordVariableKeyValue extends BLangNodeEntry implements BLangRecordVariableKeyValueNode {

        public BLangIdentifier key;
        public BLangVariable valueBindingPattern;

        @Override
        public BLangIdentifier getKey() {
            return key;
        }

        @Override
        public BLangVariable getValue() {
            return valueBindingPattern;
        }

        @Override
        public String toString() {
            return key + ": " + valueBindingPattern;
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
