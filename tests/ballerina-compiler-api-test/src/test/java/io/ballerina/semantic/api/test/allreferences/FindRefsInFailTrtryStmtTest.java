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
 * Test cases for the find all references API related to regular compound statement contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInFailTrtryStmtTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {17, 11, location(17, 11, 14),
                        List.of(location(17, 11, 14),
                                location(23, 12, 15),
                                location(26, 8, 11))
                },
                {24, 12, location(49, 9, 13),
                        List.of(location(24, 12, 16),
                                location(35, 8, 12),
                                location(49, 9, 13))
                },
                {33, 10, location(56, 6, 14),
                        List.of(location(56, 6, 14),
                                location(33, 10, 18))
                },
                {36, 13, location(52, 9, 14),
                        List.of(location(36, 13, 18),
                                location(45, 13, 18),
                                location(52, 9, 14))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_refs_in_fail_retry_stmt.bal";
    }
}
