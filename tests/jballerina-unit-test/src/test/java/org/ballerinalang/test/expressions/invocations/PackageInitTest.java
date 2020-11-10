/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.expressions.invocations;

import org.ballerinalang.core.model.values.BValueType;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * package init function invocation test.
 *
 * @since 1.0.0
 */
public class PackageInitTest {

    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.out;
    }

    @Test
    public void testPackageInitsInDependantPackages() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));
            CompileResult result = BCompileUtil.compile("test-src/packagetest/initOrder", "c");
            BRunUtil.invoke(result, "getA1", new BValueType[0]);
            int count = countOccurences(outputStream.toString().replace("\r", ""), "PackageA");
            Assert.assertEquals(count, 1);
        } finally {
            System.setOut(original);
        }
    }

    private static int countOccurences(String str, String word) {
        String[] a = str.split("\n");

        int count = 0;
        for (String s : a) {
            if (word.equals(s)) {
                count++;
            }
        }

        return count;
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        System.setOut(original);
    }

}
