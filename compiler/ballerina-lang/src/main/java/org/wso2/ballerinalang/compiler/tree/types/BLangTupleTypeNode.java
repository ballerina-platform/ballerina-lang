/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.TupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code BLangUnionTypeNode} represents a tuple type node in Ballerina
 * <p>
 * e.g. [int, float , string]
 *
 * @since 0.966.0
 */
public class BLangTupleTypeNode extends BLangType implements TupleTypeNode {

    // BLangNodes
    public List<BLangType> memberTypeNodes = new ArrayList<>();
    public BLangType restParamType;

    @Override
    public List<BLangType> getMemberTypeNodes() {
        return memberTypeNodes;
    }

    @Override
    public BLangType getRestParamType() {
        return this.restParamType;
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
    public NodeKind getKind() {
        return NodeKind.TUPLE_TYPE_NODE;
    }

    @Override
    public String toString() {
        return "[" + memberTypeNodes.stream().map(BLangType::toString).collect(Collectors.joining(","))
                + ((restParamType != null) ? "," + restParamType.toString() + "...]" : "]");
    }
}
