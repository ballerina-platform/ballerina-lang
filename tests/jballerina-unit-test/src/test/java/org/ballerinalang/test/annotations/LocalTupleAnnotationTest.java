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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.TypedescValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test annotation evaluation within local tuple types.
 *
 * @since 2201.4.0
 */
public class LocalTupleAnnotationTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/annotations/local_tuple_annotation_test.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test(dataProvider = "dataToTestAnnotationsOfLocalTuple")
    public void testLocalRecordAnnotations(String function) {
        BRunUtil.invoke(result, function);
    }

    @Test(description = "Test annotations of tuple members from the type")
    public void testTupleAnnotations1() {
        Object returns = BRunUtil.invoke(result, "testTupleMemberAnnotations1", new Object[]{});
        TupleValueImpl returnTuple = (TupleValueImpl) returns;

        Object annot1 = ((BAnnotatableType) returnTuple.getType()).getAnnotation(StringUtils.fromString("$field$.0"));
        Object annot2 = ((BAnnotatableType) returnTuple.getType()).getAnnotation(StringUtils.fromString("$field$.1"));

        Assert.assertTrue(annot1 instanceof BMap);
        Assert.assertTrue(annot2 instanceof BMap);
    }

    @Test(description = "Test annotations of tuple members from the type")
    public void testTupleAnnotations2() {
        Object returns = BRunUtil.invoke(result, "testTupleMemberAnnotations2", new Object[]{});
        TupleValueImpl returnTuple = (TupleValueImpl) returns;

        Object annot1 = ((BAnnotatableType) returnTuple.getType()).getAnnotation(StringUtils.fromString("$field$.0"));

        Assert.assertTrue(annot1 instanceof BMap);
    }

    @DataProvider(name = "dataToTestAnnotationsOfLocalTuple")
    public Object[] dataToTestAnnotationsOfLocalTuple() {
        return new String[]{
                "testAnnotationOnTupleFields",
                "testAnnotationOnTupleFields2",
                "testAnnotationOnTupleWithGlobalVariable",
                "testMultipleAnnotationsOnLocalTuple",
                "testGlobalAnnotationsOnFunctionPointerReturnType",
                "testGlobalAnnotationsOnFunctionReturnType",
                "testGlobalAnnotationsOnFunctionParameterType"
        };
    }

    public static BMap getLocalTupleAnnotations(TypedescValue typedescValue, BString annotName) {
        return (BMap) TypeChecker.getAnnotValue(typedescValue, annotName);
    }
}
