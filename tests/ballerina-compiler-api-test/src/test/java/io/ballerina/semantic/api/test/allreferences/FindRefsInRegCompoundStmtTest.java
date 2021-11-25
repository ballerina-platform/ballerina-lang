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
 * Test cases for the find all references API related to regular compound statement contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInRegCompoundStmtTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {18, 10, location(50, 9, 13),
                        List.of(location(18, 10, 14),
                                location(19, 16, 20),
                                location(50, 9, 13))
                },
                {25, 16, location(25, 16, 17),
                        List.of(location(25, 16, 17),
                                location(26, 16, 17))
                },
                {33, 8, location(54, 9, 13),
                        List.of(location(33, 8, 12),
                                location(38, 8, 12),
                                location(46, 8, 12),
                                location(54, 9, 13))
                },
                {43, 8, location(43, 8, 12),
                        List.of(location(43, 8, 12),
                                location(45, 8, 12))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_in_reg_compound_stmt.bal";
    }
}
