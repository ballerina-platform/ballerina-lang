/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.matchpatterns.ListMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.MatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.RestMatchPattern;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent list-match-pattern.
 *
 * @since Swan Lake
 */
public class BLangListMatchPattern extends BLangMatchPattern implements ListMatchPatternNode {
    public List<BLangMatchPattern> matchPatterns = new ArrayList<>();
    public BLangRestMatchPattern restMatchPattern;

    @Override
    public List<? extends MatchPatternNode> getMatchPatterns() {
        return matchPatterns;
    }

    @Override
    public void addMatchPattern(MatchPatternNode matchPattern) {
        matchPatterns.add((BLangMatchPattern) matchPattern);
    }

    @Override
    public RestMatchPattern getRestMatchPattern() {
        return restMatchPattern;
    }

    @Override
    public void setRestMatchPattern(RestMatchPattern restMatchPattern) {
        this.restMatchPattern = (BLangRestMatchPattern) restMatchPattern;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LIST_MATCH_PATTERN;
    }
}
