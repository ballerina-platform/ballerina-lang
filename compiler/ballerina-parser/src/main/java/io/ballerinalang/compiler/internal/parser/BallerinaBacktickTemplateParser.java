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

import io.ballerinalang.compiler.internal.parser.BallerinaParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Ballerina back-tick template parser. Praser supports two kinds of templates:
 * <p>
 * <code>
 * string-template-expr := string BacktickString
 * <br/><br/>
 * xml-template-expr := xml BacktickString
 * </code>
 * <br/>
 * <br/>
 * <br/>
 * Content within the back-tick can consist of following:
 * <br/>
 * <br/>
 * <code>
 * BacktickString := ` BacktickItem* Dollar* `
 * <br/>
 * BacktickItem := BacktickSafeChar | BacktickDollarsSafeChar | Dollar* interpolation
 * <br/>
 * interpolation := ${ expression }
 * <br/>
 * BacktickSafeChar := ^ ( ` | $ )
 * <br/>
 * BacktickDollarsSafeChar :=  $+ ^ ( { | ` | $)
 * <br/>
 * Dollar := $
 * </code>
 * 
 * @since 1.3.0
 */
public class BallerinaBacktickTemplateParser extends BallerinaParser {

    private final BallerinaParserErrorHandler errorHandler;
    private final AbstractTokenReader tokenReader;

    public BallerinaBacktickTemplateParser(AbstractTokenReader tokenReader) {
        super(tokenReader);
        this.tokenReader = tokenReader;
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, this);
    }

    public STNode parse(STNode typeKeyword) {
        switch (typeKeyword.kind) {
            case XML_KEYWORD:
                return parseXML(typeKeyword);
            case STRING_KEYWORD:
            default:
                throw new UnsupportedOperationException();
        }
    }

    private STNode parseXML(STNode xmlKeyword) {
        this.tokenReader.startMode(ParserMode.XML_CONTENT);
        STNode startingBackTick = parseBacktickToken();
        STNode content = parseXMLContent();
        STNode endingBackTick = parseBacktickToken();
        return null;
    }

    private STNode parseXMLContent() {
        // this.tokenReader.switchMode(ParserMode.XML_CONTENT);
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

    private STNode parseXMLContentItem() {
        STToken nextToken = peek();
        STNode content = null;
        switch (nextToken.kind) {
            case LT_TOKEN:
                content = parseXMLElement();
                break;
            case XML_COMMENT_START_TOKEN:
                break;
            case XML_PI_START_TOKEN:
                break;
            case INTERPOLATION_START_TOKEN:
                content = parseInterpolation(ParserMode.XML_CONTENT);
                break;
            default:
                content = parseXMLText();
                break;
        }

        return content;
    }

    private STNode parseInterpolation(ParserMode currentMode) {
        STNode interpolStart = parseInterpolationStart();

        // switch to default mode and parse expressions.
        this.tokenReader.endMode();
        STNode expr = parseExpression();

        // Revert back the to previous mode
        this.tokenReader.startMode(currentMode);

        STNode closeBrace = parseCloseBrace();
        System.out.println(interpolStart + "" + String.valueOf(expr) +
                // " (" + expr == null ? "XML" : String.valueOf(expr.kind) + ")" +
                closeBrace);
        return null;
    }

    /**
     * Parse back-tick token.
     *
     * @return Back-tick token
     */
    private STNode parseBacktickToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BACKTICK_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.BACKTICK_TOKEN);
            return sol.recoveredNode;
        }
    }

    private STNode parseXMLElement() {
        STNode startTag = parseXMLElementStartTag();
        STNode content = parseXMLContent();
        STNode endTag = parseXMLElementEndTag();
        return null;
    }

    private STNode parseXMLElementStartTag() {
        STNode tagOpen = parseLTToken();
        STNode name = parseXMLNCName();
        STNode tagClose = parseGTToken();
        System.out.println(tagOpen + "" + name + "" + tagClose);
        return null;
    }

    private STNode parseXMLElementEndTag() {
        STNode tagOpen = parseLTToken();
        STNode slash = parseSlashToken();
        STNode name = parseXMLNCName();
        STNode tagClose = parseGTToken();
        System.out.println(tagOpen + "" + slash + "" + name + "" + tagClose);
        return null;
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

    private STNode parseXMLNCName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return parseQualifiedIdentifier(consume());
        } else {
            Solution sol = recover(token, ParserRuleContext.XML_NAME);
            return sol.recoveredNode;
        }
    }

    private STNode parseInterpolationStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.INTERPOLATION_START_TOKEN);
            return sol.recoveredNode;
        }
    }

    private STNode parseXMLText() {
        this.tokenReader.startMode(ParserMode.XML_TEXT);
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (true) {
            switch (nextToken.kind) {
                case INTERPOLATION_START_TOKEN:
                case EOF_TOKEN:
                case BACKTICK_TOKEN:
                case LT_TOKEN:
                    break;
                default:
                    STNode content = parseCharData();
                    items.add(content);
                    nextToken = peek();
                    continue;
            }

            break;
        }
        return STNodeFactory.createNodeList(items);
    }

    private STNode parseCharData() {
        STToken text = consume();
        System.out.println(text);
        return text;
    }
}
