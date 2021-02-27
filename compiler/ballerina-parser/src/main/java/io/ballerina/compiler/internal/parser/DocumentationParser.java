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

import io.ballerina.compiler.internal.diagnostics.DiagnosticWarningCode;
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
     * <br/>
     * MarkdownDocumentationLine
     * <br/>
     * | MarkdownReferenceDocumentationLine
     * <br/>
     * | MarkdownDeprecationDocumentationLine
     * <br/>
     * | MarkdownParameterDocumentationLine
     * <br/>
     * | MarkdownReturnParameterDocumentationLine
     * <br/>
     * | MarkdownCodeBlock
     * <br/>
     * | InvalidMarkdownDocumentationLine
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
        switch (nextToken.kind) {
            case PLUS_TOKEN:
                return parseParameterDocumentationLine(hashToken);
            case DEPRECATION_LITERAL:
                return parseDeprecationDocumentationLine(hashToken);
            case TRIPLE_BACKTICK_TOKEN:
            case DOUBLE_BACKTICK_TOKEN:
                return parseCodeBlockOrInlineCodeRef(hashToken);
            default:
                return parseDocumentationLine(hashToken);
        }
    }

    /**
     * Parse documentation line starts with a inline code reference and a code block.
     * <p>
     * <i>Note: Code block should always start at the beginning of the line</i>
     *
     * @return Parsed node
     */
    private STNode parseCodeBlockOrInlineCodeRef(STNode startLineHash) {
        STNode startBacktick = consume();

        STToken nextToken = peek();
        if (!isInlineCodeRef(nextToken.kind)) {
            return parseCodeBlock(startLineHash, startBacktick);
        }

        STNode inlineCodeNode = parseInlineCode(startBacktick);
        List<STNode> docElements = new ArrayList<>();
        docElements.add(inlineCodeNode);
        parseDocElements(docElements);
        STNode docElementList = STNodeFactory.createNodeList(docElements);
        return createMarkdownReferenceDocumentationLineNode(startLineHash, docElementList);
    }

    private boolean isInlineCodeRef(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case HASH_TOKEN:
                return getNextNextToken().kind == SyntaxKind.DOCUMENTATION_DESCRIPTION;
            case CODE_CONTENT:
                return getNextNextToken().kind != SyntaxKind.HASH_TOKEN;
            case DOUBLE_BACKTICK_TOKEN:
            case TRIPLE_BACKTICK_TOKEN:
            default:
                return true;
        }
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
        parseDocElements(docElements);
        return docElements;
    }

    private void parseDocElements(List<STNode> docElements) {
        STNode docElement;
        STNode referenceType;

        SyntaxKind nextTokenKind = peek().kind;
        while (!isEndOfIntermediateDocumentation(nextTokenKind)) {
            switch (nextTokenKind) {
                case DOCUMENTATION_DESCRIPTION:
                    docElement = consume();
                    break;
                case CODE_CONTENT:
                    STToken token = consume();
                    docElement = convertToDocDescriptionToken(token);
                    break;
                case DOUBLE_BACKTICK_TOKEN:
                case TRIPLE_BACKTICK_TOKEN:
                    docElement = parseInlineCode(consume());
                    break;
                case BACKTICK_TOKEN:
                    referenceType = STNodeFactory.createEmptyNode();
                    docElement = parseBallerinaNameRefOrInlineCodeRef(referenceType);
                    break;
                default:
                    if (isDocumentReferenceType(nextTokenKind)) {
                        referenceType = consume();
                        docElement = parseBallerinaNameRefOrInlineCodeRef(referenceType);
                        break;
                    }

                    // We should not reach here.
                    assert false;
                    consume();
                    nextTokenKind = peek().kind;
                    continue;
            }

            docElements.add(docElement);
            nextTokenKind = peek().kind;
        }
    }

    /**
     * Convert {@link SyntaxKind#CODE_CONTENT} token to {@link SyntaxKind#DOCUMENTATION_DESCRIPTION}.
     *
     * @param token Code content token
     * @return Converted token
     */
    private STNode convertToDocDescriptionToken(STToken token) {
        return STNodeFactory.createLiteralValueToken(SyntaxKind.DOCUMENTATION_DESCRIPTION, token.text(),
                token.leadingMinutiae(), token.trailingMinutiae());
    }

    /**
     * Convert {@link SyntaxKind#DOCUMENTATION_DESCRIPTION} token to {@link SyntaxKind#CODE_CONTENT}.
     *
     * @param token Documentation description token
     * @return Converted token
     */
    private STNode convertToCodeContentToken(STToken token) {
        return STNodeFactory.createLiteralValueToken(SyntaxKind.CODE_CONTENT, token.text(),
                token.leadingMinutiae(), token.trailingMinutiae());
    }

    /**
     * Parse inline code reference.
     *
     * @return Parsed node
     */
    private STNode parseInlineCode(STNode startBacktick) {
        STNode codeDescription = parseInlineCodeContentToken();
        STNode endBacktick = parseCodeEndBacktick(startBacktick.kind);
        return STNodeFactory.createInlineCodeReferenceNode(startBacktick, codeDescription, endBacktick);
    }

    /**
     * Parse code content token in the inline code reference.
     * <p>
     * <i>Note: If the code content token is missing and available token is a documentation description, it is converted
     * to the expected kind. (This is to handle Scenario-2 of doc_source_22.bal properly)</i>
     *
     * @return Parsed node
     */
    private STNode parseInlineCodeContentToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CODE_CONTENT) {
            return consume();
        } else if (token.kind == SyntaxKind.DOCUMENTATION_DESCRIPTION) {
            token = consume();
            return convertToCodeContentToken(token);
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.CODE_CONTENT);
        }
    }

    /**
     * Parse code block.
     * <p>
     * <code>
     * Code-Block :=
     * <br/>
     * # ``` [lang-attribute]
     * <br/>
     * code-line*
     * <br/>
     * # ```
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseCodeBlock(STNode startLineHash , STNode startBacktick) {
        STNode langAttribute = parseOptionalLangAttributeToken();
        STNode codeLines = parseCodeLines();
        STNode endLineHash = parseHashToken();
        STNode endBacktick = parseCodeEndBacktick(startBacktick.kind);

        while (!isEndOfIntermediateDocumentation(peek().kind)) {
            STNode invalidToken = consume();
            endBacktick = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(endBacktick, invalidToken,
                    DiagnosticWarningCode.WARNING_CANNOT_HAVE_DOCUMENTATION_INLINE_WITH_A_CODE_REFERENCE_BLOCK);
        }

        return STNodeFactory.createMarkdownCodeBlockNode(startLineHash, startBacktick, langAttribute,
                codeLines, endLineHash, endBacktick);
    }

    /**
     * Parse optional language attribute token.
     *
     * @return Parsed node
     */
    private STNode parseOptionalLangAttributeToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CODE_CONTENT) {
            return consume();
        } else {
            return STNodeFactory.createEmptyNode();
        }
    }

    /**
     * Parse code lines of a code block.
     *
     * @return Parsed node
     */
    private STNode parseCodeLines() {
        List<STNode> codeLineList = new ArrayList<>();
        while (!isEndOfCodeLines()) {
            STNode codeLineNode = parseCodeLine();
            codeLineList.add(codeLineNode);
        }

        return STNodeFactory.createNodeList(codeLineList);
    }

    /**
     * Parse a single code line of code block.
     *
     * @return Parsed node
     */
    private STNode parseCodeLine() {
        STNode hash = parseHashToken();

        STNode codeDescription;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.HASH_TOKEN) {
            // We reach here, when the code line is empty
            codeDescription = createEmptyCodeContentToken();
        } else {
            codeDescription = parseInlineCodeContentToken();
        }

        return STNodeFactory.createMarkdownCodeLineNode(hash, codeDescription);
    }

    /**
     * Create an empty code content token.
     *
     * @return Parsed node
     */
    private STNode createEmptyCodeContentToken() {
        STNode codeDescription;
        String lexeme = "";
        STNode emptyMinutiae = STNodeFactory.createEmptyNodeList();
        codeDescription = STNodeFactory.createLiteralValueToken(SyntaxKind.CODE_CONTENT, lexeme, emptyMinutiae,
                emptyMinutiae);
        return codeDescription;
    }

    /**
     * Parse hash token.
     *
     * @return Parsed node
     */
    private STNode parseHashToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.HASH_TOKEN) {
            return consume();
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.HASH_TOKEN);
        }
    }

    /**
     * Parse ending backtick token of a code reference.
     *
     * @param backtickKind Kind of the code ref starting backtick
     * @return Parsed node
     */
    private STNode parseCodeEndBacktick(SyntaxKind backtickKind) {
        STToken token = peek();
        if (token.kind == backtickKind) {
            return consume();
        } else {
            return createMissingTokenWithDiagnostics(backtickKind);
        }
    }

    private boolean isEndOfCodeLines() {
        STNode nextToken = peek();
        if (nextToken.kind == SyntaxKind.HASH_TOKEN) {
            STNode nextNextToken = getNextNextToken();
            switch (nextNextToken.kind) {
                case CODE_CONTENT:
                case HASH_TOKEN: // case where code line description is empty
                    return false;
                default:
                    return true;
            }
        }

        return true;
    }

    /**
     * Parse ballerina name reference and inline code reference.
     *
     * @param referenceType Token that precedes the single backtick
     * @return Parsed node
     */
    private STNode parseBallerinaNameRefOrInlineCodeRef(STNode referenceType) {
        STNode startBacktick = parseBacktickToken();

        boolean isCodeRef = false;
        STNode contentToken;
        ReferenceGenre referenceGenre = getReferenceGenre(referenceType);
        if (isBallerinaNameRefTokenSequence(referenceGenre)) {
            contentToken = parseNameReferenceContent();
        } else {
            contentToken = combineAndCreateCodeContentToken();
            if (referenceGenre != ReferenceGenre.NO_KEY) {
                contentToken = SyntaxErrors.addDiagnostic(contentToken,
                        DiagnosticWarningCode.WARNING_INVALID_BALLERINA_NAME_REFERENCE,
                        ((STToken) contentToken).text());
            } else {
                isCodeRef = true;
            }
        }

        STNode endBacktick = parseBacktickToken();

        if (isCodeRef) {
            return STNodeFactory.createInlineCodeReferenceNode(startBacktick, contentToken, endBacktick);
        } else {
            return STNodeFactory.createBallerinaNameReferenceNode(referenceType, startBacktick, contentToken,
                    endBacktick);
        }
    }

    /**
     * Represents the current position with respect to the head in a token-sequence-search.
     */
    private static class Lookahead {
        private int offset = 1;
    }

    /**
     * Genre of the reference that precedes the backtick block.
     */
    private enum ReferenceGenre {
        NO_KEY, SPECIAL_KEY, FUNCTION_KEY
    }

    /**
     * Look ahead and see if incoming token sequence is a ballerina name reference.
     *
     * @param refGenre Genre of the backtick block reference
     * @return <code>true</code> if content is a ballerina name reference<code>false</code> otherwise.
     */
    private boolean isBallerinaNameRefTokenSequence(ReferenceGenre refGenre) {
        boolean hasMatch;
        Lookahead lookahead = new Lookahead();
        switch (refGenre) {
            case SPECIAL_KEY:
                // Look for x, m:x match
                hasMatch = hasQualifiedIdentifier(lookahead);
                break;
            case FUNCTION_KEY:
                // Look for x, m:x, x(), m:x(), T.y(), m:T.y() match
                hasMatch = hasBacktickExpr(lookahead, true);
                break;
            case NO_KEY:
                // Look for x(), m:x(), T.y(), m:T.y() match
                hasMatch = hasBacktickExpr(lookahead, false);
                break;
            default:
                throw new IllegalStateException("Unsupported backtick reference genre");
        }

        return hasMatch && peek(lookahead.offset).kind == SyntaxKind.BACKTICK_TOKEN;
    }

    private boolean hasBacktickExpr(Lookahead lookahead, boolean isFunctionKey) {
        if (!hasQualifiedIdentifier(lookahead)) {
            return false;
        }

        STToken nextToken = peek(lookahead.offset);
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return hasFuncSignature(lookahead);
        } else if (nextToken.kind == SyntaxKind.DOT_TOKEN) {
            lookahead.offset++;
            if (!hasIdentifier(lookahead)) {
                return false;
            }
            return hasFuncSignature(lookahead);
        }

        return isFunctionKey;
    }

    private boolean hasFuncSignature(Lookahead lookahead) {
        if (!hasOpenParenthesis(lookahead)) {
            return false;
        }
        return hasCloseParenthesis(lookahead);
    }

    private boolean hasOpenParenthesis(Lookahead lookahead) {
        STToken nextToken = peek(lookahead.offset);
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            lookahead.offset++;
            return true;
        } else {
            return false;
        }
    }

    private boolean hasCloseParenthesis(Lookahead lookahead) {
        STToken nextToken = peek(lookahead.offset);
        if (nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            lookahead.offset++;
            return true;
        } else {
            return false;
        }
    }

    private boolean hasQualifiedIdentifier(Lookahead lookahead) {
        if (!hasIdentifier(lookahead)) {
            return false;
        }

        STToken nextToken = peek(lookahead.offset);
        if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
            lookahead.offset++;
            return hasIdentifier(lookahead);
        }

        return true;
    }

    private boolean hasIdentifier(Lookahead lookahead) {
        STToken  nextToken = peek(lookahead.offset);
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            lookahead.offset++;
            return true;
        }
        return false;
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
            case DOUBLE_BACKTICK_TOKEN:
            case TRIPLE_BACKTICK_TOKEN:
            case CODE_CONTENT:
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
            return createMissingTokenWithDiagnostics(SyntaxKind.PARAMETER_NAME);
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
            return createMissingTokenWithDiagnostics(SyntaxKind.MINUS_TOKEN);
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
            return createMissingTokenWithDiagnostics(SyntaxKind.BACKTICK_TOKEN);
        }
    }

    /**
     * Get the genre of the reference type.
     *
     * @param referenceType Node that precedes the backtick block
     * @return Enum representing the genre
     */
    private ReferenceGenre getReferenceGenre(STNode referenceType) {
        if (referenceType == null) {
            return ReferenceGenre.NO_KEY;
        }

        if (referenceType.kind == SyntaxKind.FUNCTION_DOC_REFERENCE_TOKEN) {
            return ReferenceGenre.FUNCTION_KEY;
        }

        return ReferenceGenre.SPECIAL_KEY;
    }

    private STNode combineAndCreateCodeContentToken() {
        if (!isBacktickExprToken(peek().kind)) {
            return createMissingTokenWithDiagnostics(SyntaxKind.CODE_CONTENT);
        }

        StringBuilder backtickContent = new StringBuilder();
        STToken token;
        while (isBacktickExprToken(peek(2).kind)) {
            token = consume();
            backtickContent.append(token.toString());
        }
        token = consume();
        backtickContent.append(token.text());

        // We do not capture leading minutiae in DOCUMENTATION_BACKTICK_EXPR lexer mode.
        // Therefore, set only the trailing minutiae
        STNode leadingMinutiae = STNodeFactory.createEmptyNodeList();
        STNode trailingMinutiae = token.trailingMinutiae();
        return STNodeFactory.createLiteralValueToken(SyntaxKind.CODE_CONTENT, backtickContent.toString(),
                leadingMinutiae, trailingMinutiae);
    }

    private boolean isBacktickExprToken(SyntaxKind kind) {
        switch (kind) {
            case DOT_TOKEN:
            case COLON_TOKEN:
            case OPEN_PAREN_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case IDENTIFIER_TOKEN:
            case CODE_CONTENT:
                return true;
            default:
                return false;
        }
    }

    private STNode parseNameReferenceContent() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode identifier = consume();
            return parseBacktickExpr(identifier);
        }
        return parseNameReferenceContent();
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
            case OPEN_PAREN_TOKEN:
                return parseFuncCall(referenceName);
            default:
                // Since we have validated the token sequence beforehand, code should not reach here.
                throw new IllegalStateException("Unsupported token kind");
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
            return createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN);
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
            return createMissingTokenWithDiagnostics(SyntaxKind.OPEN_PAREN_TOKEN);
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
            return createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_PAREN_TOKEN);
        }
    }

    private STNode createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return SyntaxErrors.createMissingDocTokenWithDiagnostics(expectedKind);
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
