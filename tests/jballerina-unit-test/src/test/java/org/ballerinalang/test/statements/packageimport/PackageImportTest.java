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

import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Tests covering package imports.
 */
@Test
public class PackageImportTest {

    @Test()
    public void testDuplicatePackageImports() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/duplicate-import-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateError(result, 0, "redeclared import module 'ballerina/java'", 2, 1);
    }

    @Test (enabled = false)
    public void testImportSamePkgWithDifferentAlias() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/import-same-pkg-with-different-alias.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test (enabled = false)
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
        BAssertUtil.validateError(result, 0, "cannot resolve module 'abcd'", 1, 1);
    }

    @Test()
    public void testInvalidImport2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot resolve module 'foo.x as x'", 1, 1);
    }

    @Test (enabled = false)
    public void testImportsPerfile() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/sample-project-1", "invalid-imports");
        Assert.assertEquals(result.getErrorCount(), 6);
        int i = 0;
        BAssertUtil.validateError(result, i++, "redeclared import module 'ballerina/io'", "src/file-negative1.bal", 3,
                1);
        BAssertUtil.validateError(result, i++, "undefined module 'http'", "src/file-negative2.bal", 3, 5);
        BAssertUtil.validateError(result, i++, "unknown type 'Client'", "src/file-negative2.bal", 3, 5);
        BAssertUtil.validateError(result, i++, "undefined function 'println'", "src/file-negative2.bal", 4, 5);
        BAssertUtil.validateError(result, i++, "undefined module 'io'", "src/file-negative2.bal", 4, 5);
        BAssertUtil.validateError(result, i, "undefined module 'io'", "src/file-negative2.bal", 5, 18);
    }

    @Test
    public void testUnusedImports() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/unused-imports-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        int i = 0;
        BAssertUtil.validateError(result, i++, "unused import module 'ballerina/io'", 1, 1);
        BAssertUtil.validateError(result, i, "unused import module 'ballerina/io as otherIO'", 2, 1);
    }

    @Test(enabled = false)
    public void testIgnoreImport() {
        PrintStream out = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        BaloCreator.cleanCacheDirectories();
        CompileResult result = BCompileUtil.compile("test-src/statements/package/sample-project-2", "foo");
        BRunUtil.invoke(result, "runFoo");

        System.setOut(out);
        String output = new String(baos.toByteArray());
        Assert.assertTrue(output.contains("initializing bar\nRunning foo"), "found: " + output);
    }

    @Test(enabled = false)
    // New spec change has introduced to support this
    public void testUnderscoreAsPkgQualifier() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/invalid-package-qualifier-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "invalid package name '_'", 4, 5);
    }
}
