/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
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
package org.ballerinalang.test.types.stream;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test stream type.
 *
 * @since 1.2.0
 */
public class BStreamValueTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/stream/stream-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/stream/stream-negative.bal");
    }

    @Test(description = "Test global stream construct")
    public void testGlobalStreamConstruct() {
        BValue[] values = BRunUtil.invoke(result, "testGlobalStreamConstruct", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream construct within a function")
    public void testStreamConstruct() {
        BValue[] values = BRunUtil.invoke(result, "testStreamConstruct", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream construct with stream filter")
    public void testStreamConstructWithFilter() {
        BValue[] values = BRunUtil.invoke(result, "testStreamConstructWithFilter", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream explicit return type")
    public void testStreamReturnTypeExplicit() {
        BValue[] values = BRunUtil.invoke(result, "testStreamReturnTypeExplicit", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream implicit return type")
    public void testStreamReturnTypeImplicit() {
        BValue[] values = BRunUtil.invoke(result, "testStreamReturnTypeImplicit", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test Iterator With Custom Error")
    public void testIteratorWithCustomError() {
        BValue[] values = BRunUtil.invoke(result, "testIteratorWithCustomError", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test Iterator with generic error")
    public void testIteratorWithGenericError() {
        BValue[] values = BRunUtil.invoke(result, "testIteratorWithGenericError", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test Iterator with withOut error")
    public void testIteratorWithOutError() {
        BValue[] values = BRunUtil.invoke(result, "testIteratorWithOutError", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test Iterator with error union")
    public void testIteratorWithErrorUnion() {
        BValue[] values = BRunUtil.invoke(result, "testIteratorWithErrorUnion", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream construct within never")
    public void testStreamConstructWithNever() {
        BValue[] values = BRunUtil.invoke(result, "testStreamConstructWithNever", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test stream of streams")
    public void testStreamOfStreams() {
        BValue[] values = BRunUtil.invoke(result, "testStreamOfStreams", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test empty stream constructs")
    public void testEmptyStreamConstructs() {
        BValue[] values = BRunUtil.invoke(result, "testEmptyStreamConstructs", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test negative test scenarios of stream type")
    public void testStreamTypeNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 74, 45);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|CustomError)?'", 77, 35);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|CustomError)?'", 78, 31);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| int value; " +
                "|}|CustomError)?', found '(record {| int value; |}|error)?'", 105, 57);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 106, 45);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| int value; " +
                "|}|CustomError)?', found '(record {| int value; |}|error)?'", 109, 48);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| int value; " +
                "|}|CustomError)?', found '(record {| int value; |}|error)?'", 110, 44);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 113, 35);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 114, 31);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not return an error",
                137, 42);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not return an error",
                138, 38);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not return an error",
                139, 48);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not return an error",
                140, 44);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns record {| int value; |}?'.", 159, 35);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns record {| int value; |}?'.", 160, 31);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns (record {| int value; |}|error)?'.", 161, 42);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns (record {| int value; |}|error)?'.", 162, 38);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns (record {| int value; |}|CustomError)?'.", 163, 48);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithOutNext' must implement 'public function next() " +
                "returns (record {| int value; |}|CustomError)?'.", 164, 44);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns record {| int value; |}?'.", 178, 35);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns record {| int value; |}?'.", 179, 31);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns (record {| int value; |}|error)?'.", 180, 42);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns (record {| int value; |}|error)?'.", 181, 38);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns (record {| int value; |}|CustomError)?'.", 182, 48);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextA' must implement 'public function" +
                " next() returns (record {| int value; |}|CustomError)?'.", 183, 44);
        BAssertUtil.validateError(negativeResult, i++, "incompatible mapping constructor expression for type '(record" +
                " {| int val; |}|CustomError)?'", 191, 16);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| int value; |}|error)?'.", 198, 42);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| int value; |}|error)?'.", 199, 38);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| string value; |}|error)?'.", 202, 45);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| string value; |}|error)?'.", 203, 41);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| string value; |}|CustomError)?'.", 204, 51);
        BAssertUtil.validateError(negativeResult, i++, "'IteratorWithMismatchedNextC' must implement 'public function" +
                " next() returns (record {| string value; |}|CustomError)?'.", 205, 47);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| string value; " +
                "|}|CustomError1)?', found '(record {| int value; |}|CustomError)?'", 227, 52);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| string value; " +
                "|}|CustomError1)?', found '(record {| int value; |}|CustomError)?'", 228, 48);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not " +
                "return an error", 239, 48);
        BAssertUtil.validateError(negativeResult, i++, "invalid expected stream type. 'itr' does not " +
                "return an error", 240, 44);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                246, 27);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                247, 27);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                248, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                249, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                250, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                251, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                252, 19);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type",
                253, 19);
        BAssertUtil.validateError(negativeResult, i, "'new(itr, itr)' is not a valid constructor for streams type",
                254, 19);
    }

}
