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
 * Test cases for the find all references API when the references spread across in lambda functions.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInLambdasTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 7, List.of(location(16, 7, 15),
                                location(19, 11, 19),
                                location(26, 15, 23),
                                location(29, 44, 52))
                },
                {26, 32, List.of(location(23, 11, 15),
                                 location(26, 32, 36),
                                 location(29, 61, 65),
                                 location(31, 65, 69))
                },
                {31, 44, List.of(location(31, 44, 49),
                                 location(31, 78, 83))
                },
                {35, 8, List.of(location(35, 8, 9),
                                location(41, 19, 20))
                },
                {38, 12, List.of(location(38, 12, 13),
                                 location(41, 28, 29))
                },
                {37, 28, List.of(location(37, 28, 30),
                                 location(41, 23, 25))
                },
                {40, 32, List.of(location(40, 32, 34),
                                 location(41, 32, 34))
                },
                // TODO: disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/26893
//                {47, 8, List.of(location(47, 8, 9),
//                                location(50, 77, 78))
//                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_within_lambdas.bal";
    }
}
