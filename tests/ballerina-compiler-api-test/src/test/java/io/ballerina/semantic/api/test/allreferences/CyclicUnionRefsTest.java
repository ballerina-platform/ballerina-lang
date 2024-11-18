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

import io.ballerina.tools.diagnostics.Location;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for the find all references API related to XML contexts.
 *
 * @since 2.0.0
 */
@Test
public class CyclicUnionRefsTest extends FindAllReferencesTest {

    @Override
    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        Location defLocation = location(16, 5, 16);
        return new Object[][]{
                {16, 13, defLocation,
                        List.of(location(16, 21, 32),
                                defLocation,
                                location(17, 21, 32),
                                location(19, 14, 25),
                                location(20, 4, 15))
                },
                {17, 30, defLocation,
                        List.of(location(16, 21, 32),
                                defLocation,
                                location(17, 21, 32),
                                location(19, 14, 25),
                                location(20, 4, 15))
                },
                {19, 21, defLocation,
                        List.of(location(16, 21, 32),
                                defLocation,
                                location(17, 21, 32),
                                location(19, 14, 25),
                                location(20, 4, 15))
                },
                {20, 11, defLocation,
                        List.of(location(16, 21, 32),
                                defLocation,
                                location(17, 21, 32),
                                location(19, 14, 25),
                                location(20, 4, 15))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_cyclic_unions.bal";
    }
}
