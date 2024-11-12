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
 * Test cases for the find all references API related to resource access action.
 *
 * @since 2201.2.0
 */
@Test
public class FindRefsInResourceAccessActionTest extends FindAllReferencesTest {

    @Override
    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {46, 4, location(45, 8, 17),
                        List.of(location(45, 8, 17),
                                location(46, 4, 13),
                                location(47, 24, 33))
                },
                {46, 15, location(22, 22, 25),
                        List.of(location(22, 22, 25),
                                location(46, 15, 16))
                },
                {46, 16, location(22, 22, 25),
                        List.of(location(22, 22, 25),
                                location(46, 15, 16))
                },
                // Resource function invocation
                // Note: The symbol location of the resource-function is set to function-name's location
                {25, 22, location(25, 22, 25),
                        List.of(location(25, 22, 25),
                                location(47, 35, 49),
                                location(51, 25, 40))
                },
                {47, 35, location(25, 22, 25),
                        List.of(location(25, 22, 25),
                                location(47, 35, 49),
                                location(51, 25, 40))
                },
                {52, 26, location(35, 22, 25),
                        List.of(location(35, 22, 25),
                                location(52, 26, 36))
                },
                {54, 29, location(39, 22, 26), List.of()
                },
                {54, 31, location(39, 27, 30),
                        List.of(location(39, 27, 30),
                                location(54, 30, 33))
                },
                // Resource path name segments
                {47, 36, location(25, 26, 29),
                        List.of(location(25, 26, 29),
                                location(47, 36, 39),
                                location(51, 26, 29))
                },
                {51, 26, location(25, 26, 29),
                        List.of(location(25, 26, 29),
                                location(47, 36, 39),
                                location(51, 26, 29))
                },
                {25, 26, location(25, 26, 29),
                        List.of(location(25, 26, 29),
                                location(47, 36, 39),
                                location(51, 26, 29))
                },
                {25, 30, location(25, 30, 33),
                        List.of(location(25, 30, 33),
                                location(47, 40, 43),
                                location(51, 30, 33))
                },
                {25, 42, location(25, 42, 44),
                        List.of(location(25, 42, 44),
                                location(27, 42, 44))
                },
                {27, 42, location(25, 42, 44),
                        List.of(location(25, 42, 44),
                                location(27, 42, 44))
                },
                {47, 40, location(25, 30, 33),
                        List.of(location(25, 30, 33),
                                location(47, 40, 43),
                                location(51, 30, 33))
                },
                {53, 26, location(32, 46, 49),
                        List.of(location(32, 46, 49),
                                location(53, 26, 29))
                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_resource_access_action.bal";
    }
}
