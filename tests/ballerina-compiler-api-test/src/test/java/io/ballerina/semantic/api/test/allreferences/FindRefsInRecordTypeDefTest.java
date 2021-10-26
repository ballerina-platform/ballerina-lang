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
 * Test cases for the find all references in record type defintions.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInRecordTypeDefTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {23, 5, location(16, 12, 19),
                        List.of(location(23, 5, 12),
                                location(16, 12, 19))
                },
                {31, 4, location(26, 12, 29),
                        List.of(location(26, 12, 29),
                                location(28, 20, 37),
                                location(31, 4, 21),
                                location(32, 4, 21))
                },
                {33, 4, location(22, 12, 15),
                        List.of(location(22, 12, 15),
                                location(33, 4, 7))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_within_record_typedef.bal";
    }
}
