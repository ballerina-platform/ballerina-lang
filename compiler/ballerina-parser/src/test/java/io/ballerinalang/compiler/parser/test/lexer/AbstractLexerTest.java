/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.lexer;

import io.ballerina.compiler.internal.parser.BallerinaLexer;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.tools.text.CharReader;

/**
 * An abstract class that contains utilities for {@code BallerinaLexer} tests.
 *
 * @since 2.0.0
 */
public class AbstractLexerTest {

    public STToken lexToken(String sourceText) {
        CharReader charReader = CharReader.from(sourceText);
        BallerinaLexer lexer = new BallerinaLexer(charReader);
        return lexer.nextToken();
    }
}
