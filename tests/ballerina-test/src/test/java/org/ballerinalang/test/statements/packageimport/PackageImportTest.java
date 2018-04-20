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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests covering package imports.
 */
public class PackageImportTest {

    @Test
    public void testDuplicatePackageImports() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/imports/duplicate-import-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateWarning(result, 0, "redeclared import package 'ballerina.math'", 4, 1);
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

    @Test(enabled = false)
    public void testInvalidPackageDeclr() {
        CompileResult result = BCompileUtil.compile(this, "test-src/statements/package", "a.b");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateError(result, 0, "invalid package declaration: expected 'a.b', found 'x.y'", 1, 1);
    }

    @Test(enabled = false)
    public void testMissingPackageDeclr() {
        CompileResult result = BCompileUtil.compile(this, "test-src/statements/package", "c.d");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateError(result, 0, "missing package declaration: expected 'c.d'", 1, 1);
    }

    @Test(enabled = false)
    public void testExtraneousPackageDeclr() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/package/extraneous-package-declr-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BAssertUtil.validateError(result, 0,
                "invalid package declaration 'x.y.z': no package declaration is needed " + "for default package", 1, 1);
    }

    @Test(enabled = false)
    public void testInalidPackageDeclrInMultipleSources() {
        CompileResult result = BCompileUtil.compile(this, "test-src/statements/package/", "p.q");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "missing package declaration: expected 'p.q'", 1, 1);
        BAssertUtil.validateError(result, 1, "invalid package declaration: expected 'p.r', found 'p.q.r'", 1, 1);
        BAssertUtil.validateError(result, 2, "missing package declaration: expected 'x.y'", 1, 1);
        BAssertUtil.validateError(result, 3, "invalid package declaration: expected 'x.z', found 'x.y.z'", 1, 1);
    }

    @Test()
    public void testInvalidImport1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot resolve package 'abcd'", 1, 1);
    }

    @Test()
    public void testInvalidImport2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot resolve package 'foo.x'", 1, 1);
    }

}
