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
 * Test cases for the find all references API related to service declarations.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInServiceDeclTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 5, List.of(location(16, 5, 17),
                                location(23, 5, 17))
                },
                {48, 24, List.of(location(47, 38, 39),
                                 location(48, 24, 25))
                },
                {47, 52, List.of(location(47, 52, 53),
                                 location(49, 32, 33))
                },
                {60, 18, List.of(location(60, 18, 21),
                                 location(66, 31, 34))
                },
                {62, 5, List.of(location(66, 8, 25),
                                location(62, 5, 22))
                },
                {68, 18, List.of(location(68, 18, 23),
                                 location(70, 74, 79))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/service_symbol_test.bal";
    }
}
