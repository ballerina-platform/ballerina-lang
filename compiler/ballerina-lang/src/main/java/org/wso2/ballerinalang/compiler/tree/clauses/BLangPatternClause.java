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
import org.ballerinalang.model.tree.clauses.PatternClause;
import org.ballerinalang.model.tree.clauses.PatternStreamingInputNode;
import org.ballerinalang.model.tree.clauses.WithinClause;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.965.0
 *
 * The implementation of {@link PatternClause}.
 */
public class BLangPatternClause extends BLangNode implements PatternClause {

    public PatternStreamingInputNode patternStreamingInput;
    public boolean forAllEvents;
    public WithinClause withinClause;

    @Override
    public boolean isForAllEvents() {
        return forAllEvents;
    }

    @Override
    public void setForAllEvents(boolean isForAllEvents) {
        this.forAllEvents = isForAllEvents;
    }

    @Override
    public PatternStreamingInputNode getPatternStreamingNode() {
        return patternStreamingInput;
    }

    @Override
    public void setPatternStreamingInputNode(PatternStreamingInputNode pattern) {
        this.patternStreamingInput = pattern;
    }

    @Override
    public WithinClause getWithinClause() {
        return withinClause;
    }

    @Override
    public void setWithinClause(WithinClause within) {
        this.withinClause = within;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the kind of this node.
     *
     * @return the kind of this node.
     */
    @Override
    public NodeKind getKind() {
        return NodeKind.PATTERN_CLAUSE;
    }
}
