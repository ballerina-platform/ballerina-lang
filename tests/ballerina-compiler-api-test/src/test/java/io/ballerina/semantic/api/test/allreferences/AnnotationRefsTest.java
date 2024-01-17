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
 * Test cases for the find all references API related to annotation contexts.
 *
 * @since 2.0.0
 */
@Test
public class AnnotationRefsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {37, 24, location(37, 24, 26),
                        List.of(location(37, 24, 26),
                                location(45, 1, 3),
                                location(60, 1, 3),
                                location(57, 22, 24))
                },
                {63, 1, location(38, 19, 21),
                        List.of(location(38, 19, 21),
                                location(63, 1, 3),
                                location(66, 1, 3))
                },
                {38, 19, location(38, 19, 21),
                        List.of(location(38, 19, 21),
                                location(63, 1, 3),
                                location(66, 1, 3))
                },
                {39, 24, location(39, 24, 26),
                        List.of(location(39, 24, 26),
                                location(72, 5, 7))
                },
                {40, 17, location(40, 17, 19),
                        List.of(location(40, 17, 19),
                                location(75, 29, 31),
                                location(76, 29, 31),
                                location(77, 29, 31))
                },
                {86, 8, location(85, 18, 20),
                        List.of(location(86, 8, 10),
                                location(85, 18, 20),
                                location(87, 17, 19))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_annotation_context.bal";
    }
}
