/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.editor;

import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.ballerinalang.plugins.idea.BallerinaFileType;

/**
 * Brace matcher tests.
 */
public class BallerinaBraceMatcherTest extends BallerinaCodeInsightFixtureTestCase {

    public void testCloseParenthesis() {
        myFixture.configureByText(BallerinaFileType.INSTANCE, "function test<caret>");
        myFixture.type('(');
        myFixture.checkResult("function test(<caret>)");
    }

    public void testCloseBrace() {
        myFixture.configureByText(BallerinaFileType.INSTANCE, "function test()<caret>");
        myFixture.type('{');
        myFixture.checkResult("function test(){<caret>}");
    }

    public void testCloseBracket() {
        myFixture.configureByText(BallerinaFileType.INSTANCE, "int<caret>");
        myFixture.type('[');
        myFixture.checkResult("int[<caret>]");
    }

    public void testCloseBraceInsideDefinition() {
        myFixture.configureByText(BallerinaFileType.INSTANCE, "function test(){try<caret>}");
        myFixture.type('{');
        myFixture.checkResult("function test(){try{<caret>}}");
    }
}
