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

import io.ballerina.compiler.internal.parser.tree.STToken;
import org.testng.annotations.Test;

/**
 * This class contains cases to test the newline normalization logic in the {@code BallerinaLexer}.
 *
 * @since 2.0.0
 */
public class NewlineNormalizationTest extends AbstractLexerTest {

    @Test(description = "Tests the presence of \n character")
    public void testUnixNewlineChars() {
        STToken token = lexToken("sameera\r\n");
    }

    @Test(description = "Tests the presence of \r\n characters")
    public void testWindowsNewlineChars() {

    }

    @Test(description = "Tests the presence of \r character")
    public void testOldMacNewlineChars() {

    }
}
