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

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {40, 23, location(22, 21, 24),
                        List.of(location(22, 21, 24),
                                location(40, 23, 54),
                                location(46, 16, 50))
                },
                {41, 14, location(29, 21, 24),
                        List.of(location(29, 21, 24),
                                location(41, 14, 41))
                },
                {42, 17, location(33, 21, 25),
                        List.of(location(33, 21, 25),
                                location(42, 17, 53))
                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_resource_access_action.bal";
    }
}
