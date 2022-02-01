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
import org.ballerinalang.model.tree.types.UnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * {@code BLangUnionTypeNode} represents a union type node in Ballerina
 * <p>
 * e.g. int | float | string
 *
 * @since 0.966.0
 */
public class BLangUnionTypeNode extends BLangType implements UnionTypeNode {

    // BLangNodes
    public List<BLangType> memberTypeNodes;

    public BLangUnionTypeNode() {
        this.memberTypeNodes = new ArrayList<>();
    }

    public BLangUnionTypeNode(List<BLangType> memberTypeNodes) {
        this.memberTypeNodes = memberTypeNodes;
    }

    @Override
    public List<BLangType> getMemberTypeNodes() {
        return memberTypeNodes;
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
        return NodeKind.UNION_TYPE_NODE;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("|");
        memberTypeNodes.forEach(memberTypeNode -> stringJoiner.add(memberTypeNode.toString()));
        return stringJoiner.toString();
    }
}
