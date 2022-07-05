/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array index too large test.
 *
 * @since 0.86
 */
public class ArrayIndexTooLargeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-index-too-large.bal");
    }

    @Test(description = "Test adding too large index to an array",
          expectedExceptions = {BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*index number too large: 2,147,483,648.*")
    public void addTooLargeIndex() {
        Object[] args = {(2147483648L), (7)};
        BRunUtil.invoke(compileResult, "addTooLargeIndex", args);
    }

    @Test(description = "Test accessing too large index from an array",
          expectedExceptions = {BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*index number too large: 2,147,483,648.*")
    public void accessTooLargeIndex() {
        Object[] args = {(2147483648L)};
        Object returns =  BRunUtil.invoke(compileResult, "accessTooLargeIndex", args);
    }

    @Test(description = "Test adding minus index to an array",
          expectedExceptions = {BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*array index out of range: index: -4, size: 0.*")
    public void addMinusIndex() {
        Object[] args = {(-4), (7)};
        BRunUtil.invoke(compileResult, "addMinusIndex", args);
    }

    @Test(description = "Test accessing minus index from an array",
          expectedExceptions = {BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*array index out of range: index: -4, size: 0.*")
    public void accessMinusIndex() {
        Object[] args = {(-4)};
        Object returns =  BRunUtil.invoke(compileResult, "accessMinusIndex", args);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
