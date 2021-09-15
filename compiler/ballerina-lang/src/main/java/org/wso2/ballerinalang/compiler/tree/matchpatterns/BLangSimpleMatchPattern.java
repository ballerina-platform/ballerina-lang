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
import org.ballerinalang.model.tree.matchpatterns.ConstPatternNode;
import org.ballerinalang.model.tree.matchpatterns.SimpleMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.VarBindingPatternMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.WildCardMatchPatternNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Represent simple-match-pattern.
 *
 * @since Swan Lake
 */
public class BLangSimpleMatchPattern extends BLangMatchPattern implements SimpleMatchPatternNode {

    public BLangConstPattern constPattern;
    public BLangWildCardMatchPattern wildCardMatchPattern;
    public BLangVarBindingPatternMatchPattern varVariableName;

    @Override
    public ConstPatternNode getConstPattern() {
        return constPattern;
    }

    @Override
    public void setConstPattern(ConstPatternNode constPattern) {
        this.constPattern = (BLangConstPattern) constPattern;
    }

    @Override
    public WildCardMatchPatternNode getWildCardPattern() {
        return wildCardMatchPattern;
    }

    @Override
    public void setWildCardPattern(WildCardMatchPatternNode wildCardPattern) {
        this.wildCardMatchPattern = (BLangWildCardMatchPattern) wildCardPattern;
    }

    @Override
    public VarBindingPatternMatchPatternNode getVarBindingPatternMatchPattern() {
        return varVariableName;
    }

    @Override
    public void setVarBindingPatternMatchPattern(VarBindingPatternMatchPatternNode varBindingPatternMatchPattern) {
        this.varVariableName = (BLangVarBindingPatternMatchPattern) varBindingPatternMatchPattern;
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
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.SIMPLE_MATCH_PATTERN;
    }
}
