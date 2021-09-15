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
package org.wso2.ballerinalang.compiler.tree.bindingpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.CaptureBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.SimpleBindingPatternNode;
import org.ballerinalang.model.tree.bindingpattern.WildCardBindingPatternNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Represent simple-binding-pattern.
 *
 * @since 2.0.0
 */
public class BLangSimpleBindingPattern extends BLangBindingPattern implements SimpleBindingPatternNode {

    // BLangNodes
    public BLangCaptureBindingPattern captureBindingPattern;
    public BLangWildCardBindingPattern wildCardBindingPattern;

    @Override
    public CaptureBindingPatternNode getCaptureBindingPattern() {
        return captureBindingPattern;
    }

    @Override
    public void setCaptureBindingPattern(CaptureBindingPatternNode captureBindingPattern) {
        this.captureBindingPattern = (BLangCaptureBindingPattern) captureBindingPattern;
    }

    @Override
    public WildCardBindingPatternNode getWildCardBindingPatternNode() {
        return wildCardBindingPattern;
    }

    @Override
    public void setWildCardBindingPatternNode(WildCardBindingPatternNode wildCardBindingPatternNode) {
        this.wildCardBindingPattern = wildCardBindingPattern;
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
        return NodeKind.SIMPLE_BINDING_PATTERN;
    }
}
