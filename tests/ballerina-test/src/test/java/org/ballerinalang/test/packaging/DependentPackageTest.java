/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.packaging;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.packerina.UserRepositoryUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DependentPackageTest extends PackagingTest {

    @Test(description = "Installed packages should be visible to other packages.")
    public void testInstall() {
        Path parent = Paths.get("src/test/resources/test-src/packaging/parent1");
        try {
            UserRepositoryUtils.installSourcePackage(parent.toAbsolutePath(), "foo/bar");
            CompileResult compileResult = BCompileUtil.compile("test-src/packaging/child1/main.bal");
            BStringArray arrayValue = new BStringArray();
            arrayValue.add(0, "hello");
            BValue[] args = {arrayValue};
            BValue[] returnValues = BRunUtil.invoke(compileResult, "main", args);
            Assert.assertEquals(returnValues[0].stringValue(), "hello world");
        } finally {
            UserRepositoryUtils.uninstallSourcePackage("foo/bar");
        }
    }

}
