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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

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
        return parseDocumentationLines();
    }

    /**
     * Parse documentation lines.
     * <p>
     * <code>
     * DocumentationLine :=
     *          ( DocumentationLine
     *          | ReferenceDocumentationLine
     *          | DeprecationDocumentationLine
     *          | ParameterDocumentationLine
     *          | ReturnParameterDocumentationLine
     *          | InvalidDocumentationLine )
     * </code>
     * <p>
     * Refer {@link DocumentationLexer}
     *
     * @return Parsed node
     */
    private STNode parseDocumentationLines() {
        List<STNode> docLines = new ArrayList<>();
        STToken nextToken = peek();
        while (nextToken.kind == SyntaxKind.HASH_TOKEN) {
            docLines.add(parseSingleDocumentationLine());
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(docLines);
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
        return createMarkdownDeprecationDocumentationLineNode(hashToken, docElementList);
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
                return createMarkdownDocumentationLineNode(hashToken, docElementList);
            case 1:
                STNode docElement = docElements.get(0);
                if (docElement.kind == SyntaxKind.DOCUMENTATION_DESCRIPTION) {
                    return createMarkdownDocumentationLineNode(hashToken, docElementList);
                }
                // Else fall through
            default:
                return createMarkdownReferenceDocumentationLineNode(hashToken, docElementList);
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
            kind = SyntaxKind.MARKDOWN_RETURN_PARAMETER_DOCUMENTATION_LINE;
        } else {
            kind = SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE;
        }

        return STNodeFactory.createMarkdownParameterDocumentationLineNode(kind, hashToken, plusToken, parameterName,
                dashToken, docElementList);
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
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode identifier = consume();
            return parseBacktickExpr(identifier);
        }
        return parseBacktickContentToken();
    }

    /**
     * Parse back-tick content token.
     *
     * @return Parsed node
     */
    private STNode parseBacktickContentToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BACKTICK_CONTENT) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.BACKTICK_CONTENT);
        }
    }

    /**
     * Parse back-tick expr.
     *
     * @param identifier Initial identifier
     * @return Function call, method call or name reference node
     */
    private STNode parseBacktickExpr(STNode identifier) {
        STNode referenceName = parseQualifiedIdentifier(identifier);

        STToken nextToken = peek();
        switch (nextToken.kind) {
            case BACKTICK_TOKEN:
                return referenceName;
            case DOT_TOKEN:
                STNode dotToken = consume();
                return parseMethodCall(referenceName, dotToken);
            default:
                return parseFuncCall(referenceName);
        }
    }

    /**
     * Parse qualified name reference or simple name reference.
     *
     * @param identifier Initial identifier
     * @return Parsed node
     */
    private STNode parseQualifiedIdentifier(STNode identifier) {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
            STNode colon = consume();
            return parseQualifiedIdentifier(identifier, colon);
        }
        return STNodeFactory.createSimpleNameReferenceNode(identifier);
    }

    private STNode parseQualifiedIdentifier(STNode identifier, STNode colon) {
        STNode refName = parseIdentifier();
        return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, refName);
    }

    /**
     * Parse identifier token.
     *
     * @return Parsed node
     */
    private STNode parseIdentifier() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        }
    }

    /**
     * Parse function call expression.
     * <code>function-call-expr := function-reference ( )</code>
     *
     * @param referenceName Function name
     * @return Function call expression
     */
    private STNode parseFuncCall(STNode referenceName) {
        STNode openParen = parseOpenParenthesis();
        STNode args = STNodeFactory.createEmptyNodeList();
        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createFunctionCallExpressionNode(referenceName, openParen, args, closeParen);
    }

    /**
     * Parse method call expression.
     * <code>method-call-expr := reference-name . method-name ( )</code>
     *
     * @param referenceName Reference name
     * @param dotToken Dot token
     * @return Method call expression
     */
    private STNode parseMethodCall(STNode referenceName, STNode dotToken) {
        STNode methodName = parseSimpleNameReference();
        STNode openParen = parseOpenParenthesis();
        STNode args = STNodeFactory.createEmptyNodeList();
        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createMethodCallExpressionNode(referenceName, dotToken, methodName, openParen, args,
                closeParen);
    }

    /**
     * Parse simple name reference.
     *
     * @return Parsed node
     */
    private STNode parseSimpleNameReference() {
        STNode identifier = parseIdentifier();
        return STNodeFactory.createSimpleNameReferenceNode(identifier);
    }

    /**
     * Parse open parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseOpenParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.OPEN_PAREN_TOKEN);
        }
    }

    /**
     * Parse close parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseCloseParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        }
    }

    private STNode createMarkdownDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createMarkdownDocumentationLineNode(SyntaxKind.MARKDOWN_DOCUMENTATION_LINE, hashToken,
                documentationElements);
    }

    private STNode createMarkdownDeprecationDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createMarkdownDocumentationLineNode(SyntaxKind.MARKDOWN_DEPRECATION_DOCUMENTATION_LINE,
                hashToken, documentationElements);
    }

    private STNode createMarkdownReferenceDocumentationLineNode(STNode hashToken, STNode documentationElements) {
        return STNodeFactory.createMarkdownDocumentationLineNode(SyntaxKind.MARKDOWN_REFERENCE_DOCUMENTATION_LINE,
                hashToken, documentationElements);
    }
}
