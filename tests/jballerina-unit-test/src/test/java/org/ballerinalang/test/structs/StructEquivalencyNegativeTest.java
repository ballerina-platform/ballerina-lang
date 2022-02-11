/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.structs;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined struct types in ballerina.
 */
public class StructEquivalencyNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/StructEquivalencyNegativeTestProject");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'equivalencynegative:employee01' " +
                    "value cannot be converted to 'equivalencynegative:person01'.*")
    public void testEqOfStructsInSamePackageFieldNameMismatch() {
        JvmRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage01");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'equivalencynegative:employee02' " +
                    "value cannot be converted to 'equivalencynegative:person02'.*")
    public void testEqOfStructsInSamePackageTypeNameMismatch() {
        JvmRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage02");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'equivalencynegative:employee03' " +
                    "value cannot be converted to 'equivalencynegative:person03'.*")
    public void testEqOfStructsInSamePackageFieldCountMismatch() {
        JvmRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage03");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'equivalencynegative:employee06' " +
                    "value cannot be converted to 'equivalencynegative:person06'.*")
    public void testEqOfStructsInSamePackageTypeMismatch() {
        JvmRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage06");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'equivalencynegative.bar:userBar' " +
                    "value cannot be converted to 'equivalencynegative.foo:userFoo'.*")
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        JvmRunUtil.invoke(compileResult, "testStructEqViewFromThirdPackage");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
