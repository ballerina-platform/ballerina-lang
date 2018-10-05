/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.map;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for map initializer expression.
 *
 * @since 0.982.1
 */
public class MapInitializerExprNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(
                "test-src/types/map/map-initializer-expr-duplicated-keys.bal");
    }

    @Test(description = "Test map initializer expression with duplicated keys")
    public void mapInitWithDuplicatedKeysTest() {
        BAssertUtil.validateError(compileResult, 0, "invalid usage of map literal: duplicate key 'key'", 2, 35);
    }

    @Test(description = "Test map initializer expression with duplicated keys when one key is a string literal")
    public void mapInitWithDuplicatedKeysOneStringKeyTest() {
        BAssertUtil.validateError(compileResult, 1, "invalid usage of map literal: duplicate key 'key'", 7, 35);
    }

    @Test(description = "Test map initializer expression with duplicated keys when both keys are string literals")
    public void mapInitWithDuplicatedKeysBothStringKeysTest() {
        BAssertUtil.validateError(compileResult, 2, "invalid usage of map literal: duplicate key 'key'", 12, 37);
    }
}
