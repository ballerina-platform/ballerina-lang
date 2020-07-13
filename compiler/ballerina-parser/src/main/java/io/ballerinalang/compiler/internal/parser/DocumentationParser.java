/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * A Documentation parser for ballerina.
 *
 * @since 2.0.0
 */
public class DocumentationParser extends AbstractParser {

    /* Ballerina flavored markdown (BFM) is supported by the documentation.
     * There is no error handler attached to this parser.
     * In case of an error, simply missing token will be inserted.
     */

    protected DocumentationParser(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    public STNode parse() {
        return parseDocumentationString();
    }

    @Override
    public STNode resumeParsing(ParserRuleContext context, Object... args) {
        return null;
    }

    /**
     * Parse documentation string.
     * <p>
     * <code>
     * DocumentationString :=
     *          ( DocumentationLine
     *          | ReferenceDocumentationLine
     *          | DeprecationDocumentationLine
     *          | ParameterDocumentationLine
     *          | ReturnParameterDocumentationLine
     *          | InvalidDocumentationLine ) +
     * </code>
     * <p>
     * Refer {@link BallerinaLexer#readDocumentationToken}
     *
     * @return Parsed node
     */
    private STNode parseDocumentationString() {
        List<STNode> docLines = new ArrayList<>();
        STToken nextToken = peek();
        while (nextToken.kind == SyntaxKind.HASH_TOKEN) {
            docLines.add(parseSingleDocumentationLine());
            nextToken = peek();
        }

        STNode documentationLines = STNodeFactory.createNodeList(docLines);
        return STNodeFactory.createDocumentationStringNode(documentationLines);
    }

    /**
     * Parse a single documentation line.
     *
     * @return Parsed node
     */
    private STNode parseSingleDocumentationLine() {
        STNode hashToken = consume();
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.PLUS_TOKEN) {
            return parseParameterDocumentationLine(hashToken);
        } else if (nextToken.kind == SyntaxKind.DEPRECATION_LITERAL) {
            return parseDeprecationDocumentationLine(hashToken);
        }
        return parseDocumentationLine(hashToken);
    }

    /**
     * Parse deprecation documentation line.
     *
     * @param hashToken Hash token at the beginning of the line
     * @return Parsed node
     */
    private STNode parseDeprecationDocumentationLine(STNode hashToken) {
        STNode deprecationLiteral = consume();

        List<STNode> docElements = parseDocumentationElements();
        docElements.add(0, deprecationLiteral);

        STNode docElementList = STNodeFactory.createNodeList(docElements);
        return createDeprecationDocumentationLineNode(hashToken, docElementList);
    }

    /**
     * Parse documentation line and reference documentation line.
     *
     * @param hashToken Hash token at the beginning of the line
     * @return Parsed node
     */
    private STNode parseDocumentationLine(STNode hashToken) {
        List<STNode> docElements = parseDocumentationElements();
        STNode docElementList = STNodeFactory.createNodeList(docElements);

        switch (docElements.size()) {
            case 0:
                // When documentation line is only a `#` token
                return createDocumentationLineNode(hashToken, docElementList);
            case 1:
                STNode docElement = docElements.get(0);
                if (docElement.kind == SyntaxKind.DOCUMENTATION_DESCRIPTION) {
                    return createDocumentationLineNode(hashToken, docElementList);
                }
                // Else fall through
            default:
                return createReferenceDocumentationLineNode(hashToken, docElementList);
        }
    }

    private List<STNode> parseDocumentationElements() {
        List<STNode> docElements = new ArrayList<>();
        STNode docElement;
        SyntaxKind nextTokenKind = peek().kind;
        while (!isEndOfIntermediateDocumentation(nextTokenKind)) {
            if (nextTokenKind == SyntaxKind.DOCUMENTATION_DESCRIPTION) {
                docElement = consume();

            } else {
                docElement = parseDocumentationReference();
            }
            docElements.add(docElement);
            nextTokenKind = peek().kind;
        }
        return docElements;
    }

    private STNode parseDocumentationReference() {
        STNode referenceType = STNodeFactory.createEmptyNode();
        if (isDocumentReferenceType(peek().kind)) {
            referenceType = consume();
        }

        STNode startBacktick = parseBacktickToken();
        STNode backtickContent = parseBacktickContent();
        STNode endBacktick = parseBacktickToken();

        return STNodeFactory.createDocumentationReferenceNode(referenceType, startBacktick, backtickContent,
                endBacktick);
    }

    private boolean isDocumentReferenceType(SyntaxKind kind) {
        switch (kind) {
            case TYPE_DOC_REFERENCE_TOKEN:
            case SERVICE_DOC_REFERENCE_TOKEN:
            case VARIABLE_DOC_REFERENCE_TOKEN:
            case VAR_DOC_REFERENCE_TOKEN:
            case ANNOTATION_DOC_REFERENCE_TOKEN:
            case MODULE_DOC_REFERENCE_TOKEN:
            case FUNCTION_DOC_REFERENCE_TOKEN:
            case PARAMETER_DOC_REFERENCE_TOKEN:
            case CONST_DOC_REFERENCE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse parameter documentation line and return parameter documentation line.
     *
     * @param hashToken Hash token at the beginning of the line
     * @return Parsed node
     */
    private STNode parseParameterDocumentationLine(STNode hashToken) {
        STNode plusToken = consume();
        STNode parameterName = parseParameterName();
        STNode dashToken = parseMinusToken();

        List<STNode> docElements = parseDocumentationElements();
        STNode docElementList = STNodeFactory.createNodeList(docElements);

        SyntaxKind kind;
        if (parameterName.kind == SyntaxKind.RETURN_KEYWORD) {
            kind = SyntaxKind.RETURN_PARAMETER_DOCUMENTATION_LINE;
        } else {
            kind = SyntaxKind.PARAMETER_DOCUMENTATION_LINE;
        }

        return STNodeFactory.createParameterDocumentationLineNode(kind, hashToken, plusToken, parameterName, dashToken,
                docElementList);
    }

    private boolean isEndOfIntermediateDocumentation(SyntaxKind kind) {
        switch (kind) {
            case DOCUMENTATION_DESCRIPTION:
            case PLUS_TOKEN:
            case PARAMETER_NAME:
            case MINUS_TOKEN:
            case BACKTICK_TOKEN:
            case BACKTICK_CONTENT:
            case RETURN_KEYWORD:
            case DEPRECATION_LITERAL:
                return false;
            default:
                return !isDocumentReferenceType(kind);
        }
    }

    /**
     * Parse parameter name token.
     *
     * @return Parsed node
     */
    private STNode parseParameterName() {
        SyntaxKind tokenKind = peek().kind;
        if (tokenKind == SyntaxKind.PARAMETER_NAME || tokenKind == SyntaxKind.RETURN_KEYWORD) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.PARAMETER_NAME);
        }
    }

    /**
     * Parse minus token.
     *
     * @return Parsed node
     */
    private STNode parseMinusToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.MINUS_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.MINUS_TOKEN);
        }
    }

    /**
     * Parse back-tick token.
     *
     * @return Parsed node
     */
    private STNode parseBacktickToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BACKTICK_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.BACKTICK_TOKEN);
        }
    }

    /**
     * Parse back-tick content token.
     *
     * @return Parsed node
     */
    private STNode parseBacktickContent() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BACKTICK_CONTENT) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.BACKTICK_CONTENT);
        }
    }

    private STNode createDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createDocumentationLineNode(SyntaxKind.DOCUMENTATION_LINE, hashToken,
                documentationElements);
    }

    private STNode createDeprecationDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createDocumentationLineNode(SyntaxKind.DEPRECATION_DOCUMENTATION_LINE, hashToken,
                documentationElements);
    }

    private STNode createReferenceDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createDocumentationLineNode(SyntaxKind.REFERENCE_DOCUMENTATION_LINE, hashToken,
                documentationElements);
    }
}
