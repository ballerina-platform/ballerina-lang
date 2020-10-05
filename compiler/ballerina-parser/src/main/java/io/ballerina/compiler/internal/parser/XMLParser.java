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

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * An XML parser for ballerina.
 * 
 * @since 2.0.0
 */
public class XMLParser extends AbstractParser {

    private final Queue<STNode> intepolationExprs;

    protected XMLParser(AbstractTokenReader tokenReader, Queue<STNode> intepolationExprs) {
        super(tokenReader, new XMLParserErrorHandler(tokenReader));
        this.intepolationExprs = intepolationExprs;
    }

    @Override
    public STNode parse() {
        return parseXMLContent();
    }

    /**
     * Parse XML content.
     * <p>
     * <code>
     * content :=  CharData? ((element | Reference | CDSect | PI | Comment | Interpolation ) CharData?)*
     * </code>
     * 
     * @return XML content node
     */
    private STNode parseXMLContent() {
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfXMLContent(nextToken.kind)) {
            STNode contentItem = parseXMLContentItem();
            items.add(contentItem);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(items);
    }

    /**
     * Check whether the parser has reached the end of the back-tick literal.
     * 
     * @param kind Next token kind
     * @return <code>true</code> if this is the end of the back-tick literal. <code>false</code> otherwise
     */
    private boolean isEndOfXMLContent(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
                return true;
            case LT_TOKEN:
                STToken nextNextToken = getNextNextToken(kind);
                if (nextNextToken.kind == SyntaxKind.SLASH_TOKEN || nextNextToken.kind == SyntaxKind.LT_TOKEN) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Parse a single item in the XML content. An item could be one of:
     * <ul>
     * <li>XML Element</li>
     * <li>XML CharData</li>
     * <li>XML Comment</li>
     * <li>XML PI</li>
     * <li>Interpolated expression</li>
     * </ul>
     * 
     * @return XML content item node
     */
    private STNode parseXMLContentItem() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case LT_TOKEN:
                return parseXMLElement();
            case XML_COMMENT_START_TOKEN:
                return parseXMLComment();
            case XML_PI_START_TOKEN:
                return parseXMLPI();
            case INTERPOLATION_START_TOKEN:
                return parseInterpolation();
            default:
                return parseXMLText();
        }
    }

    /**
     * Parse interpolation of a back-tick string.
     * <p>
     * <code>
     * interpolation := ${ expression }
     * </code>
     * 
     * @return Interpolation node
     */
    private STNode parseInterpolation() {
        // consume the injected interpolation start and end. i.e: &{}
        consume();
        consume();
        return this.intepolationExprs.remove();
    }

    /**
     * Parse XML element.
     * <p>
     * <code>
     * element := EmptyElemTag | STag content ETag 
     * </code>
     * 
     * @return
     */
    private STNode parseXMLElement() {
        STNode startTag = parseXMLElementStartOrEmptyTag();
        if (startTag.kind == SyntaxKind.XML_EMPTY_ELEMENT) {
            return startTag;
        }

        STNode content = parseXMLContent();
        STNode endTag = parseXMLElementEndTag();
        return STNodeFactory.createXMLElementNode(startTag, content, endTag);
    }

    /**
     * Parse XML element start-tag or empty-tag.
     * <p>
     * <code>STag := '<' Name (S Attribute)* S? '>'
     * <br/><br/>
     * Attribute := Name Eq AttValue
     * <br/><br/>
     * EmptyElemTag := '<' Name (S Attribute)* S? '/>'
     * </code>
     * 
     * @return XML element start tag
     */
    private STNode parseXMLElementStartOrEmptyTag() {
        startContext(ParserRuleContext.XML_START_OR_EMPTY_TAG);
        STNode tagOpen = parseLTToken();
        STNode name = parseXMLNCName();

        // Parse XML Attributes
        startContext(ParserRuleContext.XML_ATTRIBUTES);
        List<STNode> attributes = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfXMLAttributes(nextToken.kind)) {
            STNode attribute = parseXMLAttribute();
            if (attribute.kind == SyntaxKind.INTERPOLATION) {
                if (attributes.isEmpty()) {
                    name = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(name, attribute,
                            DiagnosticErrorCode.ERROR_INTERPOLATION_IS_NOT_ALLOWED_WITHIN_ELEMENT_TAGS);
                } else {
                    updateLastNodeInListWithInvalidNode(attributes, attribute,
                            DiagnosticErrorCode.ERROR_INTERPOLATION_IS_NOT_ALLOWED_WITHIN_ELEMENT_TAGS);
                }
            } else {
                attributes.add(attribute);
            }
            nextToken = peek();
        }
        endContext();

        STNode xmlAttributes = STNodeFactory.createNodeList(attributes);
        return parseXMLElementTagEnd(tagOpen, name, xmlAttributes);
    }

    private STNode parseXMLElementTagEnd(STNode tagOpen, STNode name, STNode attributes) {
        STToken nextToken = peek();
        return parseXMLElementTagEnd(nextToken.kind, tagOpen, name, attributes);
    }

    private STNode parseXMLElementTagEnd(SyntaxKind nextTokenKind, STNode tagOpen, STNode name, STNode attributes) {
        switch (nextTokenKind) {
            case SLASH_TOKEN:
                STNode slash = parseSlashToken();
                STNode tagClose = parseGTToken();
                endContext();
                return STNodeFactory.createXMLEmptyElementNode(tagOpen, name, attributes, slash, tagClose);
            case GT_TOKEN:
                tagClose = parseGTToken();
                endContext();
                return STNodeFactory.createXMLStartTagNode(tagOpen, name, attributes, tagClose);
            default:
                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.XML_START_OR_EMPTY_TAG_END, tagOpen, name, attributes);
                return parseXMLElementTagEnd(solution.tokenKind, tagOpen, name, attributes);
        }
    }

    /**
     * parse slash token.
     *
     * @return Parsed node
     */
    protected STNode parseSlashToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SLASH_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.SLASH);
            return parseSlashToken();
        }
    }

    /**
     * Parse XML element end tag.
     * <p>
     * <code>
     * ETag := '< /' Name S? '>'
     * </code>
     * 
     * @return XML element end tag
     */
    private STNode parseXMLElementEndTag() {
        startContext(ParserRuleContext.XML_END_TAG);
        STNode tagOpen = parseLTToken();
        STNode slash = parseSlashToken();
        STNode name = parseXMLNCName();
        STNode tagClose = parseGTToken();
        endContext();
        return STNodeFactory.createXMLEndTagNode(tagOpen, slash, name, tagClose);
    }

    /**
     * Parse 'less-than' token (<).
     *
     * @return Less-than token
     */
    private STNode parseLTToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LT_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.LT_TOKEN);
            return parseLTToken();
        }
    }

    /**
     * Parse 'greater-than' token (>).
     *
     * @return greater-than token
     */
    private STNode parseGTToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.GT_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.GT_TOKEN);
            return parseGTToken();
        }
    }

    /**
     * Parse XML name in non-canonicalized form.
     * 
     * @return XML name node
     */
    private STNode parseXMLNCName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return parseQualifiedIdentifier(consume());
        } else if (token.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            // If there's an interpolation parse it and report an error.
            STNode interpolation = parseInterpolation();
            // Then try to re-parse the same rule.
            STNode xmlNCName = parseXMLNCName();
            return SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(xmlNCName, interpolation,
                    DiagnosticErrorCode.ERROR_INTERPOLATION_IS_NOT_ALLOWED_FOR_XML_TAG_NAMES);
        } else {
            recover(token, ParserRuleContext.XML_NAME);
            return parseXMLNCName();
        }
    }

    /**
     * Parse identifier or qualified identifier, given the starting identifier.
     *
     * @param identifier Starting identifier
     * @return Parse node
     */
    protected STNode parseQualifiedIdentifier(STNode identifier) {
        STToken nextToken = peek(1);
        if (nextToken.kind != SyntaxKind.COLON_TOKEN) {
            return STNodeFactory.createXMLSimpleNameNode(identifier);
        }

        STToken nextNextToken = peek(2);
        if (nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STToken colon = consume();
            STNode varOrFuncName = STNodeFactory.createXMLSimpleNameNode(consume());
            identifier = STNodeFactory.createXMLSimpleNameNode(identifier);
            return STNodeFactory.createXMLQualifiedNameNode(identifier, colon, varOrFuncName);
        } else {
            addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
            return parseQualifiedIdentifier(identifier);
        }
    }

    /**
     * Check whether the parser has reached the end of the XML attributes.
     *
     * @param kind Next token kind
     * @return <code>true</code> if this is the end of the XML attributes. <code>false</code> otherwise
     */
    private boolean isEndOfXMLAttributes(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case GT_TOKEN:
            case LT_TOKEN:
            case SLASH_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse XML attribute.
     * <p>
     * <code>Attribute := Name Eq AttValue</code>
     * 
     * @return XML attribute node
     */
    private STNode parseXMLAttribute() {
        if (peek().kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            return parseInterpolation();
        }

        STNode attributeName = parseXMLNCName();
        STNode equalToken = parseAssignOp();
        STNode value = parseAttributeValue();
        return STNodeFactory.createXMLAttributeNode(attributeName, equalToken, value);
    }

    /**
     * Parse assign operator.
     *
     * @return Parsed node
     */
    private STNode parseAssignOp() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EQUAL_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.ASSIGN_OP);
            return parseAssignOp();
        }
    }

    /**
     * Parse XML attribute value.
     * <p>
     * <code>
     * AttValue := SingleQuotedValue |  DoubleQuotedValue
     * <br>
     * SingleQuotedValue := "'" ([^<&'] | Reference | interpolation)* "'"
     * <br>
     * DoubleQuotedValue := '"' ([^<&"] | Reference | interpolation)* '"'
     * <br>
     * </code>
     * 
     * @return XML attribute value node
     */
    private STNode parseAttributeValue() {
        STNode startQuote = parseStartQuote(ParserRuleContext.XML_QUOTE_START);
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfXMLAttributeValue(nextToken.kind)) {
            STNode contentItem = parseXMLCharacterSet();
            items.add(contentItem);
            nextToken = peek();
        }

        STNode value = STNodeFactory.createNodeList(items);
        STNode endQuote = parseStartQuote(ParserRuleContext.XML_QUOTE_END);

        return STNodeFactory.createXMLAttributeValue(startQuote, value, endQuote);
    }

    /**
     * Parse assign operator.
     *
     * @return Parsed node
     */
    private STNode parseStartQuote(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN || token.kind == SyntaxKind.SINGLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseStartQuote(ctx);
        }
    }

    private boolean isEndOfXMLAttributeValue(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case LT_TOKEN:
            case GT_TOKEN:
            case DOUBLE_QUOTE_TOKEN:
            case SINGLE_QUOTE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse XML text.
     * <p>
     * <code>
     * text := CharData?
     * <br/>
     * CharData :=  [^<&]* - ([^<&]* ']]>' [^<&]*)
     * </code>
     * 
     * @return XML text node
     */
    private STNode parseXMLText() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case INTERPOLATION_START_TOKEN:
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case LT_TOKEN:
                return null;
            default:
                STNode content = parseCharData();
                return STNodeFactory.createXMLTextNode(content);
        }
    }

    /**
     * parse XML char-data.
     * 
     * @return XML char-data token
     */
    private STNode parseCharData() {
        return consume();
    }

    /**
     * Parse XML comment.
     * <p>
     * <code>
     * Comment := '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     * </code>
     * 
     * @return XML comment node
     */
    private STNode parseXMLComment() {
        STNode commentStart = parseXMLCommentStart();
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfXMLComment(nextToken.kind)) {
            STNode contentItem = parseXMLCharacterSet();
            items.add(contentItem);
            nextToken = peek();
        }

        STNode content = STNodeFactory.createNodeList(items);
        STNode commentEnd = parseXMLCommentEnd();
        return STNodeFactory.createXMLComment(commentStart, content, commentEnd);
    }

    /**
     * Parse XML comment start.
     * 
     * @return XML comment start token
     */
    private STNode parseXMLCommentStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XML_COMMENT_START_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.XML_COMMENT_START);
            return parseXMLCommentStart();
        }
    }

    /**
     * Parse XML comment end.
     * 
     * @return XML comment end
     */
    private STNode parseXMLCommentEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XML_COMMENT_END_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.XML_COMMENT_END);
            return parseXMLCommentEnd();
        }
    }

    private boolean isEndOfXMLComment(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case LT_TOKEN:
            case GT_TOKEN:
            case XML_COMMENT_END_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse XML processing instruction node.
     * <p>
     * <code>
     * PI := '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
     * <br/>
     * PITarget := Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
     * </code>
     * 
     * @return XML processing instruction node
     */
    private STNode parseXMLPI() {
        startContext(ParserRuleContext.XML_PI);
        STNode piStart = parseXMLPIStart();
        STNode target = parseXMLNCName();

        STToken nextToken = peek();
        List<STNode> items = new ArrayList<>();
        while (!isEndOfXMLPI(nextToken.kind)) {
            STNode contentItem = parseXMLCharacterSet();
            items.add(contentItem);
            nextToken = peek();
        }

        STNode data = STNodeFactory.createNodeList(items);

        STNode piEnd = parseXMLPIEnd();
        endContext();
        return STNodeFactory.createXMLProcessingInstruction(piStart, target, data, piEnd);
    }

    /**
     * Parse XML PI start.
     * 
     * @return XML PI start token
     */
    private STNode parseXMLPIStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XML_PI_START_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.XML_PI_START);
            return parseXMLPIStart();
        }
    }

    /**
     * Parse XML PI end.
     * 
     * @return XML PI end token
     */
    private STNode parseXMLPIEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XML_PI_END_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.XML_PI_END);
            return parseXMLPIEnd();
        }
    }

    private boolean isEndOfXMLPI(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case LT_TOKEN:
            case GT_TOKEN:
            case XML_PI_END_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse character set token.
     * 
     * @return Character set token
     */
    private STNode parseXMLCharacterSet() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case XML_TEXT_CONTENT:
                return consume();
            case INTERPOLATION_START_TOKEN:
                return parseInterpolation();
            default:
                throw new IllegalStateException();
        }
    }
}
