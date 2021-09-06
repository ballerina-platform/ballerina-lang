/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests for types with names that have special characters that may be encoded.
 *
 * @since 2.0.0
 */
public class TypeNameWithSpecialCharsBalaTest {

    @Test
    public void testTypeNameWithSpecialChars() {
        CompileResult compileResult =
                BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/type-name-with-special-chars");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
        CompileResult result = BCompileUtil.compile(
                "test-src/bala/test_bala/types/test_type_name_with_special_chars.bal");
        BRunUtil.invoke(result, "testTypeNameWithSpecialChars");
    }
}
