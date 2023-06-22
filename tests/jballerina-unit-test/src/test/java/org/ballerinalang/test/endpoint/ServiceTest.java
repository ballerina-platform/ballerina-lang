/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.endpoint;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.BRunUtil.ExitDetails;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Services test.
 *
 * @since 0.985.0
 */
public class ServiceTest {

    @Test
    public void testServiceInitNegativeTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_init_negative.bal");
        ExitDetails output = BRunUtil.run(compileResult, new String[]{});
        Assert.assertTrue(output.errorOutput.contains("error: startError"));
    }

    @Test
    public void testServiceWithTransactionalKeyword() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_transactional_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 3);
        int errIdx = 0;
        validateError(compileResult, errIdx++, "undefined symbol 'bar'", 1, 17);
        validateError(compileResult, errIdx++, "undefined annotation 'annot9'", 3, 5);
        validateError(compileResult, errIdx++, "missing close brace token", 4, 1);
    }

    @Test(expectedExceptions = { BLangTestException.class },
          expectedExceptionsMessageRegExp = ".*error: startError.*")
    public void testServiceInitPanicNegativeTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_init_panic_negative.bal");
        BRunUtil.invoke(compileResult, "test1");
    }

    @Test
    public void testMultipleServiceTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_multiple.bal");
        final Object resultArr = BRunUtil.invoke(compileResult, "test1");
        BArray result = (BArray) resultArr;
        Assert.assertEquals(result.size(), 2, "expected two return type");
        Assert.assertNotNull(result.get(0));
        Assert.assertNotNull(result.get(1));
        Assert.assertEquals(result.get(0).toString(), "2");
        Assert.assertEquals(result.get(1).toString(), "0");
    }

    @Test
    public void testUsingListenerFromDepModule() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/TestListenerProject");
        final Object result = BRunUtil.invoke(compileResult, "getStartAndAttachCount");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.toString(), "2_1");
    }

    @Test
    public void testServiceBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_basic_negative.bal");
        int errIdx = 0;
        validateError(compileResult, errIdx++, "invalid remote method call '.bar()': use '->bar()' for remote method " +
                "calls", 7, 9);
        validateError(compileResult, errIdx++,
                "incompatible types: expected 'listener', found 'string'", 17, 20);
        validateError(compileResult, errIdx++, "invalid listener attachment", 17, 20);
        validateError(compileResult, errIdx++, "redeclared symbol '$anonType$_2.foo'", 28, 21);
        validateError(compileResult, errIdx++, "undefined symbol 'invalidVar'", 54, 12);
        validateError(compileResult, errIdx++, "'private' qualifier not allowed", 60, 5);
        validateError(compileResult, errIdx++, "'public' qualifier not allowed", 64, 5);
        validateError(compileResult, errIdx++, "undefined method 'tuv' in object 'object { function xyz () " +
                "returns (); }'", 94, 13);
        validateError(compileResult, errIdx++, "undefined symbol 'kgp'", 96, 9);
        Assert.assertEquals(compileResult.getErrorCount(), errIdx);
    }
}
