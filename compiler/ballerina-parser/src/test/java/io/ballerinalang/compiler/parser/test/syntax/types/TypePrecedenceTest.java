/*
 * Copyright (c) 2021, WSO2 InValidc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 InValidc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.types;

import org.testng.annotations.Test;

/**
 * Test type descriptor parsing based on the precedence.
 *
 * @since 2.0.0
 */
public class TypePrecedenceTest extends AbstractTypesTest {

    // Valid source tests
    
    @Test
    public void testUnionAndIntersection() {
        testFile("type-precedence/type_precedence_source_01.bal", "type-precedence/type_precedence_assert_01.json");
    }

    @Test
    public void testUnionIntersectionAndArray() {
        testFile("type-precedence/type_precedence_source_02.bal", "type-precedence/type_precedence_assert_02.json");
        testFile("type-precedence/type_precedence_source_05.bal", "type-precedence/type_precedence_assert_05.json");
    }

    @Test
    public void testUnionIntersectionArrayAndDistinct() {
        testFile("type-precedence/type_precedence_source_03.bal", "type-precedence/type_precedence_assert_03.json");
    }

    @Test
    public void testPrecedenceWithOptionalType() {
        testFile("type-precedence/type_precedence_source_06.bal", "type-precedence/type_precedence_assert_06.json");
        testFile("type-precedence/type_precedence_source_07.bal", "type-precedence/type_precedence_assert_07.json");
    }

    @Test
    public void testAllLevels() {
        testFile("type-precedence/type_precedence_source_04.bal", "type-precedence/type_precedence_assert_04.json");
    }
}
