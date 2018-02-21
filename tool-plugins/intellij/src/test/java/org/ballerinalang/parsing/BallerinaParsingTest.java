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

package org.ballerinalang.parsing;

import com.intellij.testFramework.ParsingTestCase;
import org.ballerinalang.plugins.idea.BallerinaParserDefinition;

/**
 * Parsing tests.
 */
public class BallerinaParsingTest extends ParsingTestCase {

    public BallerinaParsingTest() {
        super("", "bal", new BallerinaParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData/parsing";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }

    public void testEchoService() {
        doTest(true);
    }

    public void testHelloWorld() {
        doTest(true);
    }

    public void testHelloWorldService() {
        doTest(true);
    }

    public void testTweetMediumFeed() {
        doTest(true);
    }

    public void testTweetOpenPR() {
        doTest(true);
    }

    public void testTwitterConnector() {
        doTest(true);
    }
}
