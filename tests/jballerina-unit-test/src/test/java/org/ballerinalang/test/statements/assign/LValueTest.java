/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.assign;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test lvalues of assignments.
 *
 * An lvalue is what the left hand side of an assignment evaluates to. An lvalue refers to a storage location which
 * is one of the following:
 *
 * - a variable;
 * - a specific named field of an object;
 * - the member of a container having a specific key, which will be either an integer or a string according as the
 * container is a list or a mapping.
 *
 * An lvalue that is both defined and initialized refers to a storage location that holds a value:
 * - an lvalue referring to a variable is always defined but may be uninitialized;
 * - an lvalue referring to a specific named field of an object is always defined but may not be initialized until the
 * __init method returns
 * - an lvalue referring to member of a container having a specific key is undefined if the container does not have a
 * member with that key; if such an lvalue is defined, it is also initialized; note that an lvalue always refers to a
 * container that is already constructed.
 *
 * lvexpr :=
 *    variable-reference-lvexpr
 *    | field-access-lvexpr
 *    | member-access-lvexpr
 *
 * @since 1.0
 */
public class LValueTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/assign/lvalue.bal");
        negativeResult = BCompileUtil.compile("test-src/statements/assign/lvalue_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 4);
        int i = 0;
        validateError(negativeResult, i++, "undefined field 'y' in object 'A'", 23, 5);
        validateError(negativeResult, i++, "invalid operation: type 'A' does not support indexing", 24, 5);
        validateError(negativeResult, i++, "optional field access cannot be used in the target expression of an " +
                "assignment", 34, 5);
        validateError(negativeResult, i, "optional field access cannot be used in the target expression of an " +
                "assignment", 35, 5);
    }

    @Test(dataProvider = "valueStoreFunctions")
    public void testValueStore(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "valueStoreFunctions")
    public Object[][] valueStoreFunctions() {
        return new Object[][] {
            { "testBasicValueStoreForVariable" },
            { "testValueStoreForMap" },
            { "testValueStoreForRecord" },
            { "testValueStoreForObject" },
            { "testBasicValueStoreForUnionVariable" },
            { "testValueStoreForMapUnion" },
            { "testValueStoreForRecordUnion" },
            { "testValueStoreForObjectUnion" }
        };
    }
}
