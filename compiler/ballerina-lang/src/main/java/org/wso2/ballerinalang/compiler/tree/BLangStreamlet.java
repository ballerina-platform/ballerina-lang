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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.StreamletNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.955.0
 */
public class BLangStreamlet extends BLangNode implements StreamletNode {

    public BLangIdentifier name;
    public List<BLangVariable> params;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public BLangFunction initFunction;
    private BlockNode body;
    private String siddhiQuery;
    private String streamIdsAsString;
    private List<VariableNode> globalVariables = new ArrayList<>();

    public BTypeSymbol symbol;

    public BLangStreamlet() {
        this.params = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.annAttachments = new ArrayList<>();
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
    public void setBody(BlockNode body) {
        this.body = body;
    }

    @Override
    public BlockNode getBody() {
        return body;
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
    public void setInitFunction(FunctionNode function) {
        this.initFunction = (BLangFunction) function;
    }

    @Override
    public FunctionNode getInitFunction() {
        return initFunction;
    }

    @Override
    public void addGlobalVariable(VariableNode variable) {
        globalVariables.add(variable);
    }

    @Override
    public List<VariableNode> getGlobalVariables() {
        return globalVariables;
    }

    @Override
    public Set<Flag> getFlags() {
        return flagSet;
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
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachement) {
        this.annAttachments.add((BLangAnnotationAttachment) annAttachement);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.STREAMLET;
    }

    public String getSiddhiQuery() {
        return siddhiQuery;
    }

    public void setSiddhiQuery(String siddhiQuery) {
        this.siddhiQuery = siddhiQuery;
    }

    public String getStreamIdsAsString() {
        return streamIdsAsString;
    }

    public void setStreamIdsAsString(String streamIdsAsString) {
        this.streamIdsAsString = streamIdsAsString;
    }
}
