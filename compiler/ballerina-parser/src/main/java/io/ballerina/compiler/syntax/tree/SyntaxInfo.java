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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.BallerinaLexer;
import io.ballerina.tools.text.CharReader;

import java.util.Arrays;
import java.util.List;

/**
 * A class that provides syntax related information.
 *
 * @since 2.0.0
 */
public final class SyntaxInfo {

    private static final List<String> BALLERINA_KEYWORDS = Arrays.stream(SyntaxKind.values())
            .filter(syntaxKind -> SyntaxKind.RE_KEYWORD.compareTo(syntaxKind) > 0)
            .map(SyntaxKind::stringValue)
            .toList();

    private SyntaxInfo() {
    }

    /**
     * Gives a list of all keywords in the ballerina.
     *
     * @return reserved keyword list
     */
    public static List<String> keywords() {
        return BALLERINA_KEYWORDS;
    }

    /**
     * Checks whether a given string is a ballerina keyword.
     *
     * @param text the string to check
     * @return {@code true}, if the input is a ballerina keyword. {@code false} otherwise
     */
    public static boolean isKeyword(String text) {
        return BALLERINA_KEYWORDS.contains(text);
    }

    /**
     * Checks whether a given string is a valid ballerina identifier.
     * <p>
     * <a href="https://ballerina.io/spec/lang/2021R1/#identifier">identifier</a> :=
     * UnquotedIdentifier | QuotedIdentifier
     * `<br><br/>
     * <i>Note: Ballerina keywords are not considered as valid identifiers here</i>
     *
     * @param text the string to check
     * @return {@code true}, if the input is a ballerina identifier. {@code false} otherwise.
     */
    public static boolean isIdentifier(String text) {
        BallerinaLexer ballerinaLexer = new BallerinaLexer(CharReader.from(text));
        Token token = ballerinaLexer.nextToken().createUnlinkedFacade();
        return token.kind() == SyntaxKind.IDENTIFIER_TOKEN && !token.hasDiagnostics() && text.equals(token.text());
    }
}
