/*
 *   Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.json;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests that {@code value:fromJsonString} rejects JSON string escapes containing unpaired
 * (lone) UTF-16 surrogate characters, instead of silently producing an invalid {@code string}
 * value.
 */
public class FromJsonStringSurrogateTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/json-test.bal");
    }

    @Test(description = "Test fromJsonString with a lone high surrogate escape")
    public void testLoneHighSurrogateEscapeIsRejected() {
        Object[] args = {StringUtils.fromString("\"abcd\\uD800\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a lone low surrogate escape")
    public void testLoneLowSurrogateEscapeIsRejected() {
        Object[] args = {StringUtils.fromString("\"abcd\\uDC00\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a lone high surrogate followed by an ordinary character")
    public void testLoneHighSurrogateFollowedByOrdinaryCharIsRejected() {
        Object[] args = {StringUtils.fromString("\"abcd\\uD800e\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a lone surrogate in an object field name")
    public void testLoneSurrogateInFieldNameIsRejected() {
        Object[] args = {StringUtils.fromString("{\"ab\\uD800cd\": 1}")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a lone surrogate in an array element")
    public void testLoneSurrogateInArrayElementIsRejected() {
        Object[] args = {StringUtils.fromString("[\"abcd\\uD800\"]")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a lone surrogate in an object field value")
    public void testLoneSurrogateInObjectFieldValueIsRejected() {
        Object[] args = {StringUtils.fromString("{\"a\": \"abcd\\uD800\"}")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        assertParserError(returns);
    }

    @Test(description = "Test fromJsonString with a valid surrogate pair escape (control case)")
    public void testValidSurrogatePairEscapeIsAccepted() {
        Object[] args = {StringUtils.fromString("\"abcd\\uD83D\\uDE00\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "abcd😀");
    }

    @Test(description = "Test fromJsonString with an escaped backslash followed by literal text (control case)")
    public void testEscapedBackslashFollowedByLiteralUTextIsAccepted() {
        Object[] args = {StringUtils.fromString("\"abcd\\\\uD800\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "abcd\\uD800");
    }

    private void assertParserError(Object returns) {
        Assert.assertTrue(returns instanceof BError, "expected an error, found: " + returns);
        String errorMsg =
                (((BMap<?, ?>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))).toString();
        Assert.assertTrue(errorMsg.contains("unpaired surrogate"), "unexpected error message: " + errorMsg);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
