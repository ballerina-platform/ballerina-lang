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

import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

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

    @Override
    public STNode resumeParsing(ParserRuleContext context, Object... args) {
        switch(context) {
            default:
                throw new IllegalStateException("cannot resume parsing the rule: " + context);
        }
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
                if (nextNextToken.kind == SyntaxKind.SLASH_TOKEN) {
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
                return null;
            case XML_PI_START_TOKEN:
                return null;
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
        STNode tagOpen = parseLTToken();
        STNode name = parseXMLNCName();
        STNode attributes = parseXMLAttributes();

        if (peek().kind == SyntaxKind.SLASH_TOKEN) {
            STNode slash = parseSlashToken();
            STNode tagClose = parseGTToken();
            return STNodeFactory.createXMLEmptyElementNode(tagOpen, attributes, slash, tagClose);

        }

        STNode tagClose = parseGTToken();
        return STNodeFactory.createXMLStartTagNode(tagOpen, name, attributes, tagClose);
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
            Solution sol = recover(token, ParserRuleContext.SLASH);
            return sol.recoveredNode;
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
        STNode tagOpen = parseLTToken();
        STNode slash = parseSlashToken();
        STNode name = parseXMLNCName();
        STNode tagClose = parseGTToken();
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
            Solution sol = recover(token, ParserRuleContext.LT_TOKEN);
            return sol.recoveredNode;
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
            Solution sol = recover(token, ParserRuleContext.GT_TOKEN);
            return sol.recoveredNode;
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
        } else {
            Solution sol = recover(token, ParserRuleContext.XML_NAME);
            return sol.recoveredNode;
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
            return STNodeFactory.createSimpleNameReferenceNode(identifier);
        }

        STToken nextNextToken = peek(2);
        if (nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STToken colon = consume();
            STToken varOrFuncName = consume();
            return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
        } else {
            this.errorHandler.removeInvalidToken();
            return parseQualifiedIdentifier(identifier);
        }
    }

    /**
     * Parse XML attributes.
     * 
     * @return XML attributes
     */
    private STNode parseXMLAttributes() {
        List<STNode> attributes = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfXMLAttributes(nextToken.kind)) {
            STNode attribute = parseXMLAttribute();
            attributes.add(attribute);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(attributes);
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
        STNode attributeName = parseXMLNCName();
        STNode equalToken = parseAssignOp();
        STNode value = parseXMLQuotedString();
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
            Solution sol = recover(token, ParserRuleContext.ASSIGN_OP);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse XML Quoted String.
     * 
     * @return XML quoted string
     */
    private STNode parseXMLQuotedString() {
        STToken token = peek();
        if (token.kind == SyntaxKind.STRING_LITERAL) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.XML_QUOTED_STRING);
            return sol.recoveredNode;
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
}
