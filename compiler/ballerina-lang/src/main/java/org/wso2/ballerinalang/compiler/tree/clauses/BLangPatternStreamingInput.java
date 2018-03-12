/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingInputNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link PatternStreamingInputNode}.
 *
 * @since 0.955.0
 */

public class BLangPatternStreamingInput extends BLangNode implements PatternStreamingInputNode {

    private PatternStreamingInputNode patternStreamingInput;
    private List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputNodeList = new ArrayList<>();
    private boolean isFollowedBy;
    private boolean isEnclosedInParanthesis;
    private boolean isForEach;
    private boolean isNotWithFor;
    private boolean isNotWithAnd;
    private boolean isOrAndOnly;

    @Override
    public NodeKind getKind() {
        return NodeKind.PATTERN_STREAMING_INPUT;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setPatternStreamingInput(PatternStreamingInputNode patternStreamingInput) {
        this.patternStreamingInput = patternStreamingInput;
    }

    @Override
    public void addPatternStreamingEdgeInput(PatternStreamingEdgeInputNode patternStreamingEdgeInput) {
        this.patternStreamingEdgeInputNodeList.add(patternStreamingEdgeInput);
    }

    @Override
    public PatternStreamingInputNode getPatternStreamingInput() {
        return patternStreamingInput;
    }

    @Override
    public List<PatternStreamingEdgeInputNode> getPatternStreamingEdgeInputs() {
        return patternStreamingEdgeInputNodeList;
    }

    @Override
    public boolean isFollowedBy() {
        return isFollowedBy;
    }

    @Override
    public void setEnclosedInParanthesis(boolean enclosedInParanthesis) {
        this.isEnclosedInParanthesis = enclosedInParanthesis;
    }

    @Override
    public boolean enclosedInParanthesis() {
        return isEnclosedInParanthesis;
    }

    @Override
    public void setForEach(boolean isForEach) {
        this.isForEach = isForEach;
    }

    @Override
    public boolean isForEach() {
        return isForEach;
    }

    @Override
    public void setAndWithNot(boolean isAndWithNot) {
        this.isNotWithAnd = isAndWithNot;
    }

    @Override
    public boolean isAndWithNot() {
        return isNotWithAnd;
    }

    @Override
    public void setForWithNot(boolean isForWithNot) {
        this.isNotWithFor = isForWithNot;
    }

    @Override
    public boolean isForWithNot() {
        return isNotWithFor;
    }

    @Override
    public void setAndOrOnly(boolean isAndOrOnly) {
        this.isOrAndOnly = isAndOrOnly;
    }

    @Override
    public boolean isAndOrOnly() {
        return isOrAndOnly;
    }

    @Override
    public void setFollowedBy(boolean followedBy) {
        isFollowedBy = followedBy;
    }
}
