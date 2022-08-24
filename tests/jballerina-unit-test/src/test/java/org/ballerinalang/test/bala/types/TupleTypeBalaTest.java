/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bala.types;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test BIR symbol enter for tuple types.
 *
 * @since 2.0.0
 */
public class TupleTypeBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/tuple_type_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/types/test_tuple_type.bal");
    }

    @Test(dataProvider = "tupleTypeTests")
    public void testTupleType(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "tupleTypeTests")
    public Object[][] tupleTypeTests() {
        return new Object[][]{
                {"testTupleWithMemberAndRestDesc"},
                {"testTupleWithRestDescOnly"},
                {"testEmptyTupleWithRestDescOnly"},
                {"testTupleWithUnionRestDesc"},
                {"testTupleWithVar"}
        };
    }

    @Test
    public void testTupleTypeNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/bala/test_bala/types/test_tuple_type_negative.bal");

        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected '[int]', found '[int,string...]'", 18, 15);
        validateError(negativeResult, i++, "incompatible types: expected '[int,string]', found '[int,string...]'", 19,
                      23);
        validateError(negativeResult, i++, "incompatible types: expected '[int,string,string...]', " +
                "found '[int,string...]'", 20, 34);
        validateError(negativeResult, i++, "incompatible types: expected '[int,boolean...]', found '[int,string...]'",
                      21, 27);

        validateError(negativeResult, i++, "incompatible types: expected '[string,string...]', found '[string...]'",
                      23, 29);
        validateError(negativeResult, i++, "incompatible types: expected '[int...]', found '[string...]'", 24, 18);

        validateError(negativeResult, i++, "incompatible types: expected '[int,string...]', found '[int," +
                "(string|boolean)...]'", 26, 26);
        validateError(negativeResult, i++, "incompatible types: expected '[int,boolean...]', found '[int," +
                "(string|boolean)...]'", 27, 27);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testTupleAnnotations1() {
        Object returns = BRunUtil.invoke(result, "testTupleAnnot1", new Object[]{});

        Type t1 = TypeUtils.getType(returns);
        Object annot = ((BAnnotatableType) t1).getAnnotation(StringUtils.fromString("$field$.1"));

        BMap<BString, Object> expected = ValueCreator.createMapValue();
        expected.put(StringUtils.fromString("tuples/tuple_type_project:0:member"), true);

        Assert.assertEquals(annot, expected);
    }

    @Test
    public void testTupleAnnotations2() {
        Object returns = BRunUtil.invoke(result, "testTupleAnnot2", new Object[]{});
        TupleValueImpl returnTuple = (TupleValueImpl) returns;

        Type t2 = TypeUtils.getType(returnTuple.get(0));
        Type t3 = TypeUtils.getType(returnTuple.get(1));
        Type t4 = TypeUtils.getType(returnTuple.get(2));

        Object annot2 = ((BAnnotatableType) t2).getAnnotation(StringUtils.fromString("$field$.1"));
        Object annot3 = ((BAnnotatableType) t3).getAnnotation(StringUtils.fromString("$field$.0"));
        Object annot4 = ((BAnnotatableType) t4).getAnnotation(StringUtils.fromString("$field$.0"));

        BMap<BString, Object> expected = ValueCreator.createMapValue();
        expected.put(StringUtils.fromString("tuples/tuple_type_project:0:member"), true);

        Assert.assertEquals(annot2, expected);
        Assert.assertEquals(annot3, expected);
        Assert.assertEquals(annot4, expected);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
