/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for the find all references API when the references spread across in workers.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInWorkersTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {17, 8, List.of(location(17, 8, 9),
                                location(20, 7, 8),
                                location(34, 11, 12))
                },
                {19, 11, List.of(location(19, 11, 13),
                                 location(32, 9, 11))
                },
                {23, 11, List.of(location(23, 11, 13),
                                 location(31, 22, 24))
                },
                {38, 11, List.of(location(38, 11, 13),
                                 location(45, 13, 15),
                                 location(49, 25, 27))
                },
                {40, 11, List.of(location(40, 11, 13),
                                 location(43, 11, 13),
                                 location(49, 29, 31))
                },
                {53, 11, List.of(location(53, 11, 13),
                                 location(61, 13, 15))
                },
                {59, 11, List.of(location(55, 12, 14),
                                 location(56, 25, 27),
                                 location(59, 11, 13))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_within_workers.bal";
    }
}
