/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.intersection;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for intersection types.
 *
 * @since 1.3.0
 */
@Ignore
public class IntersectionTypeTest {

    private CompileResult readOnlyIntersectionResults;
    private CompileResult errorIntersectionResults;

    @BeforeClass
    public void setup() {
        readOnlyIntersectionResults = BCompileUtil.compile("test-src/types/intersection/test_intersection_type.bal");
        errorIntersectionResults = BCompileUtil.compile("test-src/types/intersection/error_intersection_type.bal");
    }

    @Test
    public void testImmutableTypes() {
        BRunUtil.invoke(readOnlyIntersectionResults, "testIntersectionTypes");
    }

    @Test
    public void testReadOnlyIntersectionFieldInRecord() {
        BRunUtil.invoke(readOnlyIntersectionResults, "testReadOnlyIntersectionFieldInRecord");
    }

    @Test
    public void testRecursiveReadonlyIntersection() {
        BRunUtil.invoke(readOnlyIntersectionResults, "testRecursiveReadonlyIntersection");
    }

    @Test
    public void testRuntimeTypeNameOfIntersectionType() {
        BRunUtil.invoke(readOnlyIntersectionResults, "testRuntimeTypeNameOfIntersectionType");
    }

    @Test
    public void testImmutableTypesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/intersection/test_intersection_type_negative.bal");
        int index = 0;

        validateError(result, index++, "invalid intersection type with 'readonly', 'future<int>' can never be " +
                "'readonly'", 19, 5);
        validateError(result, index++, "unsupported intersection 'json & int'", 23, 5);
        validateError(result, index++, "invalid intersection type '(Bar & readonly)': no intersection", 26,
                      45);
        validateError(result, index++, "invalid intersection type '(Baz & readonly)': no intersection", 32,
                      45);
        validateError(result, index++, "incompatible types: 'Y' is not a record", 42, 6);

        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testErrorIntersectionWithExistingDetail() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionForExistingDetail");
    }

    @Test
    public void testErrorIntersectionWithExistingAndNewDetail() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionForExisitingAndNewDetail");
    }

    @Test
    public void testErrorIntersectionWithDetailRecordAndMap() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionForDetailRecordAndDetailMap");
    }

    @Test
    public void testDistinctErrorIntersection() {
        BRunUtil.invoke(errorIntersectionResults, "testDistinctIntersectionType");
    }

    @Test
    public void testIntersectionOfDistinctErrors() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionOfDistinctErrors");
    }

    @Test
    public void testIntersectionAsFieldInRecord() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionAsFieldInRecord");
    }

    @Test
    public void testIntersectionAsFieldInAnonymousRecord() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionAsFieldInAnonymousRecord");
    }

    @Test
    public void testIntersectionOfErrorWithInlineError() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionOfErrorWithInlineError");
    }

    @Test
    public void testAnonDistinctError() {
        BRunUtil.invoke(errorIntersectionResults, "testAnonDistinctError");
    }

    @Test
    public void testIntersectionOfSameSetOfErrorShapes() {
        BRunUtil.invoke(errorIntersectionResults, "testIntersectionOfSameSetOfErrorShapes");
    }

    @Test
    public void testDistinctErrorWithSameTypeIdsButDifferentTypes() {
        BRunUtil.invoke(errorIntersectionResults, "testDistinctErrorWithSameTypeIdsButDifferentTypes");
    }

    @Test
    public void testSingleErrorIntersectionWithReadOnly() {
        BRunUtil.invoke(errorIntersectionResults, "testSingleErrorIntersectionWithReadOnly");
    }

    @Test
    public void testMultipleErrorIntersectionWithReadOnly() {
        BRunUtil.invoke(errorIntersectionResults, "testMultipleErrorIntersectionWithReadOnly");
    }

    @Test
    public void testErrorIntersectionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/intersection/error_intersection_type_negative.bal");

        int index = 0;
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorTwo': no intersection", 45, 24);
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorThree': no intersection", 47, 27);
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorFour': no intersection", 49, 29);
        validateError(result, index++, "error constructor does not accept additional detail args 'z' when " +
                        "error detail type 'record {| string x; string...; |}' contains individual field descriptors",
                56, 63);
        validateError(result, index++,
                      "incompatible types: expected 'DistinctErrorIntersection', found 'IntersectionErrorFour'", 57,
                      38);
        validateError(result, index++,
                "invalid intersection type 'ErrorX & ErrorY': no intersection", 72, 33);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 72, 33);
        validateError(result, index++,
                "invalid intersection type 'error<DetailX> & error<DetailY>': no intersection", 74, 39);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 74, 39);
        validateError(result, index++,
                "invalid intersection type 'error<DetailY> & error<DetailX> & ErrorX': no intersection", 76, 44);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 76, 61);
        validateError(result, index++,
                "invalid intersection type 'error & ErrorX': no intersection", 78, 35);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 78, 43);
        validateError(result, index++,
                "invalid intersection type 'ErrorX & error': no intersection", 79, 36);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 79, 36);
        validateError(result, index++,
                "invalid intersection type 'E & ErrorX': no intersection", 82, 22);
        validateError(result, index++,
                "invalid intersection: field 'x' contains a default value in type 'DetailX'", 82, 26);
        validateError(result, index++, "missing error detail arg for error detail field 'code'", 92, 39);
        validateError(result, index++, "missing error detail arg for error detail field 'code'", 93, 48);
        validateError(result, index++, "error constructor does not accept additional detail args 'c' when error " +
                "detail type 'record {| int code; anydata...; |}' contains individual field descriptors", 93, 60);
        validateError(result, index++, "missing error detail arg for error detail field 'code'", 94, 47);
        validateError(result, index++, "missing error detail arg for error detail field 'code'", 95, 47);
        validateError(result, index++, "missing error detail arg for error detail field 'code'", 96, 47);
        validateError(result, index++, "error constructor does not accept additional detail args 'oth' when error " +
                "detail type 'record {| boolean fatal?; int code; int...; |}' contains individual field descriptors",
                96, 59);

        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testDistinctErrorIntersectionNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/types/intersection/distinct_error_intersection_negative.bal");

        int index = 0;
        validateError(result, index++,
                "incompatible types: expected 'SingleDistinctError2', found 'SingleDistinctError'", 28, 32);
        validateError(result, index++,
                "incompatible types: expected 'DoubleDistinctError', found 'SingleDistinctError'", 29, 30);
        validateError(result, index++,
                "incompatible types: expected 'SingleDistinctError', found 'DoubleDistinctError'", 30, 10);
        validateError(result, index++,
                "incompatible types: expected 'DoubleDistinctError2', found 'DoubleDistinctError'", 31, 32);
        validateError(result, index++,
                "incompatible types: expected 'DistinctErrorAndSingleDistinctError', found 'SingleDistinctError'",
                35, 11);
        validateError(result, index++,
                "incompatible types: expected 'DistinctErrorAndSingleDistinctError2', found 'SingleDistinctError'",
                37, 48);
        validateError(result, index++,
                "incompatible types: expected 'DistinctErrorAndSingleDistinctError', " +
                        "found 'DistinctErrorAndSingleDistinctError2'", 39, 11);
        validateError(result, index++,
                "incompatible types: expected 'error<map<ballerina/lang.value:0.0.0:Cloneable>>', " +
                        "found 'SingleDistinctError'", 41, 31);
        validateError(result, index++,
                "incompatible types: expected 'SingleDistinctError', " +
                        "found 'error<map<ballerina/lang.value:0.0.0:Cloneable>>'", 42, 10);

        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testUnsupportedIntersectionNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/types/intersection/unsupported_intersection_negative.bal");
        int index = 0;
        validateError(result, index++, "unsupported intersection 'int & string'", 17, 8);
        validateError(result, index++, "unsupported intersection 'int & int'", 18, 9);
        validateError(result, index++,
                "unsupported intersection 'function()returns(int) & function()returns(2 | 3)'", 19, 9);
        validateError(result, index++, "unsupported intersection 'int & int'", 21, 1);
        validateError(result, index++, "unknown type 'A'", 23, 14);
        validateError(result, index++, "unknown type 'II'", 23, 19);
        validateError(result, index++, "unsupported intersection 'int & int'", 23, 25);
        assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        readOnlyIntersectionResults = null;
        errorIntersectionResults = null;
    }
}
