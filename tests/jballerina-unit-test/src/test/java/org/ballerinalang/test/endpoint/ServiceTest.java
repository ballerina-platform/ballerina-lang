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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Services test.
 *
 * @since 0.985.0
 */
public class ServiceTest {

    @Test(expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*error: startError.*")
    public void testServiceInitNegativeTest() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_init_negative.bal");
        BRunUtil.invoke(compileResult, "test1");
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
    public void testServiceBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_basic_negative.bal");
        int errIdx = 0;
        BAssertUtil
                .validateError(compileResult, errIdx++, "resource function can not be invoked with in a service", 7, 9);
        BAssertUtil.validateError(compileResult, errIdx++, "redeclared symbol 'name1'", 17, 9);
        BAssertUtil.validateError(compileResult, errIdx++,
                "incompatible types: expected 'AbstractListener', found 'string'", 17, 18);
        BAssertUtil.validateError(compileResult, errIdx++, "invalid listener attachment", 17, 18);
        BAssertUtil.validateError(compileResult, errIdx++, "uninitialized field 'id'", 18, 5);
        BAssertUtil.validateError(compileResult, errIdx++, "redeclared symbol 'MyService$$service$2.foo'", 29, 14);
        BAssertUtil.validateError(compileResult, errIdx++, "undefined symbol 'invalidVar'", 50, 12);
        Assert.assertEquals(compileResult.getErrorCount(), errIdx);
    }

}
