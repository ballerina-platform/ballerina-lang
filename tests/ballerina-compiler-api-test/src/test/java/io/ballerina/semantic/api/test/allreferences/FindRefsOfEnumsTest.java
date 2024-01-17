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
 * Test cases for the find all references API related to enum contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsOfEnumsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 5, location(16, 5, 11),
                        List.of(location(16, 5, 11),
                                location(30, 24, 30),
                                location(31, 4, 10))
                },
                {17, 4, location(17, 4, 7),
                        List.of(location(17, 4, 7),
                                location(31, 17, 20),
                                location(33, 11, 14))
                },
                {20, 6, location(20, 6, 15),
                        List.of(location(20, 6, 15),
                                location(24, 10, 19))
                },
                {22, 5, location(22, 5, 12),
                        List.of(location(22, 5, 12),
                                location(28, 14, 21))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_of_enums.bal";
    }
}
