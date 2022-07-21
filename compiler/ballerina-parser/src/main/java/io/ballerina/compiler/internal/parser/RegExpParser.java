/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.util.Queue;

/**
 * An regular expression parser for ballerina.
 *
 * @since 2201.3.0
 */
public class RegExpParser extends AbstractParser {
    private final Queue<STNode> intepolationExprs;

    protected RegExpParser(AbstractTokenReader tokenReader, Queue<STNode> intepolationExprs) {
        super(tokenReader, new XMLParserErrorHandler(tokenReader));
        this.intepolationExprs = intepolationExprs;
    }

    @Override
    public STNode parse() {
        return parseRegExpContent();
    }

    private STNode parseRegExpContent() {
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfRegExpContent(nextToken.kind)) {
            STNode contentItem = parseRegExpContentItem();
            items.add(contentItem);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(items);
    }

    /**
     * Parse a single item in regular expression.
     *
     * @return Regular expression content item node
     */
    private STNode parseRegExpContentItem() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            return parseInterpolation();
        }

        // Template string component
        return consume();
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
     * Check whether the parser has reached the end of the back-tick literal.
     *
     * @param kind           Next token kind
     * @return <code>true</code> if this is the end of the back-tick literal. <code>false</code> otherwise
     */
    private boolean isEndOfRegExpContent(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
                return true;
            default:
                return false;
        }
    }

}
