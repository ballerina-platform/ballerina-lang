/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.misc;

import io.ballerina.compiler.syntax.SyntaxInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains test cases to test the {@code SyntaxInfo} API.
 *
 * @since 2.0.0
 */
public class SyntaxInfoAPITest {

    @Test
    public void testIsBallerinaKeyword() {
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("public"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("fail"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("function"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("join"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("resource"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("var"));
        Assert.assertTrue(SyntaxInfo.isBallerinaKeyword("!is"));

        Assert.assertFalse(SyntaxInfo.isBallerinaKeyword("{"));
        Assert.assertFalse(SyntaxInfo.isBallerinaKeyword("@"));
        Assert.assertFalse(SyntaxInfo.isBallerinaKeyword("module"));
        Assert.assertFalse(SyntaxInfo.isBallerinaKeyword("variable"));
    }

    @Test
    public void testIsIdentifier() {
        Assert.assertTrue(SyntaxInfo.isIdentifier("foo"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("foo123Bar"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("is\\ keyword"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("'foo\\ \\/\\:\\@\\[\\`\\{\\~"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("foo\\ \\/\\:\\@\\[\\`\\{\\~"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("'var_\\ \\/\\:\\@\\[\\`\\{\\~"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("var_\\ \\/\\:\\@\\[\\`\\{\\~"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("\\u{1324}\\u{0387}_var"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("'සිංහල\\ වචනය"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("සිංහල\\ වචනය"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("foo_ƮέŞŢ"));
        Assert.assertTrue(SyntaxInfo.isIdentifier("üňĩćőđę_var"));

        Assert.assertFalse(SyntaxInfo.isIdentifier(""));
        Assert.assertFalse(SyntaxInfo.isIdentifier("'"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("is Identifier"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("\\"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("\\a"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("@"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("123"));
        Assert.assertFalse(SyntaxInfo.isIdentifier("function"));
    }
}
