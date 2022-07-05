/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.semantic.api.test.allreferences;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for the find all references API related to conditional statement contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInConditionalStmtTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {17, 4, location(42, 9, 13),
                        List.of(location(17, 4, 8),
                                location(24, 8, 12),
                                location(30, 8, 12),
                                location(32, 8, 12),
                                location(42, 9, 13))
                },
                {19, 12, location(45, 9, 12),
                        List.of(location(19, 12, 15),
                                location(27, 7, 10),
                                location(29, 14, 17),
                                location(45, 9, 12))
                },
                {35, 15, location(50, 8, 9),
                        List.of(location(50, 8, 9),
                                location(21, 23, 24),
                                location(35, 15, 16),
                                location(36, 16, 17))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_in_conditional_stmt.bal";
    }
}
