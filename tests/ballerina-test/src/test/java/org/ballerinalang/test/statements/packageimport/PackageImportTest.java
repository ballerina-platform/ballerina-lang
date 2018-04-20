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

import org.ballerinalang.compiler.BLangCompilerException;
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

    @Test(expectedExceptions = { BLangCompilerException.class },
            expectedExceptionsMessageRegExp = "cannot find package 'abcd'")
    public void testInvalidImport1() {
        BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative1.bal");
    }

    @Test(expectedExceptions = { BLangCompilerException.class },
            expectedExceptionsMessageRegExp = "cannot find package 'foo.x'")
    public void testInvalidImport2() {
        BCompileUtil.compile("test-src/statements/package/imports/invalid-import-negative2.bal");
    }

}
