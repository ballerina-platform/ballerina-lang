/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.test.api;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.json.JsonParser;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests that {@link JsonParser} rejects JSON string escapes containing unpaired (lone) UTF-16
 * surrogate characters, instead of silently producing an invalid {@code string} value.
 */
public class JsonParserSurrogateTest {

    @Test
    public void testLoneHighSurrogateEscapeIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("\"abcd\\uD800\""));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }

    @Test
    public void testLoneLowSurrogateEscapeIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("\"abcd\\uDC00\""));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }

    @Test
    public void testLoneHighSurrogateFollowedByOrdinaryCharIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("\"abcd\\uD800e\""));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }

    @Test
    public void testValidSurrogatePairEscapeIsAccepted() {
        Object result = JsonParser.parse("\"abcd\\uD83D\\uDE00\"");
        Assert.assertTrue(result instanceof BString);
        Assert.assertEquals(((BString) result).getValue(), "abcd😀");
    }

    @Test
    public void testEscapedBackslashFollowedByLiteralUTextIsAccepted() {
        Object result = JsonParser.parse("\"abcd\\\\uD800\"");
        Assert.assertTrue(result instanceof BString);
        Assert.assertEquals(((BString) result).getValue(), "abcd\\uD800");
    }

    @Test
    public void testLoneSurrogateInFieldNameIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("{\"ab\\uD800cd\": 1}"));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }

    @Test
    public void testLoneSurrogateInArrayElementIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("[\"abcd\\uD800\"]"));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }

    @Test
    public void testLoneSurrogateInObjectFieldValueIsRejected() {
        BError error = Assert.expectThrows(BError.class, () -> JsonParser.parse("{\"a\": \"abcd\\uD800\"}"));
        Assert.assertTrue(error.getMessage().contains("unpaired surrogate"),
                "unexpected error message: " + error.getMessage());
    }
}
