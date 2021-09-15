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
import org.ballerinalang.model.tree.matchpatterns.ErrorCauseMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorFieldMatchPatternsNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.ErrorMessageMatchPatternNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

/**
 * Represent error-match-pattern.
 *
 * @since 2.0.0
 */
public class BLangErrorMatchPattern extends BLangMatchPattern implements ErrorMatchPatternNode {

    // BLangNodes
    public BLangErrorMessageMatchPattern errorMessageMatchPattern;
    public BLangErrorCauseMatchPattern errorCauseMatchPattern;
    public BLangErrorFieldMatchPatterns errorFieldMatchPatterns;
    public BLangUserDefinedType errorTypeReference;

    @Override
    public UserDefinedTypeNode getErrorTypeReference() {
        return errorTypeReference;
    }

    @Override
    public void setErrorTypeReference(UserDefinedTypeNode userDefinedTypeNode) {
        this.errorTypeReference = (BLangUserDefinedType) userDefinedTypeNode;
    }

    @Override
    public ErrorMessageMatchPatternNode getErrorMessageMatchPattern() {
        return errorMessageMatchPattern;
    }

    @Override
    public void setErrorMessageMatchPattern(ErrorMessageMatchPatternNode errorMessageMatchPattern) {
        this.errorMessageMatchPattern = (BLangErrorMessageMatchPattern) errorMessageMatchPattern;
    }

    @Override
    public ErrorCauseMatchPatternNode getErrorCauseMatchPatternNode() {
        return errorCauseMatchPattern;
    }

    @Override
    public void setErrorCauseMatchPatternNode(ErrorCauseMatchPatternNode errorCauseMatchPatternNode) {
        this.errorCauseMatchPattern = (BLangErrorCauseMatchPattern) errorCauseMatchPatternNode;
    }

    @Override
    public ErrorFieldMatchPatternsNode getErrorFieldMatchPatternNode() {
        return errorFieldMatchPatterns;
    }

    @Override
    public void setErrorFieldMatchPatternsNode(ErrorFieldMatchPatternsNode errorFieldMatchPatternsNode) {
        this.errorFieldMatchPatterns = (BLangErrorFieldMatchPatterns) errorFieldMatchPatternsNode;
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
        return NodeKind.ERROR_MATCH_PATTERN;
    }
}
