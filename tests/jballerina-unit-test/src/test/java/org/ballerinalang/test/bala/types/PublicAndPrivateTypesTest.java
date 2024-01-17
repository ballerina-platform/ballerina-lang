/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test module public and private types with bala.
 */
public class PublicAndPrivateTypesTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_types");
        compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/types/public_and_private_types_test.bal");
    }

    @Test
    public void testModulePublicAndPrivateTypes() {
        BRunUtil.invoke(compileResult, "testModulePublicAndPrivateTypes");
    }

    @Test
    public void testAnonymousDistinctErrorTypes() {
        BRunUtil.invoke(compileResult, "testAnonymousDistinctErrorTypes");
    }
}
