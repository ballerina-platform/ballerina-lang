/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.error;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test case for Error StackTrace.
 *
 * @since 2.0.0
 */

public class PrintableStackTraceTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/error/project");
    }

    @Test
    public void testPrintableStackTrace() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(compileResult, "main");
        } catch (Exception e) {
            expectedException = e;
        }

        Assert.assertNotNull(expectedException);
        String message = expectedException.getMessage();
        Assert.assertEquals(message, "error: error2\n" +
                "\tat test_org.project.0:testFunc(main.bal:24)\n" +
                "\t   test_org.project.0:main(main.bal:20)\n" +
                "cause: error1\n" +
                "\tat test_org.project.foo.0:errorFunc(foo.bal:22)\n" +
                "\t   test_org.project.foo.0:testFunc(foo.bal:18)\n" +
                "\t   ... 2 more");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

}
