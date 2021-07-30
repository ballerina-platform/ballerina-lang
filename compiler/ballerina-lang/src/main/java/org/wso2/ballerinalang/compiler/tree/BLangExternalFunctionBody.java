/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ExternalFunctionBodyNode;
import org.ballerinalang.model.tree.NodeKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an expression bodied function/method.
 *
 * @since 1.2.0
 */
public class BLangExternalFunctionBody extends BLangFunctionBody implements ExternalFunctionBodyNode {

    public List<BLangAnnotationAttachment> annAttachments;

    public BLangExternalFunctionBody() {
        this.annAttachments = new ArrayList<>();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.EXTERN_FUNCTION_BODY;
    }

    @Override
    public List<? extends AnnotationAttachmentNode> getAnnotationAttachments() {
        return this.annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.annAttachments.add((BLangAnnotationAttachment) annAttachment);
    }
}
