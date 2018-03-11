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

/**
 * Implementation of {@link PatternStreamingInputNode}.
 *
 * @since 0.955.0
 */

public class BLangPatternStreamingInput extends BLangNode implements PatternStreamingInputNode {

    private PatternStreamingInputNode patternStreamingInput;
    private PatternStreamingEdgeInputNode patternStreamingEdgeInputNode;
    private boolean isFollowedBy;
    private boolean isLeftParenthesisEnabled;
    private boolean isRightParenthesisEnabled;

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
    public void setPatternStreamingEdgeInput(PatternStreamingEdgeInputNode patternStreamingEdgeInput) {
        this.patternStreamingEdgeInputNode = patternStreamingEdgeInput;
    }

    @Override
    public PatternStreamingInputNode getPatternStreamingInput() {
        return patternStreamingInput;
    }

    @Override
    public PatternStreamingEdgeInputNode getPatternStreamingEdgeInput() {
        return patternStreamingEdgeInputNode;
    }

    @Override
    public boolean isFollowedBy() {
        return isFollowedBy;
    }

    @Override
    public void setFollowedBy(boolean followedBy) {
        isFollowedBy = followedBy;
    }

    @Override
    public boolean isLeftParenthesisEnabled() {
        return isLeftParenthesisEnabled;
    }

    @Override
    public void setLeftParenthesisEnabled(boolean leftParenthesisEnabled) {
        isLeftParenthesisEnabled = leftParenthesisEnabled;
    }

    @Override
    public boolean isRightParenthesisEnabled() {
        return isRightParenthesisEnabled;
    }

    @Override
    public void setRightParenthesisEnabled(boolean rightParenthesisEnabled) {
        isRightParenthesisEnabled = rightParenthesisEnabled;
    }
}
