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
package org.ballerinalang.model.tree.matchpatterns;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;

/**
 * The interface with the APIs to implement the error-match-pattern.
 *
 * @since 2.0.0
 */
public interface ErrorMatchPatternNode extends Node {

    UserDefinedTypeNode getErrorTypeReference();

    void setErrorTypeReference(UserDefinedTypeNode userDefinedTypeNode);

    ErrorMessageMatchPatternNode getErrorMessageMatchPattern();

    void setErrorMessageMatchPattern(ErrorMessageMatchPatternNode errorMessageMatchPattern);

    ErrorCauseMatchPatternNode getErrorCauseMatchPatternNode();

    void setErrorCauseMatchPatternNode(ErrorCauseMatchPatternNode errorCauseMatchPatternNode);

    ErrorFieldMatchPatternsNode getErrorFieldMatchPatternNode();

    void setErrorFieldMatchPatternsNode(ErrorFieldMatchPatternsNode errorFieldMatchPatternsNode);
}
