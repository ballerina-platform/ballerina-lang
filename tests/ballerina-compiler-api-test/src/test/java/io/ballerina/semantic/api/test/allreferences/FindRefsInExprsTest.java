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
 * Test cases for the find all references API when the references are in binary expressions.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInExprsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                // Simple var refs, binary exprs
                {22, 12, location(22, 12, 13),
                        List.of(location(22, 12, 13),
                                location(23, 21, 22),
                                location(29, 37, 38),
                                location(32, 25, 26))
                },
                {23, 25, location(19, 8, 9),
                        List.of(location(19, 8, 9),
                                location(23, 25, 26),
                                location(29, 29, 30),
                                location(35, 21, 22))
                },
                {16, 4, location(16, 4, 5),
                        List.of(location(16, 4, 5),
                                location(19, 17, 18)),
                },
                {29, 20, location(29, 20, 21), List.of(location(29, 20, 21))},
                {29, 19, null, EMPTY_LIST},

                // function and action invocations
                {41, 4, location(52, 9, 12),
                        List.of(location(41, 4, 7),
                                location(44, 8, 11),
                                location(49, 22, 25),
                                location(49, 32, 35),
                                location(52, 9, 12))
                },
                {58, 19, location(55, 9, 12),
                        List.of(location(55, 9, 12),
                                location(58, 19, 22),
                                location(61, 19, 22))
                },
                {58, 8, location(58, 8, 10),
                        List.of(location(58, 8, 10),
                                location(59, 17, 19),
                                location(62, 23, 25))
                },

                {171, 25, location(174, 9, 19),
                        List.of(location(174, 9, 19),
                                location(171, 25, 35))
                },

                // Template literals
                {76, 8, location(76, 8, 9),
                        List.of(location(76, 8, 9),
                                location(77, 25, 26),
                                location(77, 29, 30),
                                location(78, 64, 65),
                                location(79, 25, 26))
                },
                // List and map constructors
                {82, 44, location(82, 44, 45),
                        List.of(location(82, 44, 45),
                                location(83, 20, 21),
                                location(83, 23, 24),
                                location(84, 25, 26))
                },
                // Access exprs
                {88, 14, location(88, 14, 18),
                        List.of(location(88, 14, 18),
                                location(89, 18, 22))
                },
                {89, 23, location(66, 11, 15),
                        List.of(location(66, 11, 15),
                                location(70, 13, 17),
                                location(89, 23, 27))
                },
                {92, 15, location(92, 15, 19),
                        List.of(location(92, 15, 19),
                                location(94, 17, 21),
                                location(95, 18, 22),
                                location(98, 27, 31))
                },
                {93, 12, location(93, 12, 15),
                        List.of(location(93, 12, 15),
                                location(96, 23, 26))
                },
                {94, 7, location(94, 7, 13),
                        List.of(location(94, 7, 13),
                                location(95, 11, 17),
                                location(96, 15, 21),
                                location(98, 19, 25))
                },
                {98, 26, location(92, 15, 19),
                        List.of(location(92, 15, 19),
                                location(94, 17, 21),
                                location(95, 18, 22),
                                location(98, 27, 31))
                },
                // Query exprs
                {114, 22, location(114, 21, 23),
                        List.of(location(114, 21, 23),
                                location(115, 14, 16),
                                location(116, 22, 24))
                },
                {114, 33, location(112, 14, 22),
                        List.of(location(112, 14, 22),
                                location(114, 27, 35))
                },
                {115, 20, location(102, 11, 15),
                        List.of(location(102, 11, 15),
                                location(108, 18, 22),
                                location(109, 18, 22),
                                location(110, 18, 22),
                                location(115, 17, 21),
                                location(116, 25, 29))
                },
                {116, 28, location(102, 11, 15),
                        List.of(location(102, 11, 15),
                                location(108, 18, 22),
                                location(109, 18, 22),
                                location(110, 18, 22),
                                location(115, 17, 21),
                                location(116, 25, 29))
                },
                // XML Navigation
                {121, 15, location(120, 8, 11),
                        List.of(location(120, 8, 11),
                                location(121, 14, 17))
                },
                // Keys in mapping constructor
                {128, 8, location(144, 11, 15),
                        List.of(location(128, 8, 12),
                                location(144, 11, 15))
                },
                {131, 11, location(125, 12, 16),
                        List.of(location(125, 12, 16),
                                location(131, 11, 15))
                },
                {154, 11, location(154, 11, 15),
                        List.of(location(135, 8, 12),
                                location(154, 11, 15))
                },
                {158, 15, location(158, 15, 19),
                        List.of(location(137, 18, 22),
                                location(158, 15, 19))
                },
                // Constant expressions
                {163, 6, location(163, 6, 12),
                        List.of(location(166, 8, 14),
                                location(163, 6, 12))
                },
                // List spread operator
                {178, 6, location(178, 6, 8),
                        List.of(location(178, 6, 8),
                                location(179, 15, 17))
                },
                {179, 6, location(179, 6, 8),
                        List.of(location(179, 6, 8),
                                location(184, 19, 21))
                },
                {182, 10, location(182, 10, 12),
                        List.of(location(182, 10, 12),
                                location(183, 19, 21))
                },
                // Regexp expression
                {190, 19, location(190, 18, 20),
                        List.of(location(190, 18, 20),
                                location(191, 34, 36))
                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_exprs.bal";
    }
}
