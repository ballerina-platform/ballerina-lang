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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BCompileUtil.ExitDetails;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.ballerinalang.test.util.BCompileUtil.run;

/**
 * Services test.
 *
 * @since 0.985.0
 */
public class ServiceTest {

    @Test
    public void testServiceInitNegativeTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_init_negative.bal");
        ExitDetails output = run(compileResult, new String[]{});
        Assert.assertTrue(output.errorOutput.contains("error: startError"));
    }

    @Test(groups = { "disableOnOldParser" })
    public void testServiceWithTransactionalKeyword() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_transactional_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 3);
        int errIdx = 0;
        validateError(compileResult, errIdx++, "undefined symbol 'bar'", 1, 16);
        validateError(compileResult, errIdx++, "undefined annotation 'annot9'", 3, 5);
        validateError(compileResult, errIdx++, "missing close brace token", 4, 1);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*error: startError.*")
    public void testServiceInitPanicNegativeTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_init_panic_negative.bal");
        BRunUtil.invoke(compileResult, "test1");
    }

    @Test
    public void testMultipleServiceTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_multiple.bal");
        final BValue[] result = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(result.length, 2, "expected two return type");
        Assert.assertNotNull(result[0]);
        Assert.assertNotNull(result[1]);
        Assert.assertEquals(result[0].stringValue(), "2");
        Assert.assertEquals(result[1].stringValue(), "0");
    }

    @Test
    public void testUsingListenerFromDepModule() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/proj1", "c");
        final BValue[] result = BRunUtil.invoke(compileResult, "getStartAndAttachCount");
        Assert.assertEquals(result.length, 1, "expected one return type");
        Assert.assertNotNull(result[0]);
        Assert.assertEquals(result[0].stringValue(), "2_1");
    }

    @Test(groups = { "brokenOnNewParser" })
    public void testServiceBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_basic_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 17);
        int errIdx = 0;
        validateError(compileResult, errIdx++, "resource function can not be invoked with in a service", 9, 9);
        validateError(compileResult, errIdx++, "redeclared symbol 'name1'", 19, 9);
        validateError(compileResult, errIdx++,
                "incompatible types: expected 'listener', found 'string'", 19, 18);
        validateError(compileResult, errIdx++, "invalid listener attachment", 19, 18);
        validateError(compileResult, errIdx++, "redeclared symbol 'MyService$$service$2.foo'", 30, 14);
        validateError(compileResult, errIdx++, "undefined symbol 'invalidVar'", 58, 12);
        validateError(compileResult, errIdx++, "service methods cannot have explicit visibility qualifiers", 64, 5);
        validateError(compileResult, errIdx++, "service methods cannot have explicit visibility qualifiers", 68, 5);
        validateError(compileResult, errIdx++, "service methods cannot have explicit visibility qualifiers", 72, 5);
        validateError(compileResult, errIdx++, "service methods cannot have explicit visibility qualifiers", 76, 5);
        validateError(compileResult, errIdx++, "invalid resource function return type 'string?', expected a subtype " +
                "of 'error?' containing '()'", 82, 37);
        validateError(compileResult, errIdx++, "invalid resource function return type 'error', expected a subtype of " +
                "'error?' containing '()'", 86, 37);
        validateError(compileResult, errIdx++, "invalid resource function return type '(FooErr|BarErr)', expected a " +
                "subtype of 'error?' containing '()'", 98, 37);
        validateError(compileResult, errIdx++, "service method call is allowed only within the type descriptor",
                120, 9);
        validateError(compileResult, errIdx++, "service method call is allowed only within the type descriptor",
                121, 9);
        validateError(compileResult, errIdx++, "service method call is allowed only within the type descriptor",
                122, 9);
        validateError(compileResult, errIdx, "service method call is allowed only within the type descriptor",
                123, 9);
    }
}
