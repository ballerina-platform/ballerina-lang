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
package org.ballerinalang.test.statements.packageimport;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Tests covering package imports.
 */
@Test
public class PackageImportTest {

    @Test
    public void testDuplicatePackageImports() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/duplicate-import-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateError(result, 0, "redeclared import module 'ballerina/test'", 2, 1);
    }

    @Test
    public void testImportSamePkgWithDifferentAlias() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/import-same-pkg-with-different-alias.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testImportDifferentPkgsWithSameAlias() {
        CompileResult result = BCompileUtil
                .compile("test-src/statements/package/imports/import-different-pkgs-with-same-alias-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "redeclared symbol 'x'", 2, 1);
    }

    @Test()
    public void testInvalidImport1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot resolve module 'abcd/efgh'", 1, 1);
    }

    @Test()
    public void testInvalidImport2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot resolve module 'bar/foo.x as x'", 1, 1);
    }

    @Test
    public void testImportsPerfile() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/sample_project_1");
        Assert.assertEquals(result.getErrorCount(), 6);
        int i = 0;
        BAssertUtil.validateError(result, i++, "redeclared symbol 'int'", "file-negative1.bal", 3,
                1);
        BAssertUtil.validateError(result, i++, "undefined module 'http'", "file-negative2.bal", 3, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Client'", "file-negative2.bal", 3, 5);
        BAssertUtil.validateError(result, i++, "undefined function 'println'", "file-negative2.bal", 4, 5);
        BAssertUtil.validateError(result, i++, "undefined module 'io'", "file-negative2.bal", 4, 5);
        BAssertUtil.validateError(result, i, "undefined module 'io'", "file-negative2.bal", 5, 18);
    }

    @Test
    public void testUnusedImports() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/unused-imports-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        int i = 0;
        BAssertUtil.validateError(result, i++, "unused module prefix 'java'", 1, 29);
        BAssertUtil.validateError(result, i++, "unused module prefix 'otherJAVA'", 2, 37);
        BAssertUtil.validateError(result, i, "unused module prefix 'value'", 4, 23);
    }

    @Test(enabled = false)
    public void testIgnoreImport() {
        PrintStream out = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        CompileResult result = BCompileUtil.compile("test-src/statements/package/sample-project-2");
        BRunUtil.invoke(result, "runFoo");

        System.setOut(out);
        String output = new String(baos.toByteArray());
        Assert.assertTrue(output.contains("initializing bar"), "found: " + output);
    }
}
