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
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public abstract class BLangInvokable extends BLangNode implements InvokableNode {
    
    public BLangIdentifier name;
    public List<BLangVariable> params;
    public List<BLangVariable> retParams;
    public BLangBlockStmt body;
    public Set<Flag> flags;
    public List<BLangAnnotationAttachment> annAttachments;

    public BLangInvokable() {
        this.params = new ArrayList<>();
        this.retParams = new ArrayList<>();
        this.annAttachments = new ArrayList<>();
        this.flags = new HashSet<>();
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }
    
    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    @Override
    public List<BLangVariable> getParameters() {
        return params;
    }
    
    @Override
    public void addParameter(VariableNode param) {
        this.getParameters().add((BLangVariable) param);
    }

    @Override
    public List<BLangVariable> getReturnParameters() {
        return retParams;
    }
    
    @Override
    public void addReturnParameter(VariableNode retParam) {
        this.getReturnParameters().add((BLangVariable) retParam);
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }
    
    @Override
    public void setBody(BlockNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public Set<Flag> getFlags() {
        return flags;
    }
    
    @Override
    public void addFlag(Flag flag) {
        this.getFlags().add(flag);
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }
    
    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }
    
}
