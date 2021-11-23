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
 * Test cases for the find all references API related to class and objects contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInClassObjectTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 6, location(16, 6, 12),
                        List.of(location(16, 6, 12),
                                location(39, 8, 14))
                },
                {17, 13, location(17, 13, 17),
                        List.of(location(17, 13, 17),
                                location(40, 11, 15))
                },
                {21, 5, location(21, 5, 11),
                        List.of(location(21, 5, 11),
                                location(28, 5, 11),
                                location(54, 4, 10))
                },
                {22, 11, location(22, 11, 15),
                        List.of(location(22, 11, 15))
                },
                {24, 13, location(24, 13, 20),
                        List.of(location(24, 13, 20))
                },
                {30, 13, location(30, 13, 17),
                        List.of(location(30, 13, 17))
                },
                {34, 13, location(34, 13, 20),
                        List.of(location(34, 13, 20),
                                location(56, 26, 33))
                },
                {43, 31, location(43, 31, 36),
                        List.of(location(43, 31, 36),
                                location(60, 26, 31))
                },
                {46, 13, location(46, 13, 20),
                        List.of(location(46, 13, 20),
                                location(62, 4, 11))
                },
                {64, 23, location(47, 13, 17),
                        List.of(location(47, 13, 17),
                                location(64, 23, 27))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_refs_in_class_object.bal";
    }
}
