/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.internal.parser.utils;

import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STQualifiedNameReferenceNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * Utility methods to resolve ambiguity between qualified name ref and conditional expression.
 *
 *
 * @since 2.0.0
 */
public class ConditionalExprResolver {
    private static final String BOOLEAN = "boolean";
    private static final String DECIMAL = "decimal";
    private static final String FLOAT = "float";
    private static final String INT = "int";
    private static final String STRING = "string";
    private static final String ERROR = "error";
    private static final String FUTURE = "future";
    private static final String MAP = "map";
    private static final String OBJECT = "object";
    private static final String STREAM = "stream";
    private static final String TABLE = "table";
    private static final String TRANSACTION = "transaction";
    private static final String TYPEDESC = "typedesc";
    private static final String XML = "xml";

    private ConditionalExprResolver() {
    }

    public static STNode getQualifiedNameRefNode(STNode parentNode, boolean leftMost) {
        if (parentNode.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            STNode modulePrefix = ((STQualifiedNameReferenceNode) parentNode).modulePrefix;
            return isValidSimpleNameRef((STToken) modulePrefix) ? parentNode : null;
        }

        STNode firstOrLastChild =
                leftMost ? parentNode.childInBucket(0) : parentNode.childInBucket(parentNode.bucketCount() - 1);
        if (SyntaxUtils.isNonTerminalNode(firstOrLastChild)) {
            return getQualifiedNameRefNode(firstOrLastChild, leftMost);
        }

        return null;
    }

    /**
     * Check whether an identifier is a valid SimpleNameRef.
     * Predeclared prefixes which are not a BuiltinSimpleNameReference is not valid as SimpleNameRef.
     *
     * @param modulePrefixIdentifier module prefix identifier
     * @return <code>true</code> if modulePrefixIdentifier text is Valid Simple NameRef
     */
    private static boolean isValidSimpleNameRef(STToken modulePrefixIdentifier) {
        switch (modulePrefixIdentifier.text()) {
            case ERROR:
            case FUTURE:
            case MAP:
            case OBJECT:
            case STREAM:
            case TABLE:
            case TRANSACTION:
            case TYPEDESC:
            case XML:
                return false;
            default:
                return true;
        }
    }

    /**
     * Generate STBuiltinSimpleNameReferenceNode or STSimpleNameReferenceNode.
     *
     * @param modulePrefixIdentifier module prefix identifier
     * @return generated SimpleNameReferenceNode
     */
    public static STNode getSimpleNameRefNode(STNode modulePrefixIdentifier) {
        STToken identifier = (STToken) modulePrefixIdentifier;
        STToken syntaxToken;
        switch (identifier.text()) {
            case BOOLEAN:
                syntaxToken = STNodeFactory.createToken(SyntaxKind.BOOLEAN_KEYWORD,
                        identifier.leadingMinutiae(), identifier.trailingMinutiae());
                break;
            case DECIMAL:
                syntaxToken = STNodeFactory.createToken(SyntaxKind.DECIMAL_KEYWORD,
                        identifier.leadingMinutiae(), identifier.trailingMinutiae());
                break;
            case FLOAT:
                syntaxToken = STNodeFactory.createToken(SyntaxKind.FLOAT_KEYWORD,
                        identifier.leadingMinutiae(), identifier.trailingMinutiae());
                break;
            case INT:
                syntaxToken = STNodeFactory.createToken(SyntaxKind.INT_KEYWORD,
                        identifier.leadingMinutiae(), identifier.trailingMinutiae());
                break;
            case STRING:
                syntaxToken = STNodeFactory.createToken(SyntaxKind.STRING_KEYWORD,
                        identifier.leadingMinutiae(), identifier.trailingMinutiae());
                break;
            default:
                return STNodeFactory.createSimpleNameReferenceNode(identifier);
        }
        return BallerinaParser.createBuiltinSimpleNameReference(syntaxToken);
    }
}
