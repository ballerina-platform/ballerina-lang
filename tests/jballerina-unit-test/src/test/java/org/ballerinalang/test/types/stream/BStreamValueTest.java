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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test stream type.
 *
 * @since 1.2.0
 */
public class BStreamValueTest {

    private CompileResult result, negativeResult, incompleteTypeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/stream/stream-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/stream/stream-negative.bal");
        incompleteTypeResult = BCompileUtil.compile("test-src/types/stream/stream-type-incomplete-definition.bal");
    }

    @Test(description = "Test global stream construct")
    public void testGlobalStreamConstruct() {
        Object values = BRunUtil.invoke(result, "testGlobalStreamConstruct", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream construct within a function")
    public void testStreamConstruct() {
        Object values = BRunUtil.invoke(result, "testStreamConstruct", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream construct with stream filter")
    public void testStreamConstructWithFilter() {
        Object values = BRunUtil.invoke(result, "testStreamConstructWithFilter", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream explicit return type")
    public void testStreamReturnTypeExplicit() {
        Object values = BRunUtil.invoke(result, "testStreamReturnTypeExplicit", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream implicit return type")
    public void testStreamReturnTypeImplicit() {
        Object values = BRunUtil.invoke(result, "testStreamReturnTypeImplicit", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test Iterator With Custom Error")
    public void testIteratorWithCustomError() {
        Object values = BRunUtil.invoke(result, "testIteratorWithCustomError", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test Iterator with generic error")
    public void testIteratorWithGenericError() {
        Object values = BRunUtil.invoke(result, "testIteratorWithGenericError", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test Iterator with withOut error")
    public void testIteratorWithOutError() {
        Object values = BRunUtil.invoke(result, "testIteratorWithOutError", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test Iterator with error union")
    public void testIteratorWithErrorUnion() {
        Object values = BRunUtil.invoke(result, "testIteratorWithErrorUnion", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream construct within never")
    public void testStreamConstructWithNil() {
        Object values = BRunUtil.invoke(result, "testStreamConstructWithNil", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test stream of streams")
    public void testStreamOfStreams() {
        Object values = BRunUtil.invoke(result, "testStreamOfStreams", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test empty stream constructs")
    public void testEmptyStreamConstructs() {
        Object values = BRunUtil.invoke(result, "testEmptyStreamConstructs", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test passing union of stream to a function which takes a stream as a argument")
    public void testUnionOfStreamsAsFunctionParams() {
        Object values = BRunUtil.invoke(result, "testUnionOfStreamsAsFunctionParams", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test passing a stream constructor to create an unbounded stream")
    public void testUnboundedStreams() {
        BRunUtil.invoke(result, "testUnboundedStreams", new Object[]{});
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
                "|}|CustomError)?', found '(record {| int value; |}|error)?'", 109, 49);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| int value; " +
                "|}|CustomError)?', found '(record {| int value; |}|error)?'", 110, 45);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 113, 35);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| int value; |}?', " +
                "found '(record {| int value; |}|error)?'", 114, 31);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns record {| int value; |}?; }', but found " +
                "'IteratorWithOutNext'", 159, 35);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns record {| int value; |}?; }', but found " +
                "'IteratorWithOutNext'", 160, 31);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithOutNext'", 161, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithOutNext'", 162, 39);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|CustomError)?; }', but found " +
                "'IteratorWithOutNext'", 163, 49);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|CustomError)?; }', but found " +
                "'IteratorWithOutNext'", 164, 45);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns record {| int value; |}?; }', but found " +
                "'IteratorWithMismatchedNextA'", 178, 35);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns record {| int value; |}?; }', but found " +
                "'IteratorWithMismatchedNextA'", 179, 31);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextA'", 180, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextA'", 181, 39);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|CustomError)?; }', but found " +
                "'IteratorWithMismatchedNextA'", 182, 49);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|CustomError)?; }', but found " +
                "'IteratorWithMismatchedNextA'", 183, 45);
        BAssertUtil.validateError(negativeResult, i++, "incompatible mapping constructor expression for type '" +
                "(record {| int val; |}|CustomError)?'", 191, 16);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 198, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 199, 39);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| string value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 202, 46);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| string value; |}|error)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 203, 42);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| string value; |}|CustomError)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 204, 52);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| string value; |}|CustomError)?; }', but found " +
                "'IteratorWithMismatchedNextC'", 205, 48);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| string value; " +
                "|}|CustomError1)?', found '(record {| int value; |}|CustomError)?'", 227, 53);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(record {| string value; " +
                "|}|CustomError1)?', found '(record {| int value; |}|CustomError)?'", 228, 49);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 246, 27);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 247, 27);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 248, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 249, 35);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 250, 34);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 251, 35);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 252, 19);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 253, 19);
        BAssertUtil.validateError(negativeResult, i++, "'new(itr, itr)' is not a valid constructor for streams type"
                , 254, 19);
        BAssertUtil.validateError(negativeResult, i++, "invalid next method return type. expected: 'record {| int " +
                "value; |}?'", 324, 7);
        BAssertUtil.validateError(negativeResult, i++, "invalid next method return type. expected: 'record {| " +
                "stream<int> value; |}?'", 332, 7);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; }', but found " +
                "'IteratorWithNonIsolatedNext'", 350, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; public isolated function " +
                "close() returns error?; }', but found 'IteratorWithNonIsolatedNextAndIsolatedClose'", 351, 43);
        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype of 'object { " +
                "public isolated function next() returns (record {| int value; |}|error)?; public isolated function " +
                "close() returns error?; }', but found 'IteratorWithIsolatedNextAndNonIsolatedClose'", 352, 43);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 371, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 372, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 373, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 375, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 376, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'stream<int>', found " +
                "'stream<int,error?>'", 377, 20);

        BAssertUtil.validateError(negativeResult, i++, "invalid stream constructor. expected a subtype " +
                "of 'object { public isolated function next() returns record {| int value; |}?; }', but found " +
                "'string'", 381, 31);
        BAssertUtil.validateError(negativeResult, i++, "type 'readonly' not allowed here; " +
                        "expected an 'error' or a subtype of 'error'", 387, 31);
        BAssertUtil.validateError(negativeResult, i++, "no stream constructor provided. " +
                "expected a subtype of 'object { public isolated function next() " +
                "returns (record {| int value; |}|error); }'", 389, 28);

        Assert.assertEquals(i, negativeResult.getErrorCount());

    }

    @Test(description = "Test incomplete stream definition")
    public void testIncompleteTypeResult() {
        int i = 0;
        BAssertUtil.validateError(incompleteTypeResult, i++, "missing type descriptor", 17, 15);
        Assert.assertEquals(i, incompleteTypeResult.getErrorCount());
    }

    @Test(description = "Check if completion type is checked at runtime",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types: 'stream<Foo>' cannot be "
                            + "cast to 'stream<Foo,error>'.*")
    public void testInvalidCast() {
        BRunUtil.invoke(result, "testInvalidCast");
    }

    @Test(description = "Check if stream without params contextually expected type",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina}TypeCastError " +
                            "\\{\"message\":\"incompatible types: 'stream<\\(any\\|error\\),error\\?>' cannot be cast" +
                            " to 'stream<int,error\\?>'.*")
    public void testCastingFromSuperStreamType() {
        BRunUtil.invoke(result, "testCastingFromSuperStreamType");
    }


    @Test(description = "Test basic stream type variables")
    public void testBasicStreamType() {
        BRunUtil.invoke(result, "testBasicStreamType");
    }

    @Test(description = "Test basic stream type variables")
    public void testErrorStreamTypeAssignedToStreamWithoutParams() {
        BRunUtil.invoke(result, "testErrorStreamTypeAssignedToStreamWithoutParams");
    }

    @Test(description = "Test basic stream type variables")
    public void testImplicitNewExprToStreamWithoutParams() {
        BRunUtil.invoke(result, "testImplicitNewExprToStreamWithoutParams");
    }

    @Test(description = "Test using referred stream type")
    public void testStreamsTypeAsTypeReference() {
        BRunUtil.invoke(result, "testStreamsTypeAsTypeReference");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        incompleteTypeResult = null;
    }

}
