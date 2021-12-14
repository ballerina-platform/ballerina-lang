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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import io.ballerina.runtime.api.utils.IdentifierUtils;

import static io.ballerina.runtime.api.utils.IdentifierUtils.unescapeUnicodeCodepoints;

/**
 * Identifier specific expression modifier implementation.
 */
public class IdentifierModifier extends TreeModifier {

    public static final String QUOTED_IDENTIFIER_PREFIX = "'";

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
        NonTerminalNode parent = identifier.parent();
        boolean isFunctionName = parent != null && (parent.kind() == SyntaxKind.FUNCTION_DEFINITION
                || parent.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION);

        return isFunctionName ? identifier.modify(encodeIdentifier(unescapedIdentifier, IdentifierType.METHOD_NAME)) :
                identifier.modify(encodeIdentifier(unescapedIdentifier, IdentifierType.OTHER));
    }

    public static String encodeModuleName(String identifier) {
        return encodeIdentifier(identifier, IdentifierType.OTHER);
    }

    public static String encodeIdentifier(String identifier, IdentifierType type) {
        if (identifier.startsWith(QUOTED_IDENTIFIER_PREFIX)) {
            identifier = identifier.substring(1);
        }
        identifier = IdentifierUtils.unescapeUnicodeCodepoints(identifier);
        return type == IdentifierType.METHOD_NAME ? IdentifierUtils.encodeFunctionIdentifier(identifier) :
                IdentifierUtils.encodeNonFunctionIdentifier(identifier);
    }

    public static String decodeIdentifier(String encodedIdentifier) {
        return IdentifierUtils.decodeIdentifier(encodedIdentifier);
    }

    public static String decodeAndEscapeIdentifier(String encodedIdentifier) {
        String decodedIdentifier = decodeIdentifier(encodedIdentifier);
        decodedIdentifier = IdentifierUtils.escapeSpecialCharacters(decodedIdentifier);
        if (!decodedIdentifier.startsWith(QUOTED_IDENTIFIER_PREFIX)) {
            decodedIdentifier = QUOTED_IDENTIFIER_PREFIX + decodedIdentifier;
        }

        return decodedIdentifier;
    }

    /**
     * Identifier types based on different runtime identifier encoding mechanisms.
     */
    public enum IdentifierType {
        METHOD_NAME,
        OTHER
    }
}
