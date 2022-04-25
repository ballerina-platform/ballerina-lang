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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.allreferences;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for the find all references in error binding patterns.
 */
@Test
public class FindRefsInErrorBindingPatternsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 30, location(16, 30, 34),
                        List.of(location(23, 78, 82),
                                location(23, 28, 32),
                                location(24, 73, 77),
                                location(25, 36, 40),
                                location(25, 87, 91),
                                location(26, 31, 35),
                                location(26, 82, 86),
                                location(16, 30, 34))
                },
                {23, 78, location(16, 30, 34),
                        List.of(location(23, 78, 82),
                                location(23, 28, 32),
                                location(24, 73, 77),
                                location(25, 36, 40),
                                location(25, 87, 91),
                                location(26, 31, 35),
                                location(26, 82, 86),
                                location(16, 30, 34))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_in_error_binding_patterns.bal";
    }
}
