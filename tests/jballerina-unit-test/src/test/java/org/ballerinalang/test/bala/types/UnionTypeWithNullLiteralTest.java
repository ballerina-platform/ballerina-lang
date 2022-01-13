/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test cases for import user defined type.
 * @since 2.0.0
 */
public class UnionTypeWithNullLiteralTest {

    private CompileResult result;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test-union-type-with-null-literal/type-export");

        this.result = BCompileUtil.compile("test-src/bala/test_projects/test-union-type-with-null-literal/main.bal");
        this.negativeCompileResult = BCompileUtil.compile(
                "test-src/bala/test_projects/test-union-type-with-null-literal/negative_assignment.bal");
    }

    @Test
    public void unionTypeWithNullLiteralValueAssignment() {
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void unionTypeWithNullLiteralValueAssignmentNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 1,
                Arrays.asList(result.getDiagnostics()).toString());
    }

}
