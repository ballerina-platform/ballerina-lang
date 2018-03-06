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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
@Deprecated
public class BLangAnnotAttachmentAttributeValue extends BLangExpression
        implements AnnotationAttachmentAttributeValueNode {

    public Node value;
    public List<BLangAnnotAttachmentAttributeValue> arrayValues;

    public BLangAnnotAttachmentAttributeValue() {
        arrayValues = new ArrayList<>();
    }

    @Override
    public Node getValue() {
        return value;
    }

    @Override
    public List<BLangAnnotAttachmentAttributeValue> getValueArray() {
        return arrayValues;
    }

    @Override
    public void setValue(Node value) {
        this.value = value;
    }

    @Override
    public void addValue(AnnotationAttachmentAttributeValueNode value) {
        this.arrayValues.add((BLangAnnotAttachmentAttributeValue) value);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ANNOTATION_ATTACHMENT_ATTRIBUTE_VALUE;
    }

    @Override
    public String toString() {
        return "BLangAnnotAttributeValue: " + (value != null ? value.toString() : arrayValues.toString());
    }
}
