/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.launch;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Tests running packages.
 */
public class LaunchTest {

    @Test(expectedExceptions = {BLangCompilerException.class },
            expectedExceptionsMessageRegExp = "no ballerina source files found in module xxxx")
    public void testRunNonExistingPackage() {
        CompileResult result = BCompileUtil.compile(this, "test-src/launch/", "xxxx");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertNull(result.getProgFile());
    }

//    @Test
//    public void testRunEmptyPackage() {
//        CompileResult result = BCompileUtil.compile(this, "test-src/launch/", "foo");
//        Assert.assertEquals(result.getErrorCount(), 0);
//        Assert.assertNull(result.getProgFile());
//    }

    @Test
    public void testRunPackageWithFileSeparater() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/launch/", "foo.bar");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BValue[] result = BRunUtil.invoke(compileResult, "foo");
        Assert.assertEquals(result[0].stringValue(), "hello!");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "ballerina: cannot find program file 'non_existing.balx'")
    public void testNonExistingBalx() {
        LauncherUtils.runProgram(Paths.get("test-src", "launch"), Paths.get("non_existing.balx"), new HashMap<>(),
                                 null, new String[]{}, true, false);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: Main throwing an error.*")
    public void testMainFailureWithService() {
        LauncherUtils.runProgram(Paths.get("src/test/resources/test-src/launch/main-and-service"),
                                 Paths.get("main_and_service.bal"),
                                 new HashMap<>(), null, new String[]{"10s"}, true, false);
    }
}
