/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.declarations;

import org.testng.annotations.Test;

/**
 * Test parsing object method definitions.
 * 
 * @since Swan Lake
 */
public class ObjectMethodTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testIsolatedMethods() {
        test("isolated-object-methods/isolated_object_method_source_01.bal",
             "isolated-object-methods/isolated_object_method_assert_01.json");
    }

    // Recovery tests

    @Test(enabled = false) // Disabled temporary
    public void testMissingFunctionKeywordWithQualifiers() {
        testFile("isolated-object-methods/isolated_object_method_source_02.bal",
                 "isolated-object-methods/isolated_object_method_assert_02.json");
    }

    @Test
    public void testMissingFunctionNameWithQualifiers() {
        testFile("isolated-object-methods/isolated_object_method_source_03.bal",
                 "isolated-object-methods/isolated_object_method_assert_03.json");
    }

    @Test
    public void testMissingTokensWithQualifiers() {
        testFile("isolated-object-methods/isolated_object_method_source_04.bal",
                 "isolated-object-methods/isolated_object_method_assert_04.json");
    }
}
