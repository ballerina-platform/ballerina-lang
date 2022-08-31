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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * {@code BLangMemberTypeNode} represents a non-rest member of a tuple.
 *
 * @since 2201.3.0
 */
public class BLangMemberTypeNode extends BLangType implements AnnotatableNode {

    public BLangType typeNode;
    public List<BLangAnnotationAttachment> annAttachments;

    public BLangMemberTypeNode() {
        this.annAttachments = null;
        this.flagSet = EnumSet.noneOf(Flag.class);
    }

    @Override
    public Set<? extends Flag> getFlags() {
        return flagSet;
    }

    @Override
    public void addFlag(Flag flag) {
        this.flagSet.add(flag);
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TUPLE_MEMBER;
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
