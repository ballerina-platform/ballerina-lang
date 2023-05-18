/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.annotations;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.values.TypedescValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test annotation evaluation within anonymous tuple types.
 *
 * @since 2201.4.0
 */
public class AnonymousTupleAnnotationTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/annotations/anonymous_tuple_annotation_test.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test(dataProvider = "dataToTestAnnotationsOfLocalTuple")
    public void testLocalRecordAnnotations(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "dataToTestAnnotationsOfLocalTuple")
    public Object[] dataToTestAnnotationsOfLocalTuple() {
        return new String[]{
                "testAnnotationOnTupleFields",
                "testAnnotationOnTupleFields2",
                "testAnnotationOnTupleWithGlobalVariable",
                "testMultipleAnnotationsOnLocalTuple",
                "testTupleAnnotationsOnFunctionPointerReturnType",
                "testGlobalAnnotationsOnFunctionReturnType",
                "testGlobalAnnotationsOnFunctionParameterType",
                "testTupleMemberAnnotations"
        };
    }

    public static BMap getAnonymousTupleAnnotations(TypedescValue typedescValue, BString annotName) {
        return (BMap) TypeChecker.getAnnotValue(typedescValue, annotName);
    }
}
