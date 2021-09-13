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
import org.ballerinalang.model.tree.matchpatterns.FieldMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.MappingMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.RestMatchPatternNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent mapping-match-pattern.
 *
 * @since 2.0.0
 */
public class BLangMappingMatchPattern extends BLangMatchPattern implements MappingMatchPatternNode {
    public List<BLangFieldMatchPattern> fieldMatchPatterns = new ArrayList<>();
    public BLangRestMatchPattern restMatchPattern;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.MAPPING_MATCH_PATTERN;
    }

    @Override
    public List<? extends FieldMatchPatternNode> getFieldMatchPatterns() {
        return fieldMatchPatterns;
    }

    @Override
    public void addFieldMatchPattern(FieldMatchPatternNode fieldMatchPatternNode) {
        fieldMatchPatterns.add((BLangFieldMatchPattern) fieldMatchPatternNode);
    }

    @Override
    public RestMatchPatternNode getRestMatchPattern() {
        return restMatchPattern;
    }

    @Override
    public void setRestMatchPattern(RestMatchPatternNode restMatchPattern) {
        this.restMatchPattern = (BLangRestMatchPattern) restMatchPattern;
    }
}
