/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import static io.ballerina.runtime.IdentifierUtils.encodeIdentifier;
import static io.ballerina.runtime.IdentifierUtils.unescapeUnicodeCodepoints;

/**
 * Identifier specific expression modifier implementation.
 */
public class ExpressionIdentifierModifier extends TreeModifier {

    private static final String QUOTED_IDENTIFIER_PREFIX = "'";

    @Override
    public IdentifierToken transform(IdentifierToken identifier) {
        String identifierText = identifier.text();
        // Removes quoted identifier prefix, if presents.
        if (identifierText.startsWith(QUOTED_IDENTIFIER_PREFIX)) {
            identifierText = identifierText.substring(1);
        }
        // Processes escaped unicode codepoints.
        String unescapedIdentifier = unescapeUnicodeCodepoints(identifierText);
        // Encodes the user provided identifier in order to be aligned with JVM runtime identifiers.
        return identifier.modify(encodeIdentifier(unescapedIdentifier));
    }
}
