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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests running packages.
 */
public class LaunchTest {

    @Test(expectedExceptions = { RuntimeException.class },
            expectedExceptionsMessageRegExp = "cannot find package 'xxxx'")
    public void testRunNonExistingPackage() {
        BTestUtils.compile("test-src/launch/", "xxxx");
    }

    @Test(expectedExceptions = { RuntimeException.class },
            expectedExceptionsMessageRegExp = "cannot find any entry in package 'foo'")
    public void testRunEmptyPackage() {
        BTestUtils.compile("test-src/launch/", "foo");
    }

    @Test
    public void testRunPackageWithFileSeparater() {
        CompileResult compileResult = BTestUtils.compile("test-src/launch/", "foo/bar");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BValue[] result = BTestUtils.invoke(compileResult, "foo");
        Assert.assertEquals(result[0].stringValue(), "hello!");
    }

    @Test
    public void testRunPackageWithDotSeparater() {
        CompileResult compileResult = BTestUtils.compile("test-src/launch/", "foo.bar");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BValue[] result = BTestUtils.invoke(compileResult, "foo");
        Assert.assertEquals(result[0].stringValue(), "hello!");
    }
}
