/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for readonly array creation of basic types using java interop.
 */
public class ReadonlyArrayTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/readonly/array_creation.bal");
    }

    @Test(expectedExceptions = org.ballerinalang.core.util.exceptions.BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*modification not allowed on readonly " +
                  "value.*", dataProvider = "arrayTests")
    public void testCreateArray(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "arrayTests")
    public Object[] getFunctionNames() {
        return new String[]{"testIntArray", "testBooleanArray", "testByteArray", "testFloatArray", "testStringArray"};
    }

    @Test
    public void testReadOnlyMappingWithOptionalNeverFieldArray() {
        BRunUtil.invoke(result, "testReadOnlyMappingWithOptionalNeverFieldArray");
    }
}
