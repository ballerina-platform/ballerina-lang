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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Services test.
 *
 * @since 0.985.0
 */
public class ServiceTest {
    //    private CompileResult remoteBasic;
    //
    //    @BeforeClass
    //    public void setupServiceBasic() {
    //        //        remoteBasic = BCompileUtil.compile("test-src/endpoint/new/service_basic.bal");
    //    }
    //
    //    public void testServiceBasic() {
    //
    //    }

    @Test
    public void testServiceBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/service_basic_negative.bal");
        int errIdx = 0;
        BAssertUtil
                .validateError(compileResult, errIdx++, "resource function can not be invoked with in a service", 7, 9);
        BAssertUtil.validateError(compileResult, errIdx++, "redeclared symbol 'name'", 17, 9);
        BAssertUtil.validateError(compileResult, errIdx++,
                "incompatible types: expected 'AbstractListener', found 'string'", 17, 17);
        BAssertUtil.validateError(compileResult, errIdx++, "invalid listener attachment", 17, 17);
        BAssertUtil.validateError(compileResult, errIdx++, "redeclared symbol '$anonType$2.foo'", 29, 14);
        Assert.assertEquals(compileResult.getErrorCount(), errIdx);
    }

}
