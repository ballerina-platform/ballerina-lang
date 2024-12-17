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
 * Test cases for the find all references API related to do statement contexts.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsInDoStmtTest extends FindAllReferencesTest {

    @Override
    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {21, 33, location(17, 8, 9),
                        List.of(location(17, 8, 9),
                                location(19, 16, 17),
                                location(21, 33, 34))
                },
                {20, 20, location(20, 20, 21),
                        List.of(location(20, 20, 21),
                                location(21, 19, 20))
                },
                {21, 19, location(20, 20, 21),
                        List.of(location(20, 20, 21),
                                location(21, 19, 20))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_ref_do_stmt_context.bal";
    }
}
