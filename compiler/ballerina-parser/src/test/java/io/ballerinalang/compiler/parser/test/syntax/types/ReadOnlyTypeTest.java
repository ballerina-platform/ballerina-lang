/*
 * Copyright (c) 2020, WSO2 InValidc. (http://www.wso2.org) All Rights Reserved.
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
 * Test parsing readonly type.
 */

public class ReadOnlyTypeTest extends AbstractTypesTest {

    //Valid source tests
    @Test
    public void testValidLocalLevelReadOnlyType() {
        testTopLevelNode("readonly-type/readonly_type_assert_01.bal",
                "readonly-type/readonly_type_assert_01.json");
    }
}
