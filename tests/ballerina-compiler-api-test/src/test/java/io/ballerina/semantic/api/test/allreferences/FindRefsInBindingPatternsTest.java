/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for the find all references API when the symbols are within binding patterns.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInBindingPatternsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {20, 8, location(20, 8, 12),
                        List.of(location(20, 8, 12),
                                location(23, 5, 9)),
                },
                {23, 20, location(22, 23, 27),
                        List.of(location(22, 23, 27),
                                location(23, 20, 24)),
                },
                {23, 29, location(17, 8, 12),
                        List.of(location(17, 8, 12),
                                location(23, 29, 33),
                                location(24, 53, 57)),
                },
                {24, 29, location(24, 29, 33),
                        List.of(location(24, 29, 33),
                                location(25, 15, 19)),
                },
                {24, 44, location(24, 44, 48),
                        List.of(location(24, 44, 48),
                                location(26, 18, 22)),
                },
                {29, 11, location(29, 11, 15),
                        List.of(location(29, 11, 15),
                                location(32, 11, 15)),
                },
                {32, 26, location(31, 16, 20),
                        List.of(location(31, 16, 20),
                                location(32, 26, 30)),
                },
                // TODO: https://github.com/ballerina-platform/ballerina-lang/issues/34606
//                {32, 44, location(51, 11, 15),
//                        List.of(location(51, 11, 15),
//                                location(32, 5, 9),
//                                location(32, 26, 30))
//                },
//                {33, 29, location(33, 29, 32),
//                        List.of(location(33, 29, 32),
//                                location(33, 48, 51),
//                                location(35, 11, 14))
//                },
//                {33, 19, location(33, 19, 23),
//                        List.of(location(33, 19, 23),
//                                location(33, 36, 40))
//                },
                {33, 56, location(33, 56, 60),
                        List.of(location(33, 56, 60),
                                location(36, 19, 23))
                },
                {40, 11, location(40, 11, 14),
                        List.of(location(40, 11, 14),
                                location(44, 16, 19))
                },
                {42, 8, location(42, 8, 12),
                        List.of(location(42, 8, 12),
                                location(44, 33, 37))
                },
                {43, 16, location(43, 16, 20),
                        List.of(location(43, 16, 20),
                                location(44, 42, 46))
                },
                // TODO: https://github.com/ballerina-platform/ballerina-lang/issues/34615
//                {44, 78, location(49, 30, 34),
//                        List.of(location(49, 30, 34),
//                                location(44, 28, 32),
//                                location(44, 78, 82),
//                                location(45, 36, 40),
//                                location(45, 87, 91))
//                },
                {45, 22, location(45, 22, 26),
                        List.of(location(45, 22, 26),
                                location(46, 11, 15))
                },
                {45, 41, location(45, 41, 46),
                        List.of(location(45, 41, 46),
                                location(48, 11, 16))
                },
                {45, 51, location(45, 51, 55),
                        List.of(location(45, 51, 55),
                                location(47, 11, 15))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_binding_pattern_ctx.bal";
    }
}
