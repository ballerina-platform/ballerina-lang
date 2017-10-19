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

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests covering package imports.
 */
public class PackageImportTest {

    @Test
    public void testDuplicatePackageImports() {
        CompileResult result = BTestUtils.compile("test-src/statements/package/imports/duplicate-import-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BTestUtils.validateWarning(result, 0, "redeclared import package 'ballerina.lang.system'", 4, 1);
    }

    @Test
    public void testImportSamePkgWithDifferentAlias() {
        CompileResult result =
                BTestUtils.compile("test-src/statements/packageimport/import-same-pkg-with-different-alias.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testImportDifferentPkgsWithSameAlias() {
        CompileResult result = BTestUtils
                .compile("test-src/statements/packageimport/import-different-pkgs-with-same-alias-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BTestUtils.validateError(result, 0, "redeclared symbol 'x'", 2, 1);
        BTestUtils.validateError(result, 1, "undefined function 'pow'", 6, 5);
    }

    @Test
    public void testInvalidPackageDeclr() {
        CompileResult result = BTestUtils.compile("test-src/statements/package", "a.b");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BTestUtils.validateError(result, 0,
                "invalid package in 'invalid-package-declr-negative.bal': expected 'a.b', found 'x.y'", 1, 1);
    }

    @Test
    public void testMissingPackageDeclr() {
        CompileResult result = BTestUtils.compile("test-src/statements/package", "c.d");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BTestUtils.validateError(result, 0,
                "missing package declaration in 'missing-package-declr-negative.bal': expected 'c.d'", 1, 1);
    }

    @Test
    public void testExtraneousPackageDeclr() {
        CompileResult result = BTestUtils.compile("test-src/statements/package/extraneous-package-declr-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length > 0);
        BTestUtils.validateError(result, 0, "invalid package declaration 'x.y.z' in " + 
                "'extraneous-package-declr-negative.bal'. no package declaration is needed for default package", 1, 1);
    }
}
