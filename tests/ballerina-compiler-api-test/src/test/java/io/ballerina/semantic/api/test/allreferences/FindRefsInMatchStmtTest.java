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
 * Test cases for the find all references API related to match statement contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInMatchStmtTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
//                {18, 18, location(18, 18, 21),
//                        List.of(location(18, 18, 21),
//                                location(19, 10, 13),
//                                location(21, 20, 23),
//                                location(24, 20, 23),
//                                location(27, 20, 23),
//                                location(32, 20, 23),
//                                location(36, 24, 27),
//                                location(37, 20, 23),
//                                location(40, 20, 23))
//                },
//                {26, 13, location(26, 13, 14),
//                        List.of(location(26, 13, 14),
//                                location(28, 24, 25))
//                },
                {26, 30, location(26, 30, 34),
                        List.of(location(26, 30, 34),
                                location(29, 24, 28))
                },
                {31, 26, location(31, 26, 27),
                        List.of(location(31, 26, 27),
                                location(33, 24, 25))
                },
                {34, 24, location(31, 36, 40),
                        List.of(location(31, 36, 40),
                                location(34, 24, 28))
                },
                {43, 23, location(42, 20, 23),
                        List.of(location(42, 20, 23),
                                location(43, 23, 26))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_match_stmt_context.bal";
    }
}
