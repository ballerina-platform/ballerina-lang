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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.VariableNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangVariable extends BLangNode implements VariableNode {
    public BLangType type;
    public BLangIdentifier name;
    public BLangExpression expr;
    public long flags;
    public List<BLangAnnotationAttachment> annAttachments;

    public BLangVariable(BLangType type,
                         BLangIdentifier name,
                         BLangExpression expr,
                         long flags,
                         List<BLangAnnotationAttachment> annAttachments) {
        this.type = type;
        this.name = name;
        this.expr = expr;
        this.flags = flags;
        this.annAttachments = annAttachments;
    }

    @Override
    public BLangType getType() {
        return type;
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public BLangExpression getInitialExpression() {
        return expr;
    }

    @Override
    public Set<Flag> getFlags() {
        // Convert flags long value to a set of Flags
        return null;
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
