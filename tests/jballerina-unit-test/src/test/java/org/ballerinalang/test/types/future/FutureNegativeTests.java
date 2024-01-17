/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.future;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This class contains future type related test cases.
 */
public class FutureNegativeTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/future/future_negative.bal");
    }

    @Test
    public void testNegatives() {
        CompileResult errors = BCompileUtil.compile("test-src/types/future/future_negative_compilation.bal");
        int index = 0;
        validateError(errors, index++, "incompatible types: expected 'future<Bar>', found 'future<Foo>'", 18, 22);
        validateError(errors, index++,
                      "incompatible types: expected 'future<(int|string|error)>', found 'future<(Foo|Bar)>'", 19, 35);
        validateError(errors, index++, "incompatible types: expected 'future<Foo>', found 'future<Bar>'", 21, 22);
        validateError(errors, index++,
                      "incompatible types: expected 'future<(int|string)>', found 'future<(int|string|error)>'",
                      22, 29);
        validateError(errors, index++, "incompatible types: expected 'future<Bar>', found 'future<(any|error)>'",
                      25, 22);
        Assert.assertEquals(errors.getErrorCount(), index);
    }

    @DataProvider(name = "multipleWait")
    public Object[] getFunctionNames() {
        return new String[]{"testWaitTwiceOnSingleFuture", "testMultipleWaitTwiceOnTwoFutures",
                "testMultipleWaitTwiceOnDifferentFutures", "testAlternateWaitTwiceOnTwoFutures",
                "testMultipleWaitAndAlternateWait", "testWaitError"};
    }

    @Test(dataProvider = "multipleWait")
    public void testFutureNegatives(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @Test
    public void testStrand() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/future/strand_error.bal");
        Object result = BRunUtil.invoke(compileResult, "testStrand");
        var mapResult = (BMap<String, BMap<String, Object>>) result;
        Set<String> set = new HashSet<>();
        set.add(mapResult.get(StringUtils.fromString("f1")).get(StringUtils.fromString("f0")).toString());
        set.add(mapResult.get(StringUtils.fromString("f1")).get(StringUtils.fromString("f00")).toString());
        set.add(mapResult.get(StringUtils.fromString("f2")).get(StringUtils.fromString("f0")).toString());
        set.add(mapResult.get(StringUtils.fromString("f2")).get(StringUtils.fromString("f00")).toString());
        Assert.assertEquals(set.size(), 2);
        Assert.assertTrue(set.contains("3"));
        Assert.assertTrue(set.contains("error(\"multiple waits on the same future is not allowed\")"));
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
