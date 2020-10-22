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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined struct types in ballerina.
 */
public class StructEquivalencyNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src/structs/proj/", "struct-eq-neg");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'struct-eq-neg:employee01' " +
                    "value cannot be converted to 'struct-eq-neg:person01'.*")
    public void testEqOfStructsInSamePackageFieldNameMismatch() {
        BValue[] result = BRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage01");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'struct-eq-neg:employee02' " +
                    "value cannot be converted to 'struct-eq-neg:person02'.*")
    public void testEqOfStructsInSamePackageTypeNameMismatch() {
        BValue[] result = BRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage02");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'struct-eq-neg:employee03' " +
                    "value cannot be converted to 'struct-eq-neg:person03'.*")
    public void testEqOfStructsInSamePackageFieldCountMismatch() {
        BValue[] result = BRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage03");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'struct-eq-neg:employee06' " +
                    "value cannot be converted to 'struct-eq-neg:person06'.*")
    public void testEqOfStructsInSamePackageTypeMismatch() {
        BValue[] result = BRunUtil.invoke(compileResult, "testEqOfStructsInSamePackage06");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*ConversionError \\{\"message\":\"'org.foo.bar:userBar' " +
                    "value cannot be converted to 'org.foo:userFoo'.*")
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStructEqViewFromThirdPackage");
    }
}
