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
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for intersection types.
 *
 * @since 1.3.0
 */
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
    public void testImmutableTypesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/intersection/test_intersection_type_negative.bal");
        int index = 0;

        validateError(result, index++, "invalid intersection type with 'readonly', 'future<int>' can never be " +
                "'readonly'", 19, 5);
        validateError(result, index++, "invalid intersection type 'json & int', intersection types are currently " +
                "supported only with 'readonly'", 23, 5);
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
    public void testErrorIntersectionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/intersection/error_intersection_type_negative.bal");

        int index = 0;
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorTwo': no intersection", 45, 24);
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorThree': no intersection", 47, 27);
        validateError(result, index++, "invalid intersection type 'ErrorOne & ErrorFour': no intersection", 49, 29);
        validateError(result, index++,
                      "invalid error detail rest arg 'z' passed to open detail record '"
                              + "record {| string x; string...; |}'", 56, 15);
        validateError(result, index++,
                      "incompatible types: expected 'DistinctErrorIntersection', found 'IntersectionErrorFour'", 57,
                      38);

        assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        readOnlyIntersectionResults = null;
        errorIntersectionResults = null;
    }
}
