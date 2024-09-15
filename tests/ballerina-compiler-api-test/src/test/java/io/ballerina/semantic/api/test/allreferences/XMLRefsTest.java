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
 * Test cases for the find all references API related to XML contexts.
 *
 * @since 2.0.0
 */
@Test
public class XMLRefsTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 33, location(16, 33, 36),
                        List.of(location(16, 33, 36),
                                location(21, 24, 27),
                                location(26, 17, 20),
                                location(27, 17, 20),
                                location(29, 16, 19),
                                location(34, 23, 26))
                },
                {23, 25, location(19, 37, 40),
                        List.of(location(19, 37, 40),
                                location(23, 25, 28),
                                location(27, 23, 26),
                                location(28, 17, 20),
                                location(32, 20, 23))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_xml_context.bal";
    }
}
