/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 * Test cases for the find all references API related to tuple type context.
 *
 * @since 2201.4.0
 */
@Test
public class FindRefsInTupleTypeTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {22, 5, location(22, 5, 14),
                        List.of(location(22, 5, 14),
                                location(25, 4, 13))
                },
                {22, 35, location(16, 6, 7),
                        List.of(location(22, 35, 36),
                                location(16, 6, 7),
                                location(26, 4, 5),
                                location(26, 10, 11))
                },
                {22, 38, location(18, 5, 8),
                        List.of(location(22, 38, 41),
                                location(18, 5, 8))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_refs_in_tuple_types.bal";
    }
}
